/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ide.eclipse.adt.internal.build;

import com.android.ide.eclipse.adt.AdtPlugin;
import com.android.ide.eclipse.adt.AndroidConstants;
import com.android.ide.eclipse.adt.internal.preferences.AdtPrefs;
import com.android.ide.eclipse.adt.internal.preferences.AdtPrefs.BuildVerbosity;
import com.android.ide.eclipse.adt.internal.project.ApkInstallManager;
import com.android.ide.eclipse.adt.internal.project.BaseProjectHelper;
import com.android.ide.eclipse.adt.internal.project.ProjectHelper;
import com.android.ide.eclipse.adt.internal.project.ProjectState;
import com.android.ide.eclipse.adt.internal.sdk.AndroidTargetData;
import com.android.ide.eclipse.adt.internal.sdk.DexWrapper;
import com.android.ide.eclipse.adt.internal.sdk.Sdk;
import com.android.jarutils.DebugKeyProvider;
import com.android.jarutils.JavaResourceFilter;
import com.android.jarutils.SignedJarBuilder;
import com.android.jarutils.DebugKeyProvider.IKeyGenOutput;
import com.android.jarutils.DebugKeyProvider.KeytoolException;
import com.android.jarutils.SignedJarBuilder.IZipEntryFilter;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.android.sdklib.internal.project.ApkSettings;
import com.android.sdklib.xml.AndroidManifest;
import com.android.sdklib.xml.AndroidXPathFactory;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;

public class ApkBuilder extends BaseBuilder {

    public static final String ID = "com.android.ide.eclipse.adt.ApkBuilder"; //$NON-NLS-1$

    private static final String PROPERTY_CONVERT_TO_DEX = "convertToDex"; //$NON-NLS-1$
    private static final String PROPERTY_PACKAGE_RESOURCES = "packageResources"; //$NON-NLS-1$
    private static final String PROPERTY_BUILD_APK = "buildApk"; //$NON-NLS-1$

    private static final String DX_PREFIX = "Dx"; //$NON-NLS-1$

    final static String GDBSERVER_NAME = "gdbserver"; //$NON-NLS-1$

    /**
     * Dex conversion flag. This is set to true if one of the changed/added/removed
     * file is a .class file. Upon visiting all the delta resource, if this
     * flag is true, then we know we'll have to make the "classes.dex" file.
     */
    private boolean mConvertToDex = false;

    /**
     * Package resources flag. This is set to true if one of the changed/added/removed
     * file is a resource file. Upon visiting all the delta resource, if
     * this flag is true, then we know we'll have to repackage the resources.
     */
    private boolean mPackageResources = false;

    /**
     * Final package build flag.
     */
    private boolean mBuildFinalPackage = false;

    private PrintStream mOutStream = null;
    private PrintStream mErrStream = null;

    /**
     * Basic Resource Delta Visitor class to check if a referenced project had a change in its
     * compiled java files.
     */
    private static class ReferencedProjectDeltaVisitor implements IResourceDeltaVisitor {

        private boolean mConvertToDex = false;
        private boolean mMakeFinalPackage;

        private IPath mOutputFolder;
        private ArrayList<IPath> mSourceFolders;

        private ReferencedProjectDeltaVisitor(IJavaProject javaProject) {
            try {
                mOutputFolder = javaProject.getOutputLocation();
                mSourceFolders = BaseProjectHelper.getSourceClasspaths(javaProject);
            } catch (JavaModelException e) {
            } finally {
            }
        }

        /**
         * {@inheritDoc}
         * @throws CoreException
         */
        public boolean visit(IResourceDelta delta) throws CoreException {
            //  no need to keep looking if we already know we need to convert
            // to dex and make the final package.
            if (mConvertToDex && mMakeFinalPackage) {
                return false;
            }

            // get the resource and the path segments.
            IResource resource = delta.getResource();
            IPath resourceFullPath = resource.getFullPath();

            if (mOutputFolder.isPrefixOf(resourceFullPath)) {
                int type = resource.getType();
                if (type == IResource.FILE) {
                    String ext = resource.getFileExtension();
                    if (AndroidConstants.EXT_CLASS.equals(ext)) {
                        mConvertToDex = true;
                    }
                }
                return true;
            } else {
                for (IPath sourceFullPath : mSourceFolders) {
                    if (sourceFullPath.isPrefixOf(resourceFullPath)) {
                        int type = resource.getType();
                        if (type == IResource.FILE) {
                            // check if the file is a valid file that would be
                            // included during the final packaging.
                            if (checkFileForPackaging((IFile)resource)) {
                                mMakeFinalPackage = true;
                            }

                            return false;
                        } else if (type == IResource.FOLDER) {
                            // if this is a folder, we check if this is a valid folder as well.
                            // If this is a folder that needs to be ignored, we must return false,
                            // so that we ignore its content.
                            return checkFolderForPackaging((IFolder)resource);
                        }
                    }
                }
            }

            return true;
        }

        /**
         * Returns if one of the .class file was modified.
         */
        boolean needDexConvertion() {
            return mConvertToDex;
        }

        boolean needMakeFinalPackage() {
            return mMakeFinalPackage;
        }
    }

    /**
     * Custom {@link IZipEntryFilter} to filter out everything that is not a standard java
     * resources, and also record whether the zip file contains native libraries.
     * <p/>Used in {@link SignedJarBuilder#writeZip(java.io.InputStream, IZipEntryFilter)} when
     * we only want the java resources from external jars.
     */
    private final static class JavaAndNativeResourceFilter extends JavaResourceFilter {
        private final List<String> mNativeLibs = new ArrayList<String>();
        private boolean mNativeLibInteference = false;

        @Override
        public boolean checkEntry(String name) {
            boolean value = super.checkEntry(name);

            // only do additional checks if the file passes the default checks.
            if (value) {
                if (name.endsWith(".so")) {
                    mNativeLibs.add(name);

                    // only .so located in lib/ will interfer with the installation
                    if (name.startsWith("lib/")) {
                        mNativeLibInteference = true;
                    }
                } else if (name.endsWith(".jnilib")) {
                    mNativeLibs.add(name);
                }
            }

            return value;
        }

