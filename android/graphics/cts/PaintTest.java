/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.graphics.cts;

import android.graphics.ColorFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rasterizer;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.test.AndroidTestCase;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import dalvik.annotation.BrokenTest;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(Paint.class)
public class PaintTest extends AndroidTestCase {

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Paint",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Paint",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Paint",
            args = {android.graphics.Paint.class}
        )
    })
    public void testConstructor() {
        new Paint();

        new Paint(1);

        Paint p = new Paint();
        new Paint(p);
    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "breakText",
        args = {char[].class, int.class, int.class, float.class, float[].class}
    )
    @BrokenTest("unknown if hardcoded values being checked are correct")
    public void testBreakText1() {
        Paint p = new Paint();

        char[] chars = {'H', 'I', 'J', 'K', 'L', 'M', 'N'};
        float[] width = {8.0f, 4.0f, 3.0f, 7.0f, 6.0f, 10.0f, 9.0f};
        float[] f = new float[1];

        for (int i = 0; i < chars.length; i++) {
            assertEquals(1, p.breakText(chars, i, 1, 20.0f, f));
            assertEquals(width[i], f[0]);
        }

        // start from 'H'
        int indexH = 0;
        assertEquals(4, p.breakText(chars, indexH, 4, 30.0f, f));
        assertEquals(22.0f, f[0]);
        assertEquals(3, p.breakText(chars, indexH, 3, 30.0f, f));
        assertEquals(15.0f, f[0]);
        assertEquals(2, p.breakText(chars, indexH, 2, 30.0f, f));
        assertEquals(12.0f, f[0]);
        assertEquals(1, p.breakText(chars, indexH, 1, 30.0f, f));
        assertEquals(8.0f, f[0]);
        assertEquals(0, p.breakText(chars, indexH, 0, 30.0f, f));
        assertEquals(0.0f, f[0]);

        assertEquals(1, p.breakText(chars, indexH + 2, 1, 30.0f, f));
        assertEquals(3.0f, f[0]);
        assertEquals(1, p.breakText(chars, indexH + 2, -1, 30.0f, f));
        assertEquals(3.0f, f[0]);

        assertEquals(1, p.breakText(chars, indexH, -1, 30.0f, f));
        assertEquals(8.0f, f[0]);
        assertEquals(2, p.breakText(chars, indexH, -2, 30.0f, f));
        assertEquals(12.0f, f[0]);
        assertEquals(3, p.breakText(chars, indexH, -3, 30.0f, f));
        assertEquals(15.0f, f[0]);
        assertEquals(4, p.breakText(chars, indexH, -4, 30.0f, f));
        assertEquals(22.0f, f[0]);

        assertEquals(7, p.breakText(chars, indexH, 7, 50.0f, f));
        assertEquals(47.0f, f[0]);
        assertEquals(6, p.breakText(chars, indexH, 7, 40.0f, f));
        assertEquals(38.0f, f[0]);

        assertEquals(7, p.breakText(chars, indexH, -7, 50.0f, null));
        assertEquals(7, p.breakText(chars, indexH, 7, 50.0f, null));

        try {
            p.breakText(chars, 0, 8, 60.0f, null);
            fail("Should throw an ArrayIndexOutOfboundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }
        try {
            p.breakText(chars, -1, 7, 50.0f, null);
            fail("Should throw an ArrayIndexOutOfboundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }

    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "breakText",
        args = {java.lang.CharSequence.class, int.class, int.class, boolean.class, float.class,
                float[].class}
    )
    @BrokenTest("unknown if hardcoded values being checked are correct")
    public void testBreakText2() {
        Paint p = new Paint();
        String string = "HIJKLMN";
        float[] width = {8.0f, 4.0f, 3.0f, 7.0f, 6.0f, 10.0f, 9.0f};
        float[] f = new float[1];

        assertEquals(7, p.breakText(string, 0, 7, true, 50.0f, f));
        assertEquals(47.0f, f[0]);
        assertEquals(6, p.breakText(string, 0, 7, true, 40.0f, f));
        assertEquals(38.0f, f[0]);
        assertEquals(7, p.breakText(string, 0, 7, false, 50.0f, f));
        assertEquals(47.0f, f[0]);

        for (int i = 0; i < string.length(); i++) {
            assertEquals(1, p.breakText(string, i, i + 1, true, 20.0f, f));
            assertEquals(width[i], f[0]);
        }

        assertEquals(4, p.breakText(string, 0, 4, true, 30.0f, f));
        assertEquals(22.0f, f[0]);
        assertEquals(3, p.breakText(string, 0, 3, true, 30.0f, f));
        assertEquals(15.0f, f[0]);
        assertEquals(2, p.breakText(string, 0, 2, true, 30.0f, f));
        assertEquals(12.0f, f[0]);
        assertEquals(1, p.breakText(string, 0, 1, true, 30.0f, f));
        assertEquals(8.0f, f[0]);
        assertEquals(0, p.breakText(string, 0, 0, true, 30.0f, f));
        assertEquals(0.0f, f[0]);

        assertEquals(1, p.breakText(string, 2, 3, true, 30.0f, f));
        assertEquals(3.0f, f[0]);
        assertEquals(1, p.breakText(string, 2, 3, false, 30.0f, f));
        assertEquals(3.0f, f[0]);

        assertEquals(1, p.breakText(string, 0, 1, true, 30.0f, f));
        assertEquals(8.0f, f[0]);
        assertEquals(2, p.breakText(string, 0, 2, true, 30.0f, f));
        assertEquals(12.0f, f[0]);
        assertEquals(3, p.breakText(string, 0, 3, true, 30.0f, f));
        assertEquals(15.0f, f[0]);
        assertEquals(4, p.breakText(string, 0, 4, true, 30.0f, f));
        assertEquals(22.0f, f[0]);

        assertEquals(7, p.breakText(string, 0, 7, true, 50.0f, f));
        assertEquals(47.0f, f[0]);
        assertEquals(6, p.breakText(string, 0, 7, true, 40.0f, f));
        assertEquals(38.0f, f[0]);

        assertEquals(7, p.breakText(string, 0, 7, false, 50.0f, null));
        assertEquals(7, p.breakText(string, 0, 7, true, 50.0f, null));

        try {
            p.breakText(string, 0, 8, true, 60.0f, null);
            fail("Should throw an StringIndexOutOfboundsException");
        } catch (StringIndexOutOfBoundsException e) {
            //except here
        }
        try {
            p.breakText(string, -1, 7, true, 50.0f, null);
            fail("Should throw an StringIndexOutOfboundsException");
        } catch (StringIndexOutOfBoundsException e) {
            //except here
        }
        try {
            p.breakText(string, 1, -7, true, 50.0f, null);
            fail("Should throw an StringIndexOutOfboundsException");
        } catch (StringIndexOutOfBoundsException e) {
            //except here
        }
        try {
            p.breakText(string, 7, 1, true, 50.0f, null);
            fail("Should throw an StringIndexOutOfboundsException");
        } catch (StringIndexOutOfBoundsException e) {
            //except here
        }

    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "breakText",
        args = {java.lang.String.class, boolean.class, float.class, float[].class}
    )
    @BrokenTest("unknown if hardcoded values being checked are correct")
    public void testBreakText3() {
        Paint p = new Paint();
        String string = "HIJKLMN";
        float[] width = {8.0f, 4.0f, 3.0f, 7.0f, 6.0f, 10.0f, 9.0f};
        float[] f = new float[1];

        for (int i = 0; i < string.length(); i++) {
            assertEquals(1, p.breakText(string.substring(i, i+1), true, 20.0f, f));
            assertEquals(width[i], f[0]);
            assertEquals(1, p.breakText(string.substring(i, i+1), false, 20.0f, f));
            assertEquals(width[i], f[0]);
        }

        assertEquals(0, p.breakText("", false, 20.0f, f));
        assertEquals(0.0f, f[0]);

        assertEquals(7, p.breakText(string, true, 50.0f, f));
        assertEquals(47.0f, f[0]);
        assertEquals(7, p.breakText(string, false, 50.0f, f));
        assertEquals(47.0f, f[0]);
        assertEquals(6, p.breakText(string, false, 40.0f, f));
        assertEquals(39.0f, f[0]);
        assertEquals(5, p.breakText(string, false, 35.0f, f));
        assertEquals(35.0f, f[0]);
        assertEquals(4, p.breakText(string, false, 33.0f, f));
        assertEquals(32.0f, f[0]);
        assertEquals(3, p.breakText(string, false, 25.0f, f));
        assertEquals(25.0f, f[0]);
        assertEquals(2, p.breakText(string, false, 20.0f, f));
        assertEquals(19.0f, f[0]);
        assertEquals(1, p.breakText(string, false, 13.0f, f));
        assertEquals(9.0f, f[0]);
        assertEquals(0, p.breakText(string, false, 3.0f, f));
        assertEquals(0.0f, f[0]);

        assertEquals(7, p.breakText(string, true, 50.0f, f));
        assertEquals(47.0f, f[0]);
        assertEquals(6, p.breakText(string, true, 40.0f, f));
        assertEquals(38.0f, f[0]);
        assertEquals(5, p.breakText(string, true, 35.0f, f));
        assertEquals(28.0f, f[0]);
        assertEquals(4, p.breakText(string, true, 25.0f, f));
        assertEquals(22.0f, f[0]);
        assertEquals(3, p.breakText(string, true, 20.0f, f));
        assertEquals(15.0f, f[0]);
        assertEquals(2, p.breakText(string, true, 12.0f, f));
        assertEquals(12.0f, f[0]);
        assertEquals(1, p.breakText(string, true, 10.0f, f));
        assertEquals(8.0f, f[0]);
        assertEquals(0, p.breakText(string, true, 3.0f, f));
        assertEquals(0.0f, f[0]);

        assertEquals(7, p.breakText(string, true, 50.0f, null));
        assertEquals(6, p.breakText(string, true, 40.0f, null));
        assertEquals(5, p.breakText(string, true, 35.0f, null));
        assertEquals(4, p.breakText(string, true, 25.0f, null));
        assertEquals(3, p.breakText(string, true, 20.0f, null));
        assertEquals(2, p.breakText(string, true, 12.0f, null));
        assertEquals(1, p.breakText(string, true, 10.0f, null));
        assertEquals(0, p.breakText(string, true, 3.0f, null));
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {android.graphics.Paint.class}
    )
    public void testSet() {
        Paint p  = new Paint();
        Paint p2 = new Paint();
        ColorFilter c = new ColorFilter();
        MaskFilter m  = new MaskFilter();
        PathEffect e  = new PathEffect();
        Rasterizer r  = new Rasterizer();
        Shader s      = new Shader();
        Typeface t    = Typeface.DEFAULT;
        Xfermode x = new Xfermode();

        p.setColorFilter(c);
        p.setMaskFilter(m);
        p.setPathEffect(e);
        p.setRasterizer(r);
        p.setShader(s);
        p.setTypeface(t);
        p.setXfermode(x);
        p2.set(p);
        assertEquals(c, p2.getColorFilter());
        assertEquals(m, p2.getMaskFilter());
        assertEquals(e, p2.getPathEffect());
        assertEquals(r, p2.getRasterizer());
        assertEquals(s, p2.getShader());
        assertEquals(t, p2.getTypeface());
        assertEquals(x, p2.getXfermode());

        p2.set(p2);
        assertEquals(c, p2.getColorFilter());
        assertEquals(m, p2.getMaskFilter());
        assertEquals(e, p2.getPathEffect());
        assertEquals(r, p2.getRasterizer());
        assertEquals(s, p2.getShader());
        assertEquals(t, p2.getTypeface());
        assertEquals(x, p2.getXfermode());

        p.setColorFilter(null);
        p.setMaskFilter(null);
        p.setPathEffect(null);
        p.setRasterizer(null);
        p.setShader(null);
        p.setTypeface(null);
        p.setXfermode(null);
        p2.set(p);
        assertNull(p2.getColorFilter());
        assertNull(p2.getMaskFilter());
        assertNull(p2.getPathEffect());
        assertNull(p2.getRasterizer());
        assertNull(p2.getShader());
        assertNull(p2.getTypeface());
        assertNull(p2.getXfermode());

        p2.set(p2);
        assertNull(p2.getColorFilter());
        assertNull(p2.getMaskFilter());
        assertNull(p2.getPathEffect());
        assertNull(p2.getRasterizer());
        assertNull(p2.getShader());
        assertNull(p2.getTypeface());
        assertNull(p2.getXfermode());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStrokeCap",
            args = {android.graphics.Paint.Cap.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStrokeCap",
            args = {}
        )
    })
    public void testAccessStrokeCap() {
        Paint p = new Paint();

        p.setStrokeCap(Cap.BUTT);
        assertEquals(Cap.BUTT, p.getStrokeCap());

        p.setStrokeCap(Cap.ROUND);
        assertEquals(Cap.ROUND, p.getStrokeCap());

        p.setStrokeCap(Cap.SQUARE);
        assertEquals(Cap.SQUARE, p.getStrokeCap());

        try {
            p.setStrokeCap(null);
            fail("Should throw an Exception");
        } catch (RuntimeException e) {
            //except here
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setXfermode",
            args = {android.graphics.Xfermode.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getXfermode",
            args = {}
        )
    })
    public void testAccessXfermode() {
        Paint p = new Paint();
        Xfermode x = new Xfermode();

        assertEquals(x, p.setXfermode(x));
        assertEquals(x, p.getXfermode());

        assertNull(p.setXfermode(null));
        assertNull(p.getXfermode());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setShader",
            args = {android.graphics.Shader.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getShader",
            args = {}
        )
    })
    public void testAccessShader() {
        Paint p = new Paint();
        Shader s = new Shader();

        assertEquals(s, p.setShader(s));
        assertEquals(s, p.getShader());

        assertNull(p.setShader(null));
        assertNull(p.getShader());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAntiAlias",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isAntiAlias",
            args = {}
        )
    })
    public void testSetAntiAlias() {
        Paint p = new Paint();

        p.setAntiAlias(true);
        assertTrue(p.isAntiAlias());

        p.setAntiAlias(false);
        assertFalse(p.isAntiAlias());

    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTypeface",
            args = {android.graphics.Typeface.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTypeface",
            args = {}
        )
    })

    public void testAccessTypeface() {
        Paint p = new Paint();

        assertEquals(Typeface.DEFAULT, p.setTypeface(Typeface.DEFAULT));
        assertEquals(Typeface.DEFAULT, p.getTypeface());

        assertEquals(Typeface.DEFAULT_BOLD, p.setTypeface(Typeface.DEFAULT_BOLD));
        assertEquals(Typeface.DEFAULT_BOLD, p.getTypeface());

        assertEquals(Typeface.MONOSPACE, p.setTypeface(Typeface.MONOSPACE));
        assertEquals(Typeface.MONOSPACE, p.getTypeface());

        assertNull(p.setTypeface(null));
        assertNull(p.getTypeface());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPathEffect",
            args = {android.graphics.PathEffect.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPathEffect",
            args = {}
        )
    })
    public void testAccessPathEffect() {
        Paint p = new Paint();
        PathEffect e = new PathEffect();

        assertEquals(e, p.setPathEffect(e));
        assertEquals(e, p.getPathEffect());

        assertNull(p.setPathEffect(null));
        assertNull(p.getPathEffect());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFakeBoldText",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFakeBoldText",
            args = {}
        )
    })
    public void testSetFakeBoldText() {
        Paint p = new Paint();

        p.setFakeBoldText(true);
        assertTrue(p.isFakeBoldText());

        p.setFakeBoldText(false);
        assertFalse(p.isFakeBoldText());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStrokeJoin",
            args = {android.graphics.Paint.Join.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStrokeJoin",
            args = {}
        )
    })
    public void testAccessStrokeJoin() {
        Paint p = new Paint();

        p.setStrokeJoin(Join.BEVEL);
        assertEquals(Join.BEVEL, p.getStrokeJoin());

        p.setStrokeJoin(Join.MITER);
        assertEquals(Join.MITER, p.getStrokeJoin());

        p.setStrokeJoin(Join.ROUND);
        assertEquals(Join.ROUND, p.getStrokeJoin());

        try {
            p.setStrokeJoin(null);
            fail("Should throw an Exception");
        } catch (RuntimeException e) {
            //except here
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStyle",
            args = {android.graphics.Paint.Style.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStyle",
            args = {}
        )
    })
    public void testAccessStyle() {
        Paint p = new Paint();

        p.setStyle(Style.FILL);
        assertEquals(Style.FILL, p.getStyle());

        p.setStyle(Style.FILL_AND_STROKE);
        assertEquals(Style.FILL_AND_STROKE, p.getStyle());

        p.setStyle(Style.STROKE);
        assertEquals(Style.STROKE, p.getStyle());

        try {
            p.setStyle(null);
            fail("Should throw an Exception");
        } catch (RuntimeException e) {
            //except here
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFontSpacing",
        args = {}
    )
    public void testGetFontSpacing() {
        Paint p = new Paint();

        assertEquals(13.96875f, p.getFontSpacing());

        p.setTextSize(24.0f);
        assertEquals(27.9375f, p.getFontSpacing());

        p.setTypeface(Typeface.MONOSPACE);
        assertEquals(27.9375f, p.getFontSpacing());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isSubpixelText",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSubpixelText",
            args = {boolean.class}
        )
    })
    public void testSetSubpixelText() {
        Paint p = new Paint();

        p.setSubpixelText(true);
        assertTrue(p.isSubpixelText());

        p.setSubpixelText(false);
        assertFalse(p.isSubpixelText());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTextScaleX",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTextScaleX",
            args = {}
        )
    })
    public void testAccessTextScaleX() {
        Paint p = new Paint();

        p.setTextScaleX(2.0f);
        assertEquals(2.0f, p.getTextScaleX());

        p.setTextScaleX(1.0f);
        assertEquals(1.0f, p.getTextScaleX());

        p.setTextScaleX(0.0f);
        assertEquals(0.0f, p.getTextScaleX());

    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMaskFilter",
            args = {android.graphics.MaskFilter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMaskFilter",
            args = {}
        )
    })
    public void testAccessMaskFilter() {
        Paint p = new Paint();
        MaskFilter m = new MaskFilter();

        assertEquals(m, p.setMaskFilter(m));
        assertEquals(m, p.getMaskFilter());

        assertNull(p.setMaskFilter(null));
        assertNull(p.getMaskFilter());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setColorFilter",
            args = {android.graphics.ColorFilter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getColorFilter",
            args = {}
        )
    })
    public void testAccessColorFilter() {
        Paint p = new Paint();
        ColorFilter c = new ColorFilter();

        assertEquals(c, p.setColorFilter(c));
        assertEquals(c, p.getColorFilter());

        assertNull(p.setColorFilter(null));
        assertNull(p.getColorFilter());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRasterizer",
            args = {android.graphics.Rasterizer.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRasterizer",
            args = {}
        )
    })
    public void testAccessRasterizer() {
        Paint p = new Paint();
        Rasterizer r = new Rasterizer();

        assertEquals(r, p.setRasterizer(r));
        assertEquals(r, p.getRasterizer());

        assertNull(p.setRasterizer(null));
        assertNull(p.getRasterizer());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setARGB",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testSetARGB() {
        Paint p = new Paint();

        p.setARGB(0, 0, 0, 0);
        assertEquals(0, p.getColor());

        p.setARGB(3, 3, 3, 3);
        assertEquals((3 << 24) | (3 << 16) | (3 << 8) | 3, p.getColor());

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ascent",
        args = {}
    )
    public void testAscent() {
        Paint p = new Paint();

        assertEquals(-11.138672f, p.ascent());

        p.setTextSize(10.0f);
        assertEquals(-9.282227f, p.ascent());

        p.setTypeface(Typeface.DEFAULT_BOLD);
        assertEquals(-9.282227f, p.ascent());

        p.setTextSize(20.0f);
        assertEquals(-18.564453f, p.ascent());

        p.setTypeface(Typeface.DEFAULT_BOLD);
        assertEquals(-18.564453f, p.ascent());

        p.setTypeface(Typeface.MONOSPACE);
        assertEquals(-18.564453f, p.ascent());

        p.setTypeface(Typeface.SANS_SERIF);
        assertEquals(-18.564453f, p.ascent());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTextSkewX",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTextSkewX",
            args = {}
        )
    })
    public void testAccessTextSkewX() {
        Paint p = new Paint();

        p.setTextSkewX(1.0f);
        assertEquals(1.0f, p.getTextSkewX());

        p.setTextSkewX(0.0f);
        assertEquals(0.0f, p.getTextSkewX());

        p.setTextSkewX(-0.25f);
        assertEquals(-0.25f, p.getTextSkewX());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTextSize",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTextSize",
            args = {}
        )
    })
    public void testAccessTextSize() {
        Paint p = new Paint();

        p.setTextSize(1.0f);
        assertEquals(1.0f, p.getTextSize());

        p.setTextSize(2.0f);
        assertEquals(2.0f, p.getTextSize());

       // text size should be greater than 0, so set 0 has no effect
       p.setTextSize(0.0f);
       assertEquals(2.0f, p.getTextSize());

       // text size should be greater than 0, so set -1 has no effect
       p.setTextSize(-1.0f);
       assertEquals(2.0f, p.getTextSize());

    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "getTextWidths",
        args = {char[].class, int.class, int.class, float[].class}
    )
    @BrokenTest("unknown if hardcoded values being checked are correct")
    public void testGetTextWidths1() throws Exception {
        Paint p = new Paint();
        char[] chars = {'H', 'I', 'J', 'K', 'L', 'M', 'N'};
        float[] width = {8.0f, 4.0f, 3.0f, 7.0f, 6.0f, 10.0f, 9.0f};
        float[] f = new float[7];

        assertEquals(7, p.getTextWidths(chars, 0, 7, f));
        for (int i = 0; i < chars.length; i++) {
            assertEquals(width[i], f[i]);
        }

        assertEquals(4, p.getTextWidths(chars, 3, 4, f));
        for (int i = 3; i < chars.length; i++) {
            assertEquals(width[i], f[i - 3]);
        }

        assertEquals(1, p.getTextWidths(chars, 6, 1, f));
        assertEquals(width[6], f[0]);
        assertEquals(0, p.getTextWidths(chars, 6, 0, f));

        try {
            p.getTextWidths(chars, -1, 6, f);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(chars, 0, -1, f);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(chars, 1, 8, f);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }

        float[] f2 = new float[3];
        try {
            p.getTextWidths(chars, 0, 6, f2);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }
    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "getTextWidths",
        args = {java.lang.CharSequence.class, int.class, int.class, float[].class}
    )
    @BrokenTest("unknown if hardcoded values being checked are correct")
    public void testGetTextWidths2() throws Exception {
        Paint p = new Paint();

        // CharSequence of String
        String string = "HIJKLMN";
        float[] width = {8.0f, 4.0f, 3.0f, 7.0f, 6.0f, 10.0f, 9.0f};
        float[] f = new float[7];

        assertEquals(7, p.getTextWidths((CharSequence) string, 0, 7, f));
        for (int i = 0; i < string.length(); i++) {
            assertEquals(width[i], f[i]);
        }

        assertEquals(4, p.getTextWidths((CharSequence) string, 3, 7, f));
        for (int i = 3; i < string.length(); i++) {
            assertEquals(width[i], f[i - 3]);
        }

        assertEquals(1, p.getTextWidths((CharSequence) string, 6, 7, f));
        assertEquals(width[6], f[0]);
        assertEquals(0, p.getTextWidths((CharSequence) string, 7, 7, f));

        try {
            p.getTextWidths((CharSequence) string, -1, 6, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }
        try {
            p.getTextWidths((CharSequence) string, 0, -1, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths((CharSequence) string, 4, 3, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths((CharSequence) string, 1, 8, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        float[] f2 = new float[3];
        try {
            p.getTextWidths((CharSequence) string, 0, 6, f2);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }
        // CharSequence of SpannedString
        SpannedString spannedString = new SpannedString("HIJKLMN");

        assertEquals(7, p.getTextWidths(spannedString, 0, 7, f));
        for (int i = 0; i < spannedString.length(); i++) {
            assertEquals(width[i], f[i]);
        }

        assertEquals(4, p.getTextWidths(spannedString, 3, 7, f));
        for (int i = 3; i < spannedString.length(); i++) {
            assertEquals(width[i], f[i - 3]);
        }

        assertEquals(1, p.getTextWidths(spannedString, 6, 7, f));
        assertEquals(width[6], f[0]);
        assertEquals(0, p.getTextWidths(spannedString, 7, 7, f));

        try {
            p.getTextWidths(spannedString, -1, 6, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannedString, 0, -1, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannedString, 4, 3, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannedString, 1, 8, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannedString, 0, 6, f2);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }

        // CharSequence of SpannableString
        SpannableString spannableString = new SpannableString("HIJKLMN");

        assertEquals(7, p.getTextWidths(spannableString, 0, 7, f));
        for (int i = 0; i < spannableString.length(); i++) {
            assertEquals(width[i], f[i]);
        }

        assertEquals(4, p.getTextWidths(spannableString, 3, 7, f));
        for (int i = 3; i < spannableString.length(); i++) {
            assertEquals(width[i], f[i - 3]);
        }

        assertEquals(1, p.getTextWidths(spannableString, 6, 7, f));
        assertEquals(width[6], f[0]);
        assertEquals(0, p.getTextWidths(spannableString, 7, 7, f));

        try {
            p.getTextWidths(spannableString, -1, 6, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannableString, 0, -1, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannableString, 4, 3, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannableString, 1, 8, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannableString, 0, 6, f2);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }

        // CharSequence of SpannableStringBuilder (GraphicsOperations)
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("HIJKLMN");

        assertEquals(7, p.getTextWidths(spannableStringBuilder, 0, 7, f));
        for (int i = 0; i < spannableStringBuilder.length(); i++) {
            assertEquals(width[i], f[i]);
        }

        assertEquals(4, p.getTextWidths(spannableStringBuilder, 3, 7, f));
        for (int i = 3; i < spannableStringBuilder.length(); i++) {
            assertEquals(width[i], f[i - 3]);
        }

        assertEquals(1, p.getTextWidths(spannableStringBuilder, 6, 7, f));
        assertEquals(width[6], f[0]);
        assertEquals(0, p.getTextWidths(spannableStringBuilder, 7, 7, f));

        try {
            p.getTextWidths(spannableStringBuilder, -1, 6, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannableStringBuilder, 0, -1, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannableStringBuilder, 4, 3, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannableStringBuilder, 1, 8, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(spannableStringBuilder, 0, 6, f2);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }

        // CharSequence of StringBuilder
        StringBuilder stringBuilder = new StringBuilder("HIJKLMN");

        assertEquals(7, p.getTextWidths(stringBuilder, 0, 7, f));
        for (int i = 0; i < stringBuilder.length(); i++) {
            assertEquals(width[i], f[i]);
        }

        assertEquals(4, p.getTextWidths(stringBuilder, 3, 7, f));
        for (int i = 3; i < stringBuilder.length(); i++) {
            assertEquals(width[i], f[i - 3]);
        }

        assertEquals(1, p.getTextWidths(stringBuilder, 6, 7, f));
        assertEquals(width[6], f[0]);
        assertEquals(0, p.getTextWidths(stringBuilder, 7, 7, f));

        try {
            p.getTextWidths(stringBuilder, -1, 6, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(stringBuilder, 0, -1, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(stringBuilder, 4, 3, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(stringBuilder, 1, 8, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(stringBuilder, 0, 6, f2);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }

    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "getTextWidths",
        args = {java.lang.String.class, int.class, int.class, float[].class}
    )
    @BrokenTest("unknown if hardcoded values being checked are correct")
    public void testGetTextWidths3() {
        Paint p = new Paint();
        String string = "HIJKLMN";
        float[] width = {8.0f, 4.0f, 3.0f, 7.0f, 6.0f, 10.0f, 9.0f};
        float[] f = new float[7];

        assertEquals(7, p.getTextWidths(string, 0, 7, f));
        for (int i = 0; i < string.length(); i++) {
            assertEquals(width[i], f[i]);
        }

        assertEquals(4, p.getTextWidths(string, 3, 7, f));
        for (int i = 3; i < string.length(); i++) {
            assertEquals(width[i], f[i - 3]);
        }

        assertEquals(1, p.getTextWidths(string, 6, 7, f));
        assertEquals(width[6], f[0]);
        assertEquals(0, p.getTextWidths(string, 7, 7, f));

        try {
            p.getTextWidths(string, -1, 6, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(string, 0, -1, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(string, 4, 3, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextWidths(string, 1, 8, f);
            fail("Should throw an IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            //except here
        }
        float[] f2 = new float[3];
        try {
            p.getTextWidths(string, 0, 6, f2);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }
    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "getTextWidths",
        args = {java.lang.String.class, float[].class}
    )
    @BrokenTest("unknown if hardcoded values being checked are correct")
    public void testGetTextWidths4() throws Exception {
        Paint p = new Paint();
        String string = "HIJKLMN";
        float[] width = {8.0f, 4.0f, 3.0f, 7.0f, 6.0f, 10.0f, 9.0f};
        float[] f = new float[7];

        assertEquals(7, p.getTextWidths(string, f));
        for (int i = 0; i < string.length(); i++) {
            assertEquals(width[i], f[i]);
        }

        assertEquals(0, p.getTextWidths("", f));

        try {
            p.getTextWidths(null, f);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        float[] f2 = new float[3];
        try {
            p.getTextWidths(string, f2);
            fail("Should throw an ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            //except here
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isStrikeThruText",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStrikeThruText",
            args = {boolean.class}
        )
    })
    public void testSetStrikeThruText() {
        Paint p = new Paint();

        p.setStrikeThruText(true);
        assertTrue(p.isStrikeThruText());

        p.setStrikeThruText(false);
        assertFalse(p.isStrikeThruText());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTextAlign",
            args = {android.graphics.Paint.Align.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTextAlign",
            args = {}
        )
    })
    public void testAccessTextAlign() {
        Paint p = new Paint();

        p.setTextAlign(Align.CENTER);
        assertEquals(Align.CENTER, p.getTextAlign());

        p.setTextAlign(Align.LEFT);
        assertEquals(Align.LEFT, p.getTextAlign());

        p.setTextAlign(Align.RIGHT);
        assertEquals(Align.RIGHT, p.getTextAlign());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFillPath",
        args = {android.graphics.Path.class, android.graphics.Path.class}
    )
    public void testGetFillPath() {
        Paint p = new Paint();
        Path path1 = new Path();
        Path path2 = new Path();

        assertTrue(path1.isEmpty());
        assertTrue(path2.isEmpty());
        p.getFillPath(path1, path2);
        assertTrue(path1.isEmpty());
        assertTrue(path2.isEmpty());

        // No setter

    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAlpha",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getAlpha",
            args = {}
        )
    })
    public void testAccessAlpha() {
        Paint p = new Paint();

        p.setAlpha(0);
        assertEquals(0, p.getAlpha());

        p.setAlpha(255);
        assertEquals(255, p.getAlpha());

        // set value should between 0 and 255, so 266 is rounded to 10
        p.setAlpha(266);
        assertEquals(10, p.getAlpha());

        // set value should between 0 and 255, so -20 is rounded to 236
        p.setAlpha(-20);
        assertEquals(236, p.getAlpha());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFilterBitmap",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFilterBitmap",
            args = {boolean.class}
        )
    })
    public void testSetFilterBitmap() {
        Paint p = new Paint();

        p.setFilterBitmap(true);
        assertTrue(p.isFilterBitmap());

        p.setFilterBitmap(false);
        assertFalse(p.isFilterBitmap());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setColor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getColor",
            args = {}
        )
    })
    public void testAccessColor() {
        Paint p = new Paint();

        p.setColor(1);
        assertEquals(1, p.getColor());

        p.setColor(0);
        assertEquals(0, p.getColor());

        p.setColor(255);
        assertEquals(255, p.getColor());

        p.setColor(-1);
        assertEquals(-1, p.getColor());

        p.setColor(256);
        assertEquals(256, p.getColor());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTextBounds",
        args = {java.lang.String.class, int.class, int.class, android.graphics.Rect.class}
    )
    @BrokenTest("Test result will be different when run in batch mode")
    public void testGetTextBounds1() throws Exception {
        Paint p = new Paint();
        Rect r = new Rect();
        String s = "HIJKLMN";

        try {
            p.getTextBounds(s, -1, 2, r);
        } catch (IndexOutOfBoundsException e) {
        } catch (RuntimeException e) {
            fail("Should not throw a RuntimeException");
        }

        try {
            p.getTextBounds(s, 0, -2, r);
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextBounds(s, 4, 3, r);
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextBounds(s, 0, 8, r);
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextBounds(s, 0, 2, null);
        } catch (NullPointerException e) {
            //except here
        }

        p.getTextBounds(s, 0, 0, r);
        assertEquals(0, r.bottom);
        assertEquals(-1, r.left);
        assertEquals(0, r.right);
        assertEquals(-1, r.top);

        p.getTextBounds(s, 0, 1, r);
        assertEquals(0, r.bottom);
        assertEquals(1, r.left);
        assertEquals(8, r.right);
        assertEquals(-9, r.top);

        p.getTextBounds(s, 1, 2, r);
        assertEquals(0, r.bottom);
        assertEquals(0, r.left);
        assertEquals(4, r.right);
        assertEquals(-9, r.top);

        p.getTextBounds(s, 0, 6, r);
        assertEquals(3, r.bottom);
        assertEquals(1, r.left);
        assertEquals(38, r.right);
        assertEquals(-9, r.top);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTextBounds",
        args = {char[].class, int.class, int.class, android.graphics.Rect.class}
    )
    @BrokenTest("Test result will be different when run in batch mode")
    public void testGetTextBounds2() throws Exception {
        Paint p = new Paint();
        Rect r = new Rect();
        char[] chars = {'H', 'I', 'J', 'K', 'L', 'M', 'N'};

        try {
            p.getTextBounds(chars, -1, 2, r);
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextBounds(chars, 0, -2, r);
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextBounds(chars, 4, 3, r);
        } catch (IndexOutOfBoundsException e) {
            //except here
        }

        try {
            p.getTextBounds(chars, 0, 8, r);
        } catch (IndexOutOfBoundsException e) {
            //except here
        }
        try {
            p.getTextBounds(chars, 0, 2, null);
        } catch (NullPointerException e) {
            //except here
        }

        p.getTextBounds(chars, 0, 0, r);
        assertEquals(0, r.bottom);
        assertEquals(-1, r.left);
        assertEquals(0, r.right);
        assertEquals(0, r.top);

        p.getTextBounds(chars, 0, 1, r);
        assertEquals(0, r.bottom);
        assertEquals(1, r.left);
        assertEquals(8, r.right);
        assertEquals(-9, r.top);

        p.getTextBounds(chars, 1, 2, r);
        assertEquals(3, r.bottom);
        assertEquals(0, r.left);
        assertEquals(7, r.right);
        assertEquals(-9, r.top);

        p.getTextBounds(chars, 0, 6, r);
        assertEquals(3, r.bottom);
        assertEquals(1, r.left);
        assertEquals(38, r.right);
        assertEquals(-9, r.top);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setShadowLayer",
        args = {float.class, float.class, float.class, int.class}
    )
    public void testSetShadowLayer() {
        new Paint().setShadowLayer(10, 1, 1, 0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFontMetrics",
        args = {android.graphics.Paint.FontMetrics.class}
    )
    public void testGetFontMetrics1() {
        Paint p = new Paint();
        Paint.FontMetrics fm = new Paint.FontMetrics();

        assertEquals(13.96875f, p.getFontMetrics(fm));
        assertEquals(-11.138672f, fm.ascent);
        assertEquals(3.2519531f, fm.bottom);
        assertEquals(2.8300781f, fm.descent);
        assertEquals(0.0f, fm.leading);
        assertEquals(-12.574219f, fm.top);

        assertEquals(13.96875f, p.getFontMetrics(null));

        p.setTextSize(24.0f);

        assertEquals(27.9375f, p.getFontMetrics(fm));
        assertEquals(-22.277344f, fm.ascent);
        assertEquals(6.5039062f, fm.bottom);
        assertEquals(5.6601562f, fm.descent);
        assertEquals(0.0f, fm.leading);
        assertEquals(-25.148438f, fm.top);

        assertEquals(27.9375f, p.getFontMetrics(null));

        p.setTypeface(Typeface.MONOSPACE);
        assertEquals(27.9375f, p.getFontMetrics(fm));
        assertEquals(-22.277344f, fm.ascent);
        assertEquals(6.5039062f, fm.bottom);
        assertEquals(5.6601562f, fm.descent);
        assertEquals(0.0f, fm.leading);
        assertEquals(-25.347656f, fm.top);

        assertEquals(27.9375f, p.getFontMetrics(null));

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFontMetrics",
        args = {}
    )
    public void testGetFontMetrics2() {
        Paint p = new Paint();
        Paint.FontMetrics fm;

        fm = p.getFontMetrics();
        assertEquals(-11.138672f, fm.ascent);
        assertEquals(3.2519531f, fm.bottom);
        assertEquals(2.8300781f, fm.descent);
        assertEquals(0.0f, fm.leading);
        assertEquals(-12.574219f, fm.top);

        p.setTextSize(24.0f);

        fm = p.getFontMetrics();
        assertEquals(-22.277344f, fm.ascent);
        assertEquals(6.5039062f, fm.bottom);
                assertEquals(5.6601562f, fm.descent);
        assertEquals(0.0f, fm.leading);
        assertEquals(-25.148438f, fm.top);

        p.setTypeface(Typeface.MONOSPACE);

        fm = p.getFontMetrics();
        assertEquals(-22.277344f, fm.ascent);
        assertEquals(6.5039062f, fm.bottom);
        assertEquals(5.6601562f, fm.descent);
        assertEquals(0.0f, fm.leading);
        assertEquals(-25.347656f, fm.top);

    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStrokeMiter",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStrokeMiter",
            args = {}
        )
    })
    public void testAccessStrokeMiter() {
        Paint p = new Paint();

        p.setStrokeMiter(0.0f);
        assertEquals(0.0f, p.getStrokeMiter());

        p.setStrokeMiter(10.0f);
        assertEquals(10.0f, p.getStrokeMiter());

        // set value should be greater or equal to 0, set to -10.0f has no effect
        p.setStrokeMiter(-10.0f);
        assertEquals(10.0f, p.getStrokeMiter());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clearShadowLayer",
        args = {}
    )
    public void testClearShadowLayer() {
        new Paint().clearShadowLayer();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setUnderlineText",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isUnderlineText",
            args = {}
        )
    })
    public void testSetUnderlineText() {
        Paint p = new Paint();

        p.setUnderlineText(true);
        assertTrue(p.isUnderlineText());

        p.setUnderlineText(false);
        assertFalse(p.isUnderlineText());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDither",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isDither",
            args = {}
        )
    })
    public void testSetDither() {
        Paint p = new Paint();

        p.setDither(true);
        assertTrue(p.isDither());

        p.setDither(false);
        assertFalse(p.isDither());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "descent",
        args = {}
    )
    public void testDescent() {
        Paint p = new Paint();

        assertEquals(2.8300781f, p.descent());

        p.setTextSize(10.0f);
        assertEquals(2.3583984f, p.descent());

        p.setTypeface(Typeface.DEFAULT_BOLD);
        assertEquals(2.3583984f, p.descent());

        p.setTextSize(20.0f);
        assertEquals(4.716797f, p.descent());

        p.setTypeface(Typeface.DEFAULT_BOLD);
        assertEquals(4.716797f, p.descent());

        p.setTypeface(Typeface.MONOSPACE);
        assertEquals(4.716797f, p.descent());

        p.setTypeface(Typeface.SANS_SERIF);
        assertEquals(4.716797f, p.descent());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFlags",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFlags",
            args = {}
        )
    })
    public void testAccessFlags() {
        Paint p = new Paint();

        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        assertEquals(Paint.ANTI_ALIAS_FLAG, p.getFlags());

        p.setFlags(Paint.DEV_KERN_TEXT_FLAG);
        assertEquals(Paint.DEV_KERN_TEXT_FLAG, p.getFlags());
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStrokeWidth",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStrokeWidth",
            args = {}
        )
    })
    public void testAccessStrokeWidth() {
        Paint p = new Paint();

        p.setStrokeWidth(0.0f);
        assertEquals(0.0f, p.getStrokeWidth());

        p.setStrokeWidth(10.0f);
        assertEquals(10.0f, p.getStrokeWidth());

        // set value must greater or equal to 0, set -10.0f has no effect
        p.setStrokeWidth(-10.0f);
        assertEquals(10.0f, p.getStrokeWidth());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "reset",
        args = {}
    )
    public void testReset() {

        Paint p  = new Paint();
        ColorFilter c = new ColorFilter();
        MaskFilter m  = new MaskFilter();
        PathEffect e  = new PathEffect();
        Rasterizer r  = new Rasterizer();
        Shader s      = new Shader();
        Typeface t    = Typeface.DEFAULT;
        Xfermode x = new Xfermode();

        p.setColorFilter(c);
        p.setMaskFilter(m);
        p.setPathEffect(e);
        p.setRasterizer(r);
        p.setShader(s);
        p.setTypeface(t);
        p.setXfermode(x);
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        assertEquals(c, p.getColorFilter());
        assertEquals(m, p.getMaskFilter());
        assertEquals(e, p.getPathEffect());
        assertEquals(r, p.getRasterizer());
        assertEquals(s, p.getShader());
        assertEquals(t, p.getTypeface());
        assertEquals(x, p.getXfermode());
        assertEquals(Paint.ANTI_ALIAS_FLAG, p.getFlags());

        p.reset();
        assertEquals(Paint.DEV_KERN_TEXT_FLAG, p.getFlags());
        assertEquals(c, p.getColorFilter());
        assertEquals(m, p.getMaskFilter());
        assertEquals(e, p.getPathEffect());
        assertEquals(r, p.getRasterizer());
        assertEquals(s, p.getShader());
        assertEquals(t, p.getTypeface());
        assertEquals(x, p.getXfermode());

    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isLinearText",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setLinearText",
            args = {boolean.class}
        )
    })
    public void testSetLinearText() {
        Paint p = new Paint();

        p.setLinearText(true);
        assertTrue(p.isLinearText());

        p.setLinearText(false);
        assertFalse(p.isLinearText());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFontMetricsInt",
        args = {android.graphics.Paint.FontMetricsInt.class}
    )
    public void testGetFontMetricsInt1() {
        Paint p = new Paint();
        Paint.FontMetricsInt fmi = new Paint.FontMetricsInt();

        assertEquals(14, p.getFontMetricsInt(fmi));
        assertEquals(-11, fmi.ascent);
        assertEquals(4, fmi.bottom);
        assertEquals(3, fmi.descent);
        assertEquals(0, fmi.leading);
        assertEquals(-13, fmi.top);

        assertEquals(14, p.getFontMetricsInt(null));

        p.setTextSize(24);

        assertEquals(28, p.getFontMetricsInt(fmi));
        assertEquals(-22, fmi.ascent);
        assertEquals(7, fmi.bottom);
        assertEquals(6, fmi.descent);
        assertEquals(0, fmi.leading);
        assertEquals(-26, fmi.top);

        assertEquals(28, p.getFontMetricsInt(null));

        p.setTypeface(Typeface.MONOSPACE);
        assertEquals(28, p.getFontMetricsInt(fmi));
        assertEquals(-22, fmi.ascent);
        assertEquals(7, fmi.bottom);
        assertEquals(6, fmi.descent);
        assertEquals(0, fmi.leading);
        assertEquals(-26, fmi.top);

        assertEquals(28, p.getFontMetricsInt(null));

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFontMetricsInt",
        args = {}
    )
    public void testGetFontMetricsInt2() {
        Paint p = new Paint();
        Paint.FontMetricsInt fmi;

        fmi = p.getFontMetricsInt();
        assertEquals(-11, fmi.ascent);
        assertEquals(4, fmi.bottom);
        assertEquals(3, fmi.descent);
        assertEquals(0, fmi.leading);
        assertEquals(-13, fmi.top);

        p.setTextSize(24);

        fmi = p.getFontMetricsInt();
        assertEquals(-22, fmi.ascent);
        assertEquals(7, fmi.bottom);
        assertEquals(6, fmi.descent);
        assertEquals(0, fmi.leading);
        assertEquals(-26, fmi.top);

        p.setTypeface(Typeface.MONOSPACE);
        fmi = p.getFontMetricsInt();
        assertEquals(-22, fmi.ascent);
        assertEquals(7, fmi.bottom);
        assertEquals(6, fmi.descent);
        assertEquals(0, fmi.leading);
        assertEquals(-26, fmi.top);
    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "measureText",
        args = {char[].class, int.class, int.class}
    )
    @BrokenTest("unknown if hardcoded values being checked are correct")
    public void testMeasureText1() {
        Paint p = new Paint();

        // The default text size
        assertEquals(12.0f, p.getTextSize());

        char[] c = {};
        char[] c2 = {'H'};
        char[] c3 = {'H', 'I', 'J', 'H', 'I', 'J'};
        assertEquals(0.0f, p.measureText(c, 0, 0));
        assertEquals(8.0f, p.measureText(c2, 0, 1));
        assertEquals(8.0f, p.measureText(c3, 0, 1));
        assertEquals(15.0f, p.measureText(c3, 0, 3));
        assertEquals(15.0f, p.measureText(c3, 3, 3));
        assertEquals(30.0f, p.measureText(c3, 0, 6));

        p.setTextSize(24.0f);

        assertEquals(17.0f, p.measureText(c2, 0, 1));

        p.setTextSize(12.0f);
        p.setTypeface(Typeface.MONOSPACE);

        assertEquals(7.0f, p.measureText(c2, 0, 1));

        try {
            p.measureText(c3, -1, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(c3, 4, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(c3, 0, 9);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText((char[]) null, 0, 0);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "measureText",
        args = {java.lang.String.class, int.class, int.class}
    )
    @BrokenTest("unknown if hardcoded values being checked are correct")
    public void testMeasureText2() {
        Paint p = new Paint();
        String string = "HIJHIJ";

        // The default text size
        assertEquals(12.0f, p.getTextSize());

        assertEquals(0.0f, p.measureText("", 0, 0));
        assertEquals(8.0f, p.measureText("H", 0, 1));
        assertEquals(4.0f, p.measureText("I", 0, 1));
        assertEquals(3.0f, p.measureText("J", 0, 1));
        assertEquals(8.0f, p.measureText(string, 0, 1));
        assertEquals(15.0f, p.measureText(string, 0, 3));
        assertEquals(15.0f, p.measureText(string, 3, 6));
        assertEquals(30.0f, p.measureText(string, 0, 6));

        p.setTextSize(24.0f);

        assertEquals(17.0f, p.measureText("H", 0, 1));
        assertEquals(8.0f, p.measureText("I", 0, 1));
        assertEquals(7.0f, p.measureText("J", 0, 1));

        p.setTextSize(12.0f);
        p.setTypeface(Typeface.MONOSPACE);

        assertEquals(7.0f, p.measureText("H", 0, 1));
        assertEquals(7.0f, p.measureText("I", 0, 1));
        assertEquals(7.0f, p.measureText("J", 0, 1));

        try {
            p.measureText(string, -1, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(string, 4, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(string, 0, 9);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText((String) null, 0, 0);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "measureText",
        args = {java.lang.String.class}
    )
    @BrokenTest("unknown if hardcoded values being checked are correct")
    public void testMeasureText3() {
        Paint p = new Paint();

        // The default text size
        p.setTextSize(12.0f);
        assertEquals(12.0f, p.getTextSize());

        assertEquals(0.0f, p.measureText(""));
        assertEquals(8.0f, p.measureText("H"));
        assertEquals(4.0f, p.measureText("I"));
        assertEquals(3.0f, p.measureText("J"));
        assertEquals(7.0f, p.measureText("K"));
        assertEquals(6.0f, p.measureText("L"));
        assertEquals(10.0f, p.measureText("M"));
        assertEquals(9.0f, p.measureText("N"));
        assertEquals(12.0f, p.measureText("HI"));
        p.setTextSize(24.0f);

        assertEquals(17.0f, p.measureText("H"));
        assertEquals(8.0f, p.measureText("I"));
        assertEquals(7.0f, p.measureText("J"));
        assertEquals(14.0f, p.measureText("K"));
        assertEquals(12.0f, p.measureText("L"));
        assertEquals(21.0f, p.measureText("M"));
        assertEquals(18.0f, p.measureText("N"));
        assertEquals(25.0f, p.measureText("HI"));

        p.setTextSize(12.0f);
        p.setTypeface(Typeface.MONOSPACE);

        assertEquals(7.0f, p.measureText("H"));
        assertEquals(7.0f, p.measureText("I"));
        assertEquals(7.0f, p.measureText("J"));
        assertEquals(7.0f, p.measureText("K"));
        assertEquals(7.0f, p.measureText("L"));
        assertEquals(7.0f, p.measureText("M"));
        assertEquals(7.0f, p.measureText("N"));
        assertEquals(14.0f, p.measureText("HI"));

        try {
            p.measureText(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
    }

    @TestTargetNew(
        level = TestLevel.TODO,
        method = "measureText",
        args = {java.lang.CharSequence.class, int.class, int.class}
    )
    @BrokenTest("unknown if hardcoded values being tested are correct")
    public void testMeasureText4() {

        Paint p = new Paint();
        // CharSequence of String
        String string = "HIJHIJ";
        // The default text size
        p.setTextSize(12.0f);
        assertEquals(12.0f, p.getTextSize());

        assertEquals(8.0f, p.measureText((CharSequence) string, 0, 1));
        assertEquals(15.0f, p.measureText((CharSequence) string, 0, 3));
        assertEquals(15.0f, p.measureText((CharSequence) string, 3, 6));
        assertEquals(30.0f, p.measureText((CharSequence) string, 0, 6));

        p.setTextSize(24.0f);

        assertEquals(17.0f, p.measureText((CharSequence) string, 0, 1));
        assertEquals(32.0f, p.measureText((CharSequence) string, 0, 3));
        assertEquals(32.0f, p.measureText((CharSequence) string, 3, 6));
        assertEquals(64.0f, p.measureText((CharSequence) string, 0, 6));

        p.setTextSize(12.0f);
        p.setTypeface(Typeface.MONOSPACE);

        assertEquals(7.0f, p.measureText((CharSequence) string, 0, 1));
        assertEquals(21.0f, p.measureText((CharSequence) string, 0, 3));
        assertEquals(21.0f, p.measureText((CharSequence) string, 3, 6));
        assertEquals(42.0f, p.measureText((CharSequence) string, 0, 6));

        try {
            p.measureText((CharSequence) "HIJHIJ", -1, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText((CharSequence) "HIJHIJ", 4, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText((CharSequence) "HIJHIJ", 0, 9);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText((CharSequence) null, 0, 0);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        // CharSequence of SpannedString
        SpannedString spannedString = new SpannedString("HIJHIJ");
        // The default text size and typeface
        p.setTextSize(12.0f);
        p.setTypeface(Typeface.DEFAULT);

        assertEquals(8.0f, p.measureText(spannedString, 0, 1));
        assertEquals(15.0f, p.measureText(spannedString, 0, 3));
        assertEquals(15.0f, p.measureText(spannedString, 3, 6));
        assertEquals(30.0f, p.measureText(spannedString, 0, 6));

        p.setTextSize(24.0f);

        assertEquals(17.0f, p.measureText(spannedString, 0, 1));
        assertEquals(32.0f, p.measureText(spannedString, 0, 3));
        assertEquals(32.0f, p.measureText(spannedString, 3, 6));
        assertEquals(64.0f, p.measureText(spannedString, 0, 6));

        p.setTextSize(12.0f);
        p.setTypeface(Typeface.MONOSPACE);

        assertEquals(7.0f, p.measureText(spannedString, 0, 1));
        assertEquals(21.0f, p.measureText(spannedString, 0, 3));
        assertEquals(21.0f, p.measureText(spannedString, 3, 6));
        assertEquals(42.0f, p.measureText(spannedString, 0, 6));

        try {
            p.measureText(spannedString, -1, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(spannedString, 4, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(spannedString, 0, 9);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText((SpannedString) null, 0, 0);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        // CharSequence of SpannableString
        SpannableString spannableString = new SpannableString("HIJHIJ");
        // The default text size and typeface
        p.setTextSize(12.0f);
        p.setTypeface(Typeface.DEFAULT);

        assertEquals(8.0f, p.measureText(spannableString, 0, 1));
        assertEquals(15.0f, p.measureText(spannableString, 0, 3));
        assertEquals(15.0f, p.measureText(spannableString, 3, 6));
        assertEquals(30.0f, p.measureText(spannableString, 0, 6));

        p.setTextSize(24.0f);

        assertEquals(17.0f, p.measureText(spannableString, 0, 1));
        assertEquals(32.0f, p.measureText(spannableString, 0, 3));
        assertEquals(32.0f, p.measureText(spannableString, 3, 6));
        assertEquals(64.0f, p.measureText(spannableString, 0, 6));

        p.setTextSize(12.0f);
        p.setTypeface(Typeface.MONOSPACE);

        assertEquals(7.0f, p.measureText(spannableString, 0, 1));
        assertEquals(21.0f, p.measureText(spannableString, 0, 3));
        assertEquals(21.0f, p.measureText(spannableString, 3, 6));
        assertEquals(42.0f, p.measureText(spannableString, 0, 6));

        try {
            p.measureText(spannableString, -1, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(spannableString, 4, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(spannableString, 0, 9);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText((SpannableString) null, 0, 0);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        // CharSequence of SpannableStringBuilder (GraphicsOperations)
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("HIJHIJ");
        // The default text size
        p.setTextSize(12.0f);
        p.setTypeface(Typeface.DEFAULT);

        assertEquals(8.0f, p.measureText(spannableStringBuilder, 0, 1));
        assertEquals(15.0f, p.measureText(spannableStringBuilder, 0, 3));
        assertEquals(15.0f, p.measureText(spannableStringBuilder, 3, 6));
        assertEquals(30.0f, p.measureText(spannableStringBuilder, 0, 6));

        p.setTextSize(24.0f);

        assertEquals(17.0f, p.measureText(spannableStringBuilder, 0, 1));
        assertEquals(32.0f, p.measureText(spannableStringBuilder, 0, 3));
        assertEquals(32.0f, p.measureText(spannableStringBuilder, 3, 6));
        assertEquals(64.0f, p.measureText(spannableStringBuilder, 0, 6));

        p.setTextSize(12.0f);
        p.setTypeface(Typeface.MONOSPACE);

        assertEquals(7.0f, p.measureText(spannableStringBuilder, 0, 1));
        assertEquals(21.0f, p.measureText(spannableStringBuilder, 0, 3));
        assertEquals(21.0f, p.measureText(spannableStringBuilder, 3, 6));
        assertEquals(42.0f, p.measureText(spannableStringBuilder, 0, 6));

        try {
            p.measureText(spannableStringBuilder, -1, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(spannableStringBuilder, 4, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(spannableStringBuilder, 0, 9);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText((SpannableStringBuilder) null, 0, 0);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        // CharSequence of StringBuilder
        StringBuilder stringBuilder = new StringBuilder("HIJHIJ");
        // The default text size and typeface
        p.setTextSize(12.0f);
        p.setTypeface(Typeface.DEFAULT);

        assertEquals(8.0f, p.measureText(stringBuilder, 0, 1));
        assertEquals(15.0f, p.measureText(stringBuilder, 0, 3));
        assertEquals(15.0f, p.measureText(stringBuilder, 3, 6));
        assertEquals(30.0f, p.measureText(stringBuilder, 0, 6));

        p.setTextSize(24.0f);

        assertEquals(17.0f, p.measureText(stringBuilder, 0, 1));
        assertEquals(32.0f, p.measureText(stringBuilder, 0, 3));
        assertEquals(32.0f, p.measureText(stringBuilder, 3, 6));
        assertEquals(64.0f, p.measureText(stringBuilder, 0, 6));

        p.setTextSize(12.0f);
        p.setTypeface(Typeface.MONOSPACE);

        assertEquals(7.0f, p.measureText(stringBuilder, 0, 1));
        assertEquals(21.0f, p.measureText(stringBuilder, 0, 3));
        assertEquals(21.0f, p.measureText(stringBuilder, 3, 6));
        assertEquals(42.0f, p.measureText(stringBuilder, 0, 6));

        try {
            p.measureText(stringBuilder, -1, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(stringBuilder, 4, 3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText(stringBuilder, 0, 9);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

        try {
            p.measureText((StringBuilder) null, 0, 0);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTextPath",
        args = {char[].class, int.class, int.class, float.class, float.class,
                android.graphics.Path.class}
    )
    public void testGetTextPath1() {
        Paint p = new Paint();
        char[] chars = {'H', 'I', 'J', 'K', 'L', 'M', 'N'};
        Path path = new Path();

        assertTrue(path.isEmpty());
        p.getTextPath(chars, 0, 7, 0, 0, path);
        assertFalse(path.isEmpty());

        try {
            p.getTextPath(chars, -2, 7, 0, 0, path);
            fail("Should throw an exception here");
        } catch (RuntimeException e) {
        }

        try {
            p.getTextPath(chars, 0, -3, 0, 0, path);
            fail("Should throw an exception here");
        } catch (RuntimeException e) {
        }

        try {
            p.getTextPath(chars, 3, 7, 0, 0, path);
            fail("Should throw an exception here");
        } catch (RuntimeException e) {
        }

    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTextPath",
        args = {java.lang.String.class, int.class, int.class, float.class, float.class,
                android.graphics.Path.class}
    )
    public void testGetTextPath2() {
        Paint p = new Paint();
        String string = "HIJKLMN";
        Path path = new Path();

        assertTrue(path.isEmpty());
        p.getTextPath(string, 0, 7, 0, 0, path);
        assertFalse(path.isEmpty());

        try {
            p.getTextPath(string, -2, 7, 0, 0, path);
            fail("Should throw an exception here");
        } catch (RuntimeException e) {
        }

        try {
            p.getTextPath(string, 0, -3, 0, 0, path);
            fail("Should throw an exception here");
        } catch (RuntimeException e) {
        }

        try {
            p.getTextPath(string, 7, 3, 0, 0, path);
            fail("Should throw an exception here");
        } catch (RuntimeException e) {
        }

        try {
            p.getTextPath(string, 3, 9, 0, 0, path);
            fail("Should throw an exception here");
        } catch (RuntimeException e) {
        }
    }

}