        List<String> getNativeLibs() {
            return mNativeLibs;
        }

        boolean getNativeLibInterefence() {
            return mNativeLibInteference;
        }

        void clear() {
            mNativeLibs.clear();
            mNativeLibInteference = false;
        }
    }

    private final JavaAndNativeResourceFilter mResourceFilter = new JavaAndNativeResourceFilter();

    public ApkBuilder() {
        super();
    }

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        super.clean(monitor);

        // Get the project.
        IProject project = getProject();

        // Clear the project of the generic markers
        removeMarkersFromProject(project, AndroidConstants.MARKER_AAPT_COMPILE);
        removeMarkersFromProject(project, AndroidConstants.MARKER_PACKAGING);
    }

    // build() returns a list of project from which this project depends for future compilation.
    @SuppressWarnings({"unchecked"})
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
        // get a project object
        IProject project = getProject();

        // list of referenced projects.
        IProject[] libProjects = null;
        IProject[] javaProjects = null;
        IProject[] allRefProjects = null;

        try {
            // get the project info
            ProjectState projectState = Sdk.getProjectState(project);
            if (projectState == null || projectState.isLibrary()) {
                // library project do not need to be dexified or packaged.
                return null;
            }

            // get the libraries
            libProjects = projectState.getLibraryProjects();

            IJavaProject javaProject = JavaCore.create(project);

            // Top level check to make sure the build can move forward.
            abortOnBadSetup(javaProject);

            // get the list of referenced projects.
            javaProjects = ProjectHelper.getReferencedProjects(project);
            IJavaProject[] referencedJavaProjects = getJavaProjects(javaProjects);

            // mix the java project and the library projects
            final int libCount = libProjects != null ? libProjects.length : 0;
            final int javaCount = javaProjects != null ? javaProjects.length : 0;
            allRefProjects = new IProject[libCount + javaCount];
            if (libCount > 0) {
                System.arraycopy(libProjects, 0, allRefProjects, 0, libCount);
            }
            if (javaCount > 0) {
                System.arraycopy(javaProjects, 0, allRefProjects, libCount, javaCount);
            }

            // get the output folder, this method returns the path with a trailing
            // separator
            IFolder outputFolder = BaseProjectHelper.getOutputFolder(project);

            // now we need to get the classpath list
            ArrayList<IPath> sourceList = BaseProjectHelper.getSourceClasspaths(javaProject);

            // First thing we do is go through the resource delta to not
            // lose it if we have to abort the build for any reason.
            ApkDeltaVisitor dv = null;
            if (kind == FULL_BUILD) {
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                        Messages.Start_Full_Apk_Build);

                mPackageResources = true;
                mConvertToDex = true;
                mBuildFinalPackage = true;
            } else {
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                        Messages.Start_Inc_Apk_Build);

                // go through the resources and see if something changed.
                IResourceDelta delta = getDelta(project);
                if (delta == null) {
                    mPackageResources = true;
                    mConvertToDex = true;
                    mBuildFinalPackage = true;
                } else {
                    dv = new ApkDeltaVisitor(this, sourceList, outputFolder);
                    delta.accept(dv);

                    // save the state
                    mPackageResources |= dv.getPackageResources();
                    mConvertToDex |= dv.getConvertToDex();
                    mBuildFinalPackage |= dv.getMakeFinalPackage();
                }

                // if the main resources didn't change, then we check for the library
                // ones (will trigger resource repackaging too)
                if ((mPackageResources == false || mBuildFinalPackage == false) &&
                        libProjects != null && libProjects.length > 0) {
                    for (IProject libProject : libProjects) {
                        delta = getDelta(libProject);
                        if (delta != null) {
                            LibraryDeltaVisitor visitor = new LibraryDeltaVisitor();
                            delta.accept(visitor);

                            mPackageResources |= visitor.getResChange();
                            mBuildFinalPackage |= visitor.getLibChange();

                            if (mPackageResources && mBuildFinalPackage) {
                                break;
                            }
                        }
                    }
                }

                // also go through the delta for all the referenced projects, until we are forced to
                // compile anyway
                for (int i = 0 ; i < referencedJavaProjects.length &&
                        (mBuildFinalPackage == false || mConvertToDex == false); i++) {
                    IJavaProject referencedJavaProject = referencedJavaProjects[i];
                    delta = getDelta(referencedJavaProject.getProject());
                    if (delta != null) {
                        ReferencedProjectDeltaVisitor refProjectDv = new ReferencedProjectDeltaVisitor(
                                referencedJavaProject);
                        delta.accept(refProjectDv);

                        // save the state
                        mConvertToDex |= refProjectDv.needDexConvertion();
                        mBuildFinalPackage |= refProjectDv.needMakeFinalPackage();
                    }
                }
            }

            // store the build status in the persistent storage
            saveProjectBooleanProperty(PROPERTY_CONVERT_TO_DEX , mConvertToDex);
            saveProjectBooleanProperty(PROPERTY_PACKAGE_RESOURCES, mPackageResources);
            saveProjectBooleanProperty(PROPERTY_BUILD_APK, mBuildFinalPackage);

            if (dv != null && dv.mXmlError) {
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                Messages.Xml_Error);

                // if there was some XML errors, we just return w/o doing
                // anything since we've put some markers in the files anyway
                return allRefProjects;
            }

            // remove older packaging markers.
            removeMarkersFromProject(javaProject.getProject(), AndroidConstants.MARKER_PACKAGING);

            if (outputFolder == null) {
                // mark project and exit
                markProject(AndroidConstants.MARKER_PACKAGING, Messages.Failed_To_Get_Output,
                        IMarker.SEVERITY_ERROR);
                return allRefProjects;
            }

            // first thing we do is check that the SDK directory has been setup.
            String osSdkFolder = AdtPlugin.getOsSdkFolder();

            if (osSdkFolder.length() == 0) {
                // this has already been checked in the precompiler. Therefore,
                // while we do have to cancel the build, we don't have to return
                // any error or throw anything.
                return allRefProjects;
            }

            // get the APK configs for the project.
            ProjectState state = Sdk.getProjectState(project);
            Set<Entry<String, String>> apkfilters = null;
            if (state != null) {
                ApkSettings apkSettings = state.getApkSettings();
                if (apkSettings != null) {
                    Map<String, String> filterMap = apkSettings.getResourceFilters();
                    if (filterMap != null && filterMap.size() > 0) {
                        apkfilters = filterMap.entrySet();
                    }
                }
            }

            // do some extra check, in case the output files are not present. This
            // will force to recreate them.
            IResource tmp = null;

            if (mPackageResources == false) {
                // check the full resource package
                tmp = outputFolder.findMember(AndroidConstants.FN_RESOURCES_AP_);
                if (tmp == null || tmp.exists() == false) {
                    mPackageResources = true;
                    mBuildFinalPackage = true;
                } else {
                    // if the full package is present, we check the filtered resource packages
                    // as well
                    if (apkfilters != null) {
                        for (Entry<String, String> entry : apkfilters) {
                            String filename = String.format(AndroidConstants.FN_RESOURCES_S_AP_,
                                    entry.getKey());

                            tmp = outputFolder.findMember(filename);
                            if (tmp == null || (tmp instanceof IFile &&
                                    tmp.exists() == false)) {
                                String msg = String.format(Messages.s_Missing_Repackaging,
                                        filename);
                                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE,
                                        project, msg);
                                mPackageResources = true;
                                mBuildFinalPackage = true;
                                break;
                            }
                        }
                    }
                }
            }

            // check classes.dex is present. If not we force to recreate it.
            if (mConvertToDex == false) {
                tmp = outputFolder.findMember(AndroidConstants.FN_CLASSES_DEX);
                if (tmp == null || tmp.exists() == false) {
                    mConvertToDex = true;
                    mBuildFinalPackage = true;
                }
            }

            // also check the final file(s)!
            String finalPackageName = ProjectHelper.getApkFilename(project, null /*config*/);
            if (mBuildFinalPackage == false) {
                tmp = outputFolder.findMember(finalPackageName);
                if (tmp == null || (tmp instanceof IFile &&
                        tmp.exists() == false)) {
                    String msg = String.format(Messages.s_Missing_Repackaging, finalPackageName);
                    AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project, msg);
                    mBuildFinalPackage = true;
                } else if (apkfilters != null) {
                    // if the full apk is present, we check the filtered apk as well
                    for (Entry<String, String> entry : apkfilters) {
                        String filename = ProjectHelper.getApkFilename(project, entry.getKey());

                        tmp = outputFolder.findMember(filename);
                        if (tmp == null || (tmp instanceof IFile &&
                                tmp.exists() == false)) {
                            String msg = String.format(Messages.s_Missing_Repackaging, filename);
                            AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project, msg);
                            mBuildFinalPackage = true;
                            break;
                        }
                    }
                }
            }

            // at this point we know if we need to recreate the temporary apk
            // or the dex file, but we don't know if we simply need to recreate them
            // because they are missing

            // refresh the output directory first
            IContainer ic = outputFolder.getParent();
            if (ic != null) {
                ic.refreshLocal(IResource.DEPTH_ONE, monitor);
            }

            // we need to test all three, as we may need to make the final package
            // but not the intermediary ones.
            if (mPackageResources || mConvertToDex || mBuildFinalPackage) {
                // resource to the AndroidManifest.xml file
                IFile manifestFile = project.getFile(AndroidConstants.FN_ANDROID_MANIFEST);

                if (manifestFile == null || manifestFile.exists() == false) {
                    // mark project and exit
                    String msg = String.format(Messages.s_File_Missing,
                            AndroidConstants.FN_ANDROID_MANIFEST);
                    markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);
                    return allRefProjects;
                }

                IPath binLocation = outputFolder.getLocation();
                if (binLocation == null) {
                    markProject(AndroidConstants.MARKER_PACKAGING, Messages.Output_Missing,
                            IMarker.SEVERITY_ERROR);
                    return allRefProjects;
                }
                String osBinPath = binLocation.toOSString();

                // Remove the old .apk.
                // This make sure that if the apk is corrupted, then dx (which would attempt
                // to open it), will not fail.
                String osFinalPackagePath = osBinPath + File.separator + finalPackageName;
                File finalPackage = new File(osFinalPackagePath);

                // if delete failed, this is not really a problem, as the final package generation
                // handle already present .apk, and if that one failed as well, the user will be
                // notified.
                finalPackage.delete();

                if (apkfilters != null) {
                    for (Entry<String, String> entry : apkfilters) {
                        String packageFilepath = osBinPath + File.separator +
                                ProjectHelper.getApkFilename(project, entry.getKey());

                        finalPackage = new File(packageFilepath);
                        finalPackage.delete();
                    }
                }

                // first we check if we need to package the resources.
                if (mPackageResources) {
                    // remove some aapt_package only markers.
                    removeMarkersFromContainer(project, AndroidConstants.MARKER_AAPT_PACKAGE);

                    // need to figure out some path before we can execute aapt;

                    // get the resource folder
                    IFolder resFolder = project.getFolder(AndroidConstants.WS_RESOURCES);

                    // and the assets folder
                    IFolder assetsFolder = project.getFolder(AndroidConstants.WS_ASSETS);

                    // we need to make sure this one exists.
                    if (assetsFolder.exists() == false) {
                        assetsFolder = null;
                    }

                    IPath resLocation = resFolder.getLocation();
                    IPath manifestLocation = manifestFile.getLocation();

                    if (resLocation != null && manifestLocation != null) {
                        // list of res folder (main project + maybe libraries)
                        ArrayList<String> osResPaths = new ArrayList<String>();
                        osResPaths.add(resLocation.toOSString()); //main project

                        // libraries?
                        if (libProjects != null) {
                            for (IProject lib : libProjects) {
                                IFolder libResFolder = lib.getFolder(SdkConstants.FD_RES);
                                if (libResFolder.exists()) {
                                    osResPaths.add(libResFolder.getLocation().toOSString());
                                }
                            }
                        }

                        String osManifestPath = manifestLocation.toOSString();

                        String osAssetsPath = null;
                        if (assetsFolder != null) {
                            osAssetsPath = assetsFolder.getLocation().toOSString();
                        }

                        // build the default resource package
                        if (executeAapt(project, osManifestPath, osResPaths,
                                osAssetsPath,
                                osBinPath + File.separator + AndroidConstants.FN_RESOURCES_AP_,
                                null /*configFilter*/) == false) {
                            // aapt failed. Whatever files that needed to be marked
                            // have already been marked. We just return.
                            return allRefProjects;
                        }

                        // now do the same thing for all the configured resource packages.
                        if (apkfilters != null) {
                            for (Entry<String, String> entry : apkfilters) {
                                String outPathFormat = osBinPath + File.separator +
                                        AndroidConstants.FN_RESOURCES_S_AP_;
                                String outPath = String.format(outPathFormat, entry.getKey());
                                if (executeAapt(project, osManifestPath, osResPaths,
                                        osAssetsPath, outPath, entry.getValue()) == false) {
                                    // aapt failed. Whatever files that needed to be marked
                                    // have already been marked. We just return.
                                    return allRefProjects;
                                }
                            }
                        }

                        // build has been done. reset the state of the builder
                        mPackageResources = false;

                        // and store it
                        saveProjectBooleanProperty(PROPERTY_PACKAGE_RESOURCES, mPackageResources);
                    }
                }

                // then we check if we need to package the .class into classes.dex
                if (mConvertToDex) {
                    if (executeDx(javaProject, osBinPath, osBinPath + File.separator +
                            AndroidConstants.FN_CLASSES_DEX, referencedJavaProjects) == false) {
                        // dx failed, we return
                        return allRefProjects;
                    }

                    // build has been done. reset the state of the builder
                    mConvertToDex = false;

                    // and store it
                    saveProjectBooleanProperty(PROPERTY_CONVERT_TO_DEX, mConvertToDex);
                }

                // figure out whether the application is debuggable.
                // It is considered debuggable if the attribute debuggable is set to true
                // in the manifest
                boolean debuggable = false;
                XPath xpath = AndroidXPathFactory.newXPath();
                String result = xpath.evaluate(
                        "/"  + AndroidManifest.NODE_MANIFEST +                //$NON-NLS-1$
                        "/"  + AndroidManifest.NODE_APPLICATION +             //$NON-NLS-1$
                        "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX +        //$NON-NLS-1$
                                ":" + AndroidManifest.ATTRIBUTE_DEBUGGABLE,   //$NON-NLS-1$
                        new InputSource(manifestFile.getContents()));
                if (result.length() > 0) {
                    debuggable = Boolean.valueOf(result);
                }

                // now we need to make the final package from the intermediary apk
                // and classes.dex.
                // This is the default package with all the resources.

                String classesDexPath = osBinPath + File.separator +
                        AndroidConstants.FN_CLASSES_DEX;
                if (finalPackage(osBinPath + File.separator + AndroidConstants.FN_RESOURCES_AP_,
                                classesDexPath, osFinalPackagePath, javaProject, libProjects,
                                referencedJavaProjects, debuggable) == false) {
                    return allRefProjects;
                }

                // now do the same thing for all the configured resource packages.
                if (apkfilters != null) {
                    String resPathFormat = osBinPath + File.separator +
                            AndroidConstants.FN_RESOURCES_S_AP_;

                    for (Entry<String, String> entry : apkfilters) {
                        // make the filename for the resource package.
                        String resPath = String.format(resPathFormat, entry.getKey());

                        // make the filename for the apk to generate
                        String apkOsFilePath = osBinPath + File.separator +
                                ProjectHelper.getApkFilename(project, entry.getKey());
                        if (finalPackage(resPath, classesDexPath, apkOsFilePath, javaProject,
                                libProjects, referencedJavaProjects, debuggable) == false) {
                            return allRefProjects;
                        }
                    }
                }

                // we are done.

                // get the resource to bin
                outputFolder.refreshLocal(IResource.DEPTH_ONE, monitor);

                // build has been done. reset the state of the builder
                mBuildFinalPackage = false;

                // and store it
                saveProjectBooleanProperty(PROPERTY_BUILD_APK, mBuildFinalPackage);

                // reset the installation manager to force new installs of this project
                ApkInstallManager.getInstance().resetInstallationFor(project);

                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, getProject(),
                        "Build Success!");
            }
        } catch (Exception exception) {
            // try to catch other exception to actually display an error. This will be useful
            // if we get an NPE or something so that we can at least notify the user that something
            // went wrong.

            // first check if this is a CoreException we threw to cancel the build.
            if (exception instanceof CoreException) {
                if (((CoreException)exception).getStatus().getSeverity() == IStatus.CANCEL) {
                    // Project is already marked with an error. Nothing to do
                    return allRefProjects;
                }
            }

            String msg = exception.getMessage();
            if (msg == null) {
                msg = exception.getClass().getCanonicalName();
            }

            msg = String.format("Unknown error: %1$s", msg);
            AdtPlugin.printErrorToConsole(project, msg);
            markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);
        }

        return allRefProjects;
    }

    @Override
    protected void startupOnInitialize() {
        super.startupOnInitialize();

        // load the build status. We pass true as the default value to
        // force a recompile in case the property was not found
        mConvertToDex = loadProjectBooleanProperty(PROPERTY_CONVERT_TO_DEX , true);
        mPackageResources = loadProjectBooleanProperty(PROPERTY_PACKAGE_RESOURCES, true);
        mBuildFinalPackage = loadProjectBooleanProperty(PROPERTY_BUILD_APK, true);
    }

    /**
     * Executes aapt. If any error happen, files or the project will be marked.
     * @param project The Project
     * @param osManifestPath The path to the manifest file
     * @param osResPath The path to the res folder
     * @param osAssetsPath The path to the assets folder. This can be null.
     * @param osOutFilePath The path to the temporary resource file to create.
     * @param configFilter The configuration filter for the resources to include
     * (used with -c option, for example "port,en,fr" to include portrait, English and French
     * resources.)
     * @return true if success, false otherwise.
     */
    private boolean executeAapt(IProject project, String osManifestPath,
            List<String> osResPaths, String osAssetsPath, String osOutFilePath,
            String configFilter) {
        IAndroidTarget target = Sdk.getCurrent().getTarget(project);

        // Create the command line.
        ArrayList<String> commandArray = new ArrayList<String>();
        commandArray.add(target.getPath(IAndroidTarget.AAPT));
        commandArray.add("package"); //$NON-NLS-1$
        commandArray.add("-f");//$NON-NLS-1$
        if (AdtPrefs.getPrefs().getBuildVerbosity() == BuildVerbosity.VERBOSE) {
            commandArray.add("-v"); //$NON-NLS-1$
        }

        // if more than one res, this means there's a library (or more) and we need
        // to activate the auto-add-overlay
        if (osResPaths.size() > 1) {
            commandArray.add("--auto-add-overlay"); //$NON-NLS-1$
        }

        if (configFilter != null) {
            commandArray.add("-c"); //$NON-NLS-1$
            commandArray.add(configFilter);
        }

        commandArray.add("-M"); //$NON-NLS-1$
        commandArray.add(osManifestPath);

        for (String path : osResPaths) {
            commandArray.add("-S"); //$NON-NLS-1$
            commandArray.add(path);
        }

        if (osAssetsPath != null) {
            commandArray.add("-A"); //$NON-NLS-1$
            commandArray.add(osAssetsPath);
        }

        commandArray.add("-I"); //$NON-NLS-1$
        commandArray.add(target.getPath(IAndroidTarget.ANDROID_JAR));

        commandArray.add("-F"); //$NON-NLS-1$
        commandArray.add(osOutFilePath);

        String command[] = commandArray.toArray(
                new String[commandArray.size()]);

        if (AdtPrefs.getPrefs().getBuildVerbosity() == BuildVerbosity.VERBOSE) {
            StringBuilder sb = new StringBuilder();
            for (String c : command) {
                sb.append(c);
                sb.append(' ');
            }
            AdtPlugin.printToConsole(project, sb.toString());
        }

        // launch
        int execError = 1;
        try {
            // launch the command line process
            Process process = Runtime.getRuntime().exec(command);

            // list to store each line of stderr
            ArrayList<String> results = new ArrayList<String>();

            // get the output and return code from the process
            execError = grabProcessOutput(process, results);

            // attempt to parse the error output
            boolean parsingError = parseAaptOutput(results, project);

            // if we couldn't parse the output we display it in the console.
            if (parsingError) {
                if (execError != 0) {
                    AdtPlugin.printErrorToConsole(project, results.toArray());
                } else {
                    AdtPlugin.printBuildToConsole(BuildVerbosity.ALWAYS, project,
                            results.toArray());
                }
            }

            // We need to abort if the exec failed.
            if (execError != 0) {
                // if the exec failed, and we couldn't parse the error output (and therefore
                // not all files that should have been marked, were marked), we put a generic
                // marker on the project and abort.
                if (parsingError) {
                    markProject(AndroidConstants.MARKER_PACKAGING, Messages.Unparsed_AAPT_Errors,
                            IMarker.SEVERITY_ERROR);
                }

                // abort if exec failed.
                return false;
            }
        } catch (IOException e1) {
            String msg = String.format(Messages.AAPT_Exec_Error, command[0]);
            markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);
            return false;
        } catch (InterruptedException e) {
            String msg = String.format(Messages.AAPT_Exec_Error, command[0]);
            markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);
            return false;
        }

        return true;
    }

    /**
     * Execute the Dx tool for dalvik code conversion.
     * @param javaProject The java project
     * @param osBinPath the path to the output folder of the project
     * @param osOutFilePath the path of the dex file to create.
     * @param referencedJavaProjects the list of referenced projects for this project.
     *
     * @throws CoreException
     */
    private boolean executeDx(IJavaProject javaProject, String osBinPath, String osOutFilePath,
            IJavaProject[] referencedJavaProjects) throws CoreException {
        IAndroidTarget target = Sdk.getCurrent().getTarget(javaProject.getProject());
        AndroidTargetData targetData = Sdk.getCurrent().getTargetData(target);
        if (targetData == null) {
            throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                    Messages.ApkBuilder_UnableBuild_Dex_Not_loaded));
        }

        // get the dex wrapper
        DexWrapper wrapper = targetData.getDexWrapper();

        if (wrapper == null) {
            throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                    Messages.ApkBuilder_UnableBuild_Dex_Not_loaded));
        }

        // make sure dx use the proper output streams.
        // first make sure we actually have the streams available.
        if (mOutStream == null) {
            IProject project = getProject();
            mOutStream = AdtPlugin.getOutPrintStream(project, DX_PREFIX);
            mErrStream = AdtPlugin.getErrPrintStream(project, DX_PREFIX);
        }

        try {
            // get the list of libraries to include with the source code
            String[] libraries = getExternalJars();

            // get the list of referenced projects output to add
            String[] projectOutputs = getProjectOutputs(referencedJavaProjects);

            String[] fileNames = new String[1 + projectOutputs.length + libraries.length];

            // first this project output
            fileNames[0] = osBinPath;

            // then other project output
            System.arraycopy(projectOutputs, 0, fileNames, 1, projectOutputs.length);

            // then external jars.
            System.arraycopy(libraries, 0, fileNames, 1 + projectOutputs.length, libraries.length);

            int res = wrapper.run(osOutFilePath, fileNames,
                    AdtPrefs.getPrefs().getBuildVerbosity() == BuildVerbosity.VERBOSE,
                    mOutStream, mErrStream);

            if (res != 0) {
                // output error message and marker the project.
                String message = String.format(Messages.Dalvik_Error_d,
                        res);
                AdtPlugin.printErrorToConsole(getProject(), message);
                markProject(AndroidConstants.MARKER_PACKAGING, message, IMarker.SEVERITY_ERROR);
                return false;
            }
        } catch (Throwable ex) {
            String message = ex.getMessage();
            if (message == null) {
                message = ex.getClass().getCanonicalName();
            }
            message = String.format(Messages.Dalvik_Error_s, message);
            AdtPlugin.printErrorToConsole(getProject(), message);
            markProject(AndroidConstants.MARKER_PACKAGING, message, IMarker.SEVERITY_ERROR);
            if ((ex instanceof NoClassDefFoundError)
                    || (ex instanceof NoSuchMethodError)) {
                AdtPlugin.printErrorToConsole(getProject(), Messages.Incompatible_VM_Warning,
                        Messages.Requires_1_5_Error);
            }
            return false;
        }

        return true;
    }

    /**
     * Makes the final package. Package the dex files, the temporary resource file into the final
     * package file.
     * @param intermediateApk The path to the temporary resource file.
     * @param dex The path to the dex file.
     * @param output The path to the final package file to create.
     * @param javaProject the java project being compiled
     * @param libProjects an optional list of library projects (can be null)
     * @param referencedJavaProjects referenced projects.
     * @param debuggable whether the project manifest has debuggable==true. If true, any gdbserver
     * executables will be packaged with the native libraries.
     * @return true if success, false otherwise.
     */
    private boolean finalPackage(String intermediateApk, String dex, String output,
            final IJavaProject javaProject, IProject[] libProjects,
            IJavaProject[] referencedJavaProjects, boolean debuggable) {

        FileOutputStream fos = null;
        try {
            IPreferenceStore store = AdtPlugin.getDefault().getPreferenceStore();
            String osKeyPath = store.getString(AdtPrefs.PREFS_CUSTOM_DEBUG_KEYSTORE);
            if (osKeyPath == null || new File(osKeyPath).exists() == false) {
                osKeyPath = DebugKeyProvider.getDefaultKeyStoreOsPath();
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, getProject(),
                        Messages.ApkBuilder_Using_Default_Key);
            } else {
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, getProject(),
                        String.format(Messages.ApkBuilder_Using_s_To_Sign, osKeyPath));
            }

            // TODO: get the store type from somewhere else.
            DebugKeyProvider provider = new DebugKeyProvider(osKeyPath, null /* storeType */,
                    new IKeyGenOutput() {
                        public void err(String message) {
                            AdtPlugin.printErrorToConsole(javaProject.getProject(),
                                    Messages.ApkBuilder_Signing_Key_Creation_s + message);
                        }

                        public void out(String message) {
                            AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE,
                                    javaProject.getProject(),
                                    Messages.ApkBuilder_Signing_Key_Creation_s + message);
                        }
            });
            PrivateKey key = provider.getDebugKey();
            X509Certificate certificate = (X509Certificate)provider.getCertificate();

            if (key == null) {
                String msg = String.format(Messages.Final_Archive_Error_s,
                        Messages.ApkBuilder_Unable_To_Gey_Key);
                AdtPlugin.printErrorToConsole(javaProject.getProject(), msg);
                markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);
                return false;
            }

            // compare the certificate expiration date
            if (certificate != null && certificate.getNotAfter().compareTo(new Date()) < 0) {
                // TODO, regenerate a new one.
                String msg = String.format(Messages.Final_Archive_Error_s,
                    String.format(Messages.ApkBuilder_Certificate_Expired_on_s,
                            DateFormat.getInstance().format(certificate.getNotAfter())));
                AdtPlugin.printErrorToConsole(javaProject.getProject(), msg);
                markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);
                return false;
            }

            // create the jar builder.
            fos = new FileOutputStream(output);
            SignedJarBuilder builder = new SignedJarBuilder(fos, key, certificate);

            // add the intermediate file containing the compiled resources.
            AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, getProject(),
                    String.format(Messages.ApkBuilder_Packaging_s, intermediateApk));
            FileInputStream fis = new FileInputStream(intermediateApk);
            try {
                builder.writeZip(fis, null /* filter */);
            } finally {
                fis.close();
            }

            // Now we add the new file to the zip archive for the classes.dex file.
            AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, getProject(),
                    String.format(Messages.ApkBuilder_Packaging_s,
                            AndroidConstants.FN_CLASSES_DEX));
            File entryFile = new File(dex);
            builder.writeFile(entryFile, AndroidConstants.FN_CLASSES_DEX);

            // Now we write the standard resources from the project and the referenced projects.
            writeStandardResources(builder, javaProject, referencedJavaProjects);

            // Now we write the standard resources from the external libraries
            for (String libraryOsPath : getExternalJars()) {
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, getProject(),
                        String.format(Messages.ApkBuilder_Packaging_s, libraryOsPath));
                try {
                    fis = new FileInputStream(libraryOsPath);
                    mResourceFilter.clear();
                    builder.writeZip(fis, mResourceFilter);

                    // check if we found native libraries in the external library. This
                    // constitutes an error or warning depending on if they are in lib/
                    List<String> nativeLibs = mResourceFilter.getNativeLibs();
                    boolean nativeInterference = mResourceFilter.getNativeLibInterefence();
                    if (nativeLibs.size() > 0) {
                        String libName = new File(libraryOsPath).getName();
                        String msg = String.format("Native libraries detected in '%1$s'. See console for more information.",
                                libName);


                        markProject(AndroidConstants.MARKER_PACKAGING, msg,
                                nativeInterference ||
                                        AdtPrefs.getPrefs().getBuildForceErrorOnNativeLibInJar() ?
                                        IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING);

                        ArrayList<String> consoleMsgs = new ArrayList<String>();
                        consoleMsgs.add(String.format(
                                "The library '%1$s' contains native libraries that will not run on the device.",
                                libName));
                        if (nativeInterference) {
                            consoleMsgs.add("Additionally some of those libraries will interfer with the installation of the application because of their location in lib/");
                            consoleMsgs.add("lib/ is reserved for NDK libraries.");
                        }
                        consoleMsgs.add("The following libraries were found:");
                        for (String lib : nativeLibs) {
                            consoleMsgs.add(" - " + lib);
                        }
                        AdtPlugin.printErrorToConsole(javaProject.getProject(),
                                consoleMsgs.toArray());

                        return false;
                    }
                } finally {
                    fis.close();
                }
            }

            // now write the native libraries.
            // First look if the lib folder is there.
            IResource libFolder = javaProject.getProject().findMember(SdkConstants.FD_NATIVE_LIBS);
            if (libFolder != null && libFolder.exists() &&
                    libFolder.getType() == IResource.FOLDER) {
                // look inside and put .so in lib/* by keeping the relative folder path.
                writeNativeLibraries((IFolder) libFolder, builder, debuggable);
            }

            // write the native libraries for the library projects.
            if (libProjects != null) {
                for (IProject lib : libProjects) {
                    libFolder = lib.findMember(SdkConstants.FD_NATIVE_LIBS);
                    if (libFolder != null && libFolder.exists() &&
                            libFolder.getType() == IResource.FOLDER) {
                        // look inside and put .so in lib/* by keeping the relative folder path.
                        writeNativeLibraries((IFolder) libFolder, builder, debuggable);
                    }
                }
            }

            // close the jar file and write the manifest and sign it.
            builder.close();
        } catch (GeneralSecurityException e1) {
            // mark project and return
            String msg = String.format(Messages.Final_Archive_Error_s, e1.getMessage());
            AdtPlugin.printErrorToConsole(javaProject.getProject(), msg);
            markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);
            return false;
        } catch (IOException e1) {
            // mark project and return
            String msg = String.format(Messages.Final_Archive_Error_s, e1.getMessage());
            AdtPlugin.printErrorToConsole(javaProject.getProject(), msg);
            markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);
            return false;
        } catch (KeytoolException e) {
            String eMessage = e.getMessage();

            // mark the project with the standard message
            String msg = String.format(Messages.Final_Archive_Error_s, eMessage);
            markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);

            // output more info in the console
            AdtPlugin.printErrorToConsole(javaProject.getProject(),
                    msg,
                    String.format(Messages.ApkBuilder_JAVA_HOME_is_s, e.getJavaHome()),
                    Messages.ApkBuilder_Update_or_Execute_manually_s,
                    e.getCommandLine());
        } catch (AndroidLocationException e) {
            String eMessage = e.getMessage();

            // mark the project with the standard message
            String msg = String.format(Messages.Final_Archive_Error_s, eMessage);
            markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);

            // and also output it in the console
            AdtPlugin.printErrorToConsole(javaProject.getProject(), msg);
        } catch (CoreException e) {
            // mark project and return
            String msg = String.format(Messages.Final_Archive_Error_s, e.getMessage());
            AdtPlugin.printErrorToConsole(javaProject.getProject(), msg);
            markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);
            return false;
        } catch (Exception e) {
            // try to catch other exception to actually display an error. This will be useful
            // if we get an NPE or something so that we can at least notify the user that something
            // went wrong (otherwise the build appears to succeed but the zip archive is not closed
            // and therefore invalid.
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.getClass().getCanonicalName();
            }

            msg = String.format("Unknown error: %1$s", msg);
            AdtPlugin.printErrorToConsole(javaProject.getProject(), msg);
            markProject(AndroidConstants.MARKER_PACKAGING, msg, IMarker.SEVERITY_ERROR);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // pass.
                }
            }
        }

        return true;
    }

    /**
     * Writes native libraries into a {@link SignedJarBuilder}.
     * <p/>The native libraries must be located in a given main folder. Under this folder, it is
     * expected that the libraries are under a sub-folder that represents the ABI of the library.
     *
     * The path in the archive is based on the ABI folder name, and located under a main
     * folder called "lib".
     *
     * This method also packages any "gdbserver" executable it finds in the ABI folders, if
     * <var>debuggable</var> is set to true.
     *
     * @param rootFolder The folder containing the native libraries.
     * @param jarBuilder the {@link SignedJarBuilder} used to create the archive.
     * @param debuggable whether the application is debuggable. If <code>true</code> then gdbserver
     * executables will be packaged as well.
     * @throws CoreException
     * @throws IOException
     */
    private void writeNativeLibraries(IFolder rootFolder, SignedJarBuilder jarBuilder,
            boolean debuggable)
            throws CoreException, IOException {
        // the native files must be under a single sub-folder under the main root folder.
        // the sub-folder represents the abi for the native libs
        IResource[] abis = rootFolder.members();
        for (IResource abi : abis) {
            if (abi.getType() == IResource.FOLDER) { // ignore non folders.
                IResource[] libs = ((IFolder)abi).members();

                for (IResource lib : libs) {
                    if (lib.getType() == IResource.FILE) { // ignore non files.
                        IPath path = lib.getFullPath();

                        // check the extension.
                        String ext = path.getFileExtension();
                        if (AndroidConstants.EXT_NATIVE_LIB.equalsIgnoreCase(ext) ||
                                (debuggable && GDBSERVER_NAME.equals(lib.getName()))) {
                            // compute the path inside the archive.
                            IPath apkPath = new Path(SdkConstants.FD_APK_NATIVE_LIBS);
                            apkPath = apkPath.append(abi.getName()).append(lib.getName());

                            // writes the file in the apk.
                            jarBuilder.writeFile(lib.getLocation().toFile(), apkPath.toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * Writes the standard resources of a project and its referenced projects
     * into a {@link SignedJarBuilder}.
     * Standard resources are non java/aidl files placed in the java package folders.
     * @param jarBuilder the {@link SignedJarBuilder}.
     * @param javaProject the javaProject object.
     * @param referencedJavaProjects the java projects that this project references.
     * @throws IOException
     * @throws CoreException
     */
    private void writeStandardResources(SignedJarBuilder jarBuilder, IJavaProject javaProject,
            IJavaProject[] referencedJavaProjects) throws IOException, CoreException {
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot wsRoot = ws.getRoot();

        // create a list of path already put into the archive, in order to detect conflict
        ArrayList<String> list = new ArrayList<String>();

        writeStandardProjectResources(jarBuilder, javaProject, wsRoot, list);

        for (IJavaProject referencedJavaProject : referencedJavaProjects) {
            // only include output from non android referenced project
            // (This is to handle the case of reference Android projects in the context of
            // instrumentation projects that need to reference the projects to be tested).
            if (referencedJavaProject.getProject().hasNature(AndroidConstants.NATURE) == false) {
                writeStandardProjectResources(jarBuilder, referencedJavaProject, wsRoot, list);
            }
        }
    }

    /**
     * Writes the standard resources of a {@link IJavaProject} into a {@link SignedJarBuilder}.
     * Standard resources are non java/aidl files placed in the java package folders.
     * @param jarBuilder the {@link SignedJarBuilder}.
     * @param javaProject the javaProject object.
     * @param wsRoot the {@link IWorkspaceRoot}.
     * @param list a list of files already added to the archive, to detect conflicts.
     * @throws IOException
     */
    private void writeStandardProjectResources(SignedJarBuilder jarBuilder,
            IJavaProject javaProject, IWorkspaceRoot wsRoot, ArrayList<String> list)
            throws IOException {
        // get the source pathes
        ArrayList<IPath> sourceFolders = BaseProjectHelper.getSourceClasspaths(javaProject);

        // loop on them and then recursively go through the content looking for matching files.
        for (IPath sourcePath : sourceFolders) {
            IResource sourceResource = wsRoot.findMember(sourcePath);
            if (sourceResource != null && sourceResource.getType() == IResource.FOLDER) {
                writeStandardSourceFolderResources(jarBuilder, sourcePath, (IFolder)sourceResource,
                        list);
            }
        }
    }

    /**
     * Recursively writes the standard resources of a source folder into a {@link SignedJarBuilder}.
     * Standard resources are non java/aidl files placed in the java package folders.
     * @param jarBuilder the {@link SignedJarBuilder}.
     * @param sourceFolder the {@link IPath} of the source folder.
     * @param currentFolder The current folder we're recursively processing.
     * @param list a list of files already added to the archive, to detect conflicts.
     * @throws IOException
     */
    private void writeStandardSourceFolderResources(SignedJarBuilder jarBuilder, IPath sourceFolder,
            IFolder currentFolder, ArrayList<String> list) throws IOException {
        try {
            IResource[] members = currentFolder.members();

            for (IResource member : members) {
                int type = member.getType();
                if (type == IResource.FILE && member.exists()) {
                    if (checkFileForPackaging((IFile)member)) {
                        // this files must be added to the archive.
                        IPath fullPath = member.getFullPath();

                        // We need to create its path inside the archive.
                        // This path is relative to the source folder.
                        IPath relativePath = fullPath.removeFirstSegments(
                                sourceFolder.segmentCount());
                        String zipPath = relativePath.toString();

                        // lets check it's not already in the list of path added to the archive
                        if (list.indexOf(zipPath) != -1) {
                            AdtPlugin.printErrorToConsole(getProject(),
                                    String.format(
                                            Messages.ApkBuilder_s_Conflict_with_file_s,
                                            fullPath, zipPath));
                        } else {
                            // get the File object
                            File entryFile = member.getLocation().toFile();

                            AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, getProject(),
                                    String.format(
                                            Messages.ApkBuilder_Packaging_s_into_s,
                                            fullPath, zipPath));

                            // write it in the zip archive
                            jarBuilder.writeFile(entryFile, zipPath);

                            // and add it to the list of entries
                            list.add(zipPath);
                        }
                    }
                } else if (type == IResource.FOLDER) {
                    if (checkFolderForPackaging((IFolder)member)) {
                        writeStandardSourceFolderResources(jarBuilder, sourceFolder,
                                (IFolder)member, list);
                    }
                }
            }
        } catch (CoreException e) {
            // if we can't get the members of the folder, we just don't do anything.
        }
    }

    /**
     * Returns the list of the output folders for the specified {@link IJavaProject} objects, if
     * they are Android projects.
     *
     * @param referencedJavaProjects the java projects.
     * @return an array, always. Can be empty.
     * @throws CoreException
     */
    private String[] getProjectOutputs(IJavaProject[] referencedJavaProjects) throws CoreException {
        ArrayList<String> list = new ArrayList<String>();

        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot wsRoot = ws.getRoot();

        for (IJavaProject javaProject : referencedJavaProjects) {
            // only include output from non android referenced project
            // (This is to handle the case of reference Android projects in the context of
            // instrumentation projects that need to reference the projects to be tested).
            if (javaProject.getProject().hasNature(AndroidConstants.NATURE) == false) {
                // get the output folder
                IPath path = null;
                try {
                    path = javaProject.getOutputLocation();
                } catch (JavaModelException e) {
                    continue;
                }

                IResource outputResource = wsRoot.findMember(path);
                if (outputResource != null && outputResource.getType() == IResource.FOLDER) {
                    String outputOsPath = outputResource.getLocation().toOSString();

                    list.add(outputOsPath);
                }
            }
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns an array of {@link IJavaProject} matching the provided {@link IProject} objects.
     * @param projects the IProject objects.
     * @return an array, always. Can be empty.
     * @throws CoreException
     */
    private IJavaProject[] getJavaProjects(IProject[] projects) throws CoreException {
        ArrayList<IJavaProject> list = new ArrayList<IJavaProject>();

        for (IProject p : projects) {
            if (p.isOpen() && p.hasNature(JavaCore.NATURE_ID)) {

                list.add(JavaCore.create(p));
            }
        }

        return list.toArray(new IJavaProject[list.size()]);
    }

    /**
     * Checks a {@link IFile} to make sure it should be packaged as standard resources.
     * @param file the IFile representing the file.
     * @return true if the file should be packaged as standard java resources.
     */
    static boolean checkFileForPackaging(IFile file) {
        String name = file.getName();

        String ext = file.getFileExtension();
        return JavaResourceFilter.checkFileForPackaging(name, ext);
    }

    /**
     * Checks whether an {@link IFolder} and its content is valid for packaging into the .apk as
     * standard Java resource.
     * @param folder the {@link IFolder} to check.
     */
    static boolean checkFolderForPackaging(IFolder folder) {
        String name = folder.getName();
        return JavaResourceFilter.checkFolderForPackaging(name);
    }

    @Override
    protected void abortOnBadSetup(IJavaProject javaProject) throws CoreException {
        super.abortOnBadSetup(javaProject);

        // for this version, we stop on any marker (ie also markers coming from JDT).
        // The depth is set to ZERO to make sure we don't stop on warning on resources.
        // Only markers set directly on the project are considered.
        IMarker[] markers = javaProject.getProject().findMarkers(null /*type*/,
                false /*includeSubtypes*/, IResource.DEPTH_ZERO);

        if (markers.length > 0) {
            stopBuild("");
        }
    }
}
