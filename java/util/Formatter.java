/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

// BEGIN android-added
import org.apache.harmony.luni.util.LocaleCache;
// END android-added

/**
 * Formats arguments according to a format string (like {@code printf} in C).
 * <p>
 * It's relatively rare to use a {@code Formatter} directly. A variety of classes offer convenience
 * methods for accessing formatter functionality.
 * Of these, {@link String#format} is generally the most useful.
 * {@link java.io.PrintStream} and {@link java.io.PrintWriter} both offer
 * {@code format} and {@code printf} methods.
 * <p>
 * <i>Format strings</i> consist of plain text interspersed with format specifiers, such
 * as {@code "name: %s weight: %03dkg\n"}. Being a Java string, the usual Java string literal
 * backslash escapes are of course available.
 * <p>
 * <i>Format specifiers</i> (such as {@code "%s"} or {@code "%03d"} in the example) start with a
 * {@code %} and describe how to format their corresponding argument. It includes an optional
 * argument index, optional flags, an optional width, an optional precision, and a mandatory
 * conversion type.
 * In the example, {@code "%s"} has no flags, no width, and no precision, while
 * {@code "%03d"} has the flag {@code 0}, the width 3, and no precision.
 * <p>
 * Not all combinations of argument index, flags, width, precision, and conversion type
 * are valid.
 * <p>
 * <i>Argument index</i>. Normally, each format specifier consumes the next argument to
 * {@code format}.
 * For convenient localization, it's possible to reorder arguments so that they appear in a
 * different order in the output than the order in which they were supplied.
 * For example, {@code "%4$s"} formats the fourth argument ({@code 4$}) as a string ({@code s}).
 * It's also possible to reuse an argument with {@code <}. For example,
 * {@code format("%o %<d %<x", 64)} results in {@code "100 64 40"}.
 * <p>
 * <i>Flags</i>. The available flags are:
 * <p>
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor"> <TD COLSPAN=4> <B>Flags</B> </TD> </tr>
 * <tr>
 * <td width="5%">{@code ,}</td>
 * <td width="25%">Use grouping separators for large numbers. (Decimal only.)</td>
 * <td width="30%">{@code format("%,d", 1024);}</td>
 * <td width="30%">{@code 1,234}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code +}</td>
 * <td width="25%">Always show sign. (Decimal only.)</td>
 * <td width="30%">{@code format("%+d, %+4d", 5, 5);}</td>
 * <td width="30%"><pre>+5,   +5</pre></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code  }</td>
 * <td width="25%">A space indicates that non-negative numbers
 * should have a leading space. (Decimal only.)</td>
 * <td width="30%">{@code format("x% d% 5d", 4, 4);}</td>
 * <td width="30%"><pre>x 4    4</pre></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code (}</td>
 * <td width="25%">Put parentheses around negative numbers. (Decimal only.)</td>
 * <td width="30%">{@code format("%(d, %(d, %(6d", 12, -12, -12);}</td>
 * <td width="30%"><pre>12, (12),   (12)</pre></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code -}</td>
 * <td width="25%">Left-justify. (Requires width.)</td>
 * <td width="30%">{@code format("%-6dx", 5);}<br/>{@code format("%-3C, %3C", 'd', 0x65);}</td>
 * <td width="30%"><pre>5      x</pre><br/><pre>D  ,   E</pre></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code 0}</td>
 * <td width="25%">Pad the number with leading zeros. (Requires width.)</td>
 * <td width="30%">{@code format("%07d, %03d", 4, 5555);}</td>
 * <td width="30%">{@code 0000004, 5555}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code #}</td>
 * <td width="25%">Alternate form. (Octal and hex only.) </td>
 * <td width="30%">{@code format("%o %#o", 010, 010);}<br/>{@code format("%x %#x", 0x12, 0x12);}</td>
 * <td width="30%">{@code 10 010}<br/>{@code 12 0x12}</td>
 * </tr>
 * </table>
 * <p>
 * <i>Width</i>. The width is a decimal integer specifying the minimum number of characters to be
 * used to represent the argument. If the result would otherwise be shorter than the width, padding
 * will be added (the exact details of which depend on the flags). Note that you can't use width to
 * truncate a field, only to make it wider: see precision for control over the maximum width.
 * <p>
 * <i>Precision</i>. The precision is a {@code .} followed by a decimal integer, giving the minimum
 * number of digits for {@code d}, {@code o}, {@code x}, or {@code X}; the minimum number of digits
 * after the decimal point for {@code a}, {@code A}, {@code e}, {@code E}, {@code f}, or {@code F};
 * the maximum number of significant digits for {@code g} or {@code G}; or the maximum number of
 * characters for {@code s} or {@code S}.
 * <p>
 * <i>Conversion type</i>. One or two characters describing how to interpret the argument. Most
 * conversions are a single character, but date/time conversions all start with {@code t} and
 * have a single extra character describing the desired output.
 * <p>
 * Many conversion types have a corresponding uppercase variant that converts its result to
 * uppercase using the rules of the relevant locale (either the default or the locale set for
 * this formatter).
 * <p>
 * This table shows the available single-character (non-date/time) conversion types:
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>String conversions</B>
 * <br>
 * All types are acceptable arguments. Values of type {@link Formattable} have their
 * {@code formatTo} method invoked; all other types use {@code toString}.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code s}</td>
 * <td width="25%">String.</td>
 * <td width="30%">{@code format("%s %s", "hello", "Hello");}</td>
 * <td width="30%">{@code hello Hello}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code S}</td>
 * <td width="25%">Uppercase string.</td>
 * <td width="30%">{@code format("%S %S", "hello", "Hello");}</td>
 * <td width="30%">{@code HELLO HELLO}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>Character conversions</B>
 * <br>
 * Byte, Character, Short, and Integer (and primitives that box to those types) are all acceptable
 * as character arguments. Any other type is an error.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code c}</td>
 * <td width="25%">Character.</td>
 * <td width="30%">{@code format("%c %c", 'd', 'E');}</td>
 * <td width="30%">{@code d E}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code C}</td>
 * <td width="25%">Uppercase character.</td>
 * <td width="30%">{@code format("%C %C", 'd', 'E');}</td>
 * <td width="30%">{@code D E}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>Integer conversions</B>
 * <br>
 * Byte, Short, Integer, Long, and BigInteger (and primitives that box to those types) are all
 * acceptable as integer arguments. Any other type is an error.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code d}</td>
 * <td width="25%">Decimal.</td>
 * <td width="30%">{@code format("%d", 26);}</td>
 * <td width="30%">{@code 26}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code o}</td>
 * <td width="25%">Octal.</td>
 * <td width="30%">{@code format("%o", 032);}</td>
 * <td width="30%">{@code 32}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code x}, {@code X}</td>
 * <td width="25%">Hexadecimal.</td>
 * <td width="30%">{@code format("%x %X", 0x1a, 0x1a);}</td>
 * <td width="30%">{@code 1a 1A}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4><B>Floating-point conversions</B>
 * <br>
 * Float, Double, and BigDecimal (and primitives that box to those types) are all acceptable as
 * floating-point arguments. Any other type is an error.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code f}</td>
 * <td width="25%">Decimal floating point.</td>
 * <td width="30%"><pre>
format("%f", 123.456f);
format("%.1f", 123.456f);
format("%1.5f", 123.456f);
format("%10f", 123.456f);
format("%6.0f", 123.456f);</td>
 * <td width="30%" valign="top"><pre>
123.456001
123.5
123.45600
123.456001
&nbsp;&nbsp;&nbsp;123</pre></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code e}, {@code E}</td>
 * <td width="25%">Engineering/exponential floating point.</td>
 * <td width="30%"><pre>
format("%e", 123.456f);
format("%.1e", 123.456f);
format("%1.5E", 123.456f);
format("%10E", 123.456f);
format("%6.0E", 123.456f);</td>
 * <td width="30%" valign="top"><pre>
1.234560e+02
1.2e+02
1.23456E+02
1.234560E+02
&nbsp;1E+02</pre></td>
 * </tr>
 * <tr>
 * <td width="5%" valign="top">{@code g}, {@code G}</td>
 * <td width="25%" valign="top">Decimal or engineering, depending on the magnitude of the value.</td>
 * <td width="30%" valign="top">{@code format("%g %g", 0.123, 0.0000123);}</td>
 * <td width="30%" valign="top">{@code 0.123000 1.23000e-05}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code a}, {@code A}</td>
 * <td width="25%">Hexadecimal floating point.</td>
 * <td width="30%">{@code format("%a", 123.456f);}</td>
 * <td width="30%">{@code 0x1.edd2f2p6}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>Boolean conversion</B>
 * <br>
 * Accepts Boolean values. {@code null} is considered false, and instances of all other
 * types are considered true.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code b}, {@code B}</td>
 * <td width="25%">Boolean.</td>
 * <td width="30%">{@code format("%b %b", true, false);}<br>{@code format("%B %B", true, false);}<br>{@code format("%b", null);}<br>{@code format("%b", "hello");}</td>
 * <td width="30%">{@code true false}<br>{@code TRUE FALSE}<br>{@code false}<br>{@code true}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>Hash code conversion</B>
 * <br>
 * Invokes {@code hashCode} on its argument, which may be of any type.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code h}, {@code H}</td>
 * <td width="25%">Hexadecimal hash code.</td>
 * <td width="30%">{@code format("%h", this);}<br>{@code format("%H", this);}<br>{@code format("%h", null);}</td>
 * <td width="30%">{@code 190d11}<br>{@code 190D11}<br>{@code null}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>Zero-argument conversions</B></TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code %}</td>
 * <td width="25%">A literal % character.</td>
 * <td width="30%">{@code format("%d%%", 50);}</td>
 * <td width="30%">{@code 50%}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code n}</td>
 * <td width="25%">Newline. (The value of the system property {@code "line.separator"}.)</td>
 * <td width="30%">{@code format("first%nsecond");}</td>
 * <td width="30%">{@code first\nsecond}</td>
 * </tr>
 * </table>
 * <p>
 * It's also possible to format dates and times with {@code Formatter}, though you should seriously
 * consider using {@link java.text.SimpleDateFormat} via the factory methods in
 * {@link java.text.DateFormat} instead.
 * The facilities offered by {@code Formatter} are low-level and place the burden of localization
 * on the developer. Using {@link java.text.DateFormat#getDateInstance},
 * {@link java.text.DateFormat#getTimeInstance}, and
 * {@link java.text.DateFormat#getDateTimeInstance} is preferable for dates and times that will be
 * presented to a human. Those methods will select the best format strings for the user's locale.
 * <p>
 * The best non-localized form is <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>,
 * which you can get with {@code "%tF"} (2010-01-22), {@code "%tF %tR"} (2010-01-22 13:39),
 * {@code "%tF %tT"} (2010-01-22 13:39:15), or {@code "%tF %tT%z"} (2010-01-22 13:39:15-0800).
 * <p>
 * As with the other conversions, date/time conversion has an uppercase format. Replacing
 * {@code %t} with {@code %T} will uppercase the field according to the rules of the formatter's
 * locale.
 * <p>
 * This table shows the date/time conversions:
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4><B>Date/time conversions</B>
 * <br>
 * Calendar, Date, and Long (representing milliseconds past the epoch) are all acceptable
 * as date/time arguments. Any other type is an error. The epoch is 1970-01-01 00:00:00 UTC.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code ta}</td>
 * <td width="25%">Localized weekday name (abbreviated).</td>
 * <td width="30%">{@code format("%ta", cal, cal);}</td>
 * <td width="30%">{@code Tue}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tA}</td>
 * <td width="25%">Localized weekday name (full).</td>
 * <td width="30%">{@code format("%tA", cal, cal);}</td>
 * <td width="30%">{@code Tuesday}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tb}</td>
 * <td width="25%">Localized month name (abbreviated).</td>
 * <td width="30%">{@code format("%tb", cal);}</td>
 * <td width="30%">{@code Apr}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tB}</td>
 * <td width="25%">Localized month name (full).</td>
 * <td width="30%">{@code format("%tB", cal);}</td>
 * <td width="30%">{@code April}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tc}</td>
 * <td width="25%">Locale-preferred date and time representation. (See {@link java.text.DateFormat} for more variations.)</td>
 * <td width="30%">{@code format("%tc", cal);}</td>
 * <td width="30%">{@code Tue Apr 01 16:19:17 CEST 2008}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tC}</td>
 * <td width="25%">2-digit century.</td>
 * <td width="30%">{@code format("%tC", cal);}</td>
 * <td width="30%">{@code 20}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code td}</td>
 * <td width="25%">2-digit day of month (01-31).</td>
 * <td width="30%">{@code format("%td", cal);}</td>
 * <td width="30%">{@code 01}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tD}</td>
 * <td width="25%">Ambiguous US date format (MM/DD/YY). Do not use.</td>
 * <td width="30%">{@code format("%tD", cal);}</td>
 * <td width="30%">{@code 04/01/08}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code te}</td>
 * <td width="25%">Day of month (1-31).</td>
 * <td width="30%">{@code format("%te", cal);}</td>
 * <td width="30%">{@code 1}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tF}</td>
 * <td width="25%">Full date in ISO 8601 format (YYYY-MM-DD).</td>
 * <td width="30%">{@code format("%tF", cal);}</td>
 * <td width="30%">{@code 2008-04-01}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code th}</td>
 * <td width="25%">Synonym for {@code %tb}.</td>
 * <td width="30%"></td>
 * <td width="30%"></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tH}</td>
 * <td width="25%">24-hour hour of day (00-23).</td>
 * <td width="30%">{@code format("%tH", cal);}</td>
 * <td width="30%">{@code 16}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tI}</td>
 * <td width="25%">12-hour hour of day (01-12).</td>
 * <td width="30%">{@code format("%tH", cal);}</td>
 * <td width="30%">{@code 04}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tj}</td>
 * <td width="25%">3-digit day of year (001-366).</td>
 * <td width="30%">{@code format("%tj", cal);}</td>
 * <td width="30%">{@code 092}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tk}</td>
 * <td width="25%">24-hour hour of day (0-23).</td>
 * <td width="30%">{@code format("%tH", cal);}</td>
 * <td width="30%">{@code 16}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tl}</td>
 * <td width="25%">12-hour hour of day (1-12).</td>
 * <td width="30%">{@code format("%tH", cal);}</td>
 * <td width="30%">{@code 4}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tL}</td>
 * <td width="25%">Milliseconds.</td>
 * <td width="30%">{@code format("%tL", cal);}</td>
 * <td width="30%">{@code 359}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tm}</td>
 * <td width="25%">2-digit month of year (01-12).</td>
 * <td width="30%">{@code format("%tm", cal);}</td>
 * <td width="30%">{@code 04}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tM}</td>
 * <td width="25%">2-digit minute.</td>
 * <td width="30%">{@code format("%tM", cal);}</td>
 * <td width="30%">{@code 08}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tN}</td>
 * <td width="25%">Nanoseconds.</td>
 * <td width="30%">{@code format("%tN", cal);}</td>
 * <td width="30%">{@code 359000000}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tp}</td>
 * <td width="25%">a.m. or p.m.</td>
 * <td width="30%">{@code format("%tp %Tp", cal, cal);}</td>
 * <td width="30%">{@code pm PM}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tQ}</td>
 * <td width="25%">Milliseconds since the epoch.</td>
 * <td width="30%">{@code format("%tQ", cal);}</td>
 * <td width="30%">{@code 1207059412656}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tr}</td>
 * <td width="25%">Full 12-hour time ({@code %tI:%tM:%tS %Tp}).</td>
 * <td width="30%">{@code format("%tr", cal);}</td>
 * <td width="30%">{@code 04:15:32 PM}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tR}</td>
 * <td width="25%">Short 24-hour time ({@code %tH:%tM}).</td>
 * <td width="30%">{@code format("%tR", cal);}</td>
 * <td width="30%">{@code 16:15}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code ts}</td>
 * <td width="25%">Seconds since the epoch.</td>
 * <td width="30%">{@code format("%ts", cal);}</td>
 * <td width="30%">{@code 1207059412}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tS}</td>
 * <td width="25%">2-digit seconds (00-60).</td>
 * <td width="30%">{@code format("%tS", cal);}</td>
 * <td width="30%">{@code 17}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tT}</td>
 * <td width="25%">Full 24-hour time ({@code %tH:%tM:%tS}).</td>
 * <td width="30%">{@code format("%tT", cal);}</td>
 * <td width="30%">{@code 16:15:32}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code ty}</td>
 * <td width="25%">2-digit year (00-99).</td>
 * <td width="30%">{@code format("%ty", cal);}</td>
 * <td width="30%">{@code 08}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tY}</td>
 * <td width="25%">4-digit year.</td>
 * <td width="30%">{@code format("%tY", cal);}</td>
 * <td width="30%">{@code 2008}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tz}</td>
 * <td width="25%">Time zone GMT offset.</td>
 * <td width="30%">{@code format("%tz", cal);}</td>
 * <td width="30%">{@code +0100}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tZ}</td>
 * <td width="25%">Localized time zone abbreviation.</td>
 * <td width="30%">{@code format("%tZ", cal);}</td>
 * <td width="30%">{@code CEST}</td>
 * </tr>
 * </table>
 * <p>
 * Formatter is not thread-safe.
 *
 * @since 1.5
 * @see java.text.DateFormat
 * @see Formattable
 * @see java.text.SimpleDateFormat
 */
public final class Formatter implements Closeable, Flushable {

    /**
     * The enumeration giving the available styles for formatting very large
     * decimal numbers.
     */
    public enum BigDecimalLayoutForm {
        /**
         * Use scientific style for BigDecimals.
         */
        SCIENTIFIC,
        /**
         * Use normal decimal/float style for BigDecimals.
         */
        DECIMAL_FLOAT
    }

    private Appendable out;

    private Locale locale;

    private boolean closed = false;

    private IOException lastIOException;

    /**
     * Constructs a {@code Formatter}.
     *
     * The output is written to a {@code StringBuilder} which can be acquired by invoking
     * {@link #out()} and whose content can be obtained by calling
     * {@code toString()}.
     *
     * The {@code Locale} for the {@code Formatter} is the default {@code Locale}.
     */
    public Formatter() {
        this(new StringBuilder(), Locale.getDefault());
    }

    /**
     * Constructs a {@code Formatter} whose output will be written to the
     * specified {@code Appendable}.
     *
     * The locale for the {@code Formatter} is the default {@code Locale}.
     *
     * @param a
     *            the output destination of the {@code Formatter}. If {@code a} is {@code null},
     *            then a {@code StringBuilder} will be used.
     */
    public Formatter(Appendable a) {
        this(a, Locale.getDefault());
    }

    /**
     * Constructs a {@code Formatter} with the specified {@code Locale}.
     *
     * The output is written to a {@code StringBuilder} which can be acquired by invoking
     * {@link #out()} and whose content can be obtained by calling
     * {@code toString()}.
     *
     * @param l
     *            the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
     *            then no localization will be used.
     */
    public Formatter(Locale l) {
        this(new StringBuilder(), l);
    }

    /**
     * Constructs a {@code Formatter} with the specified {@code Locale}
     * and whose output will be written to the
     * specified {@code Appendable}.
     *
     * @param a
     *            the output destination of the {@code Formatter}. If {@code a} is {@code null},
     *            then a {@code StringBuilder} will be used.
     * @param l
     *            the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
     *            then no localization will be used.
     */
    public Formatter(Appendable a, Locale l) {
        if (null == a) {
            out = new StringBuilder();
        } else {
            out = a;
        }
        locale = l;
    }

    /**
     * Constructs a {@code Formatter} whose output is written to the specified file.
     *
     * The charset of the {@code Formatter} is the default charset.
     *
     * The {@code Locale} for the {@code Formatter} is the default {@code Locale}.
     *
     * @param fileName
     *            the filename of the file that is used as the output
     *            destination for the {@code Formatter}. The file will be truncated to
     *            zero size if the file exists, or else a new file will be
     *            created. The output of the {@code Formatter} is buffered.
     * @throws FileNotFoundException
     *             if the filename does not denote a normal and writable file,
     *             or if a new file cannot be created, or if any error arises when
     *             opening or creating the file.
     * @throws SecurityException
     *             if there is a {@code SecurityManager} in place which denies permission
     *             to write to the file in {@code checkWrite(file.getPath())}.
     */
    public Formatter(String fileName) throws FileNotFoundException {
        this(new File(fileName));

    }

    /**
     * Constructs a {@code Formatter} whose output is written to the specified file.
     *
     * The {@code Locale} for the {@code Formatter} is the default {@code Locale}.
     *
     * @param fileName
     *            the filename of the file that is used as the output
     *            destination for the {@code Formatter}. The file will be truncated to
     *            zero size if the file exists, or else a new file will be
     *            created. The output of the {@code Formatter} is buffered.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @throws FileNotFoundException
     *             if the filename does not denote a normal and writable file,
     *             or if a new file cannot be created, or if any error arises when
     *             opening or creating the file.
     * @throws SecurityException
     *             if there is a {@code SecurityManager} in place which denies permission
     *             to write to the file in {@code checkWrite(file.getPath())}.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(String fileName, String csn) throws FileNotFoundException,
            UnsupportedEncodingException {
        this(new File(fileName), csn);
    }

    /**
     * Constructs a {@code Formatter} with the given {@code Locale} and charset,
     * and whose output is written to the specified file.
     *
     * @param fileName
     *            the filename of the file that is used as the output
     *            destination for the {@code Formatter}. The file will be truncated to
     *            zero size if the file exists, or else a new file will be
     *            created. The output of the {@code Formatter} is buffered.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @param l
     *            the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
     *            then no localization will be used.
     * @throws FileNotFoundException
     *             if the filename does not denote a normal and writable file,
     *             or if a new file cannot be created, or if any error arises when
     *             opening or creating the file.
     * @throws SecurityException
     *             if there is a {@code SecurityManager} in place which denies permission
     *             to write to the file in {@code checkWrite(file.getPath())}.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(String fileName, String csn, Locale l)
            throws FileNotFoundException, UnsupportedEncodingException {

        this(new File(fileName), csn, l);
    }

    /**
     * Constructs a {@code Formatter} whose output is written to the specified {@code File}.
     *
     * The charset of the {@code Formatter} is the default charset.
     *
     * The {@code Locale} for the {@code Formatter} is the default {@code Locale}.
     *
     * @param file
     *            the {@code File} that is used as the output destination for the
     *            {@code Formatter}. The {@code File} will be truncated to zero size if the {@code File}
     *            exists, or else a new {@code File} will be created. The output of the
     *            {@code Formatter} is buffered.
     * @throws FileNotFoundException
     *             if the {@code File} is not a normal and writable {@code File}, or if a
     *             new {@code File} cannot be created, or if any error rises when opening or
     *             creating the {@code File}.
     * @throws SecurityException
     *             if there is a {@code SecurityManager} in place which denies permission
     *             to write to the {@code File} in {@code checkWrite(file.getPath())}.
     */
    public Formatter(File file) throws FileNotFoundException {
        this(new FileOutputStream(file));
    }

    /**
     * Constructs a {@code Formatter} with the given charset,
     * and whose output is written to the specified {@code File}.
     *
     * The {@code Locale} for the {@code Formatter} is the default {@code Locale}.
     *
     * @param file
     *            the {@code File} that is used as the output destination for the
     *            {@code Formatter}. The {@code File} will be truncated to zero size if the {@code File}
     *            exists, or else a new {@code File} will be created. The output of the
     *            {@code Formatter} is buffered.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @throws FileNotFoundException
     *             if the {@code File} is not a normal and writable {@code File}, or if a
     *             new {@code File} cannot be created, or if any error rises when opening or
     *             creating the {@code File}.
     * @throws SecurityException
     *             if there is a {@code SecurityManager} in place which denies permission
     *             to write to the {@code File} in {@code checkWrite(file.getPath())}.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(File file, String csn) throws FileNotFoundException,
            UnsupportedEncodingException {
        this(file, csn, Locale.getDefault());
    }

    /**
     * Constructs a {@code Formatter} with the given {@code Locale} and charset,
     * and whose output is written to the specified {@code File}.
     *
     * @param file
     *            the {@code File} that is used as the output destination for the
     *            {@code Formatter}. The {@code File} will be truncated to zero size if the {@code File}
     *            exists, or else a new {@code File} will be created. The output of the
     *            {@code Formatter} is buffered.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @param l
     *            the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
     *            then no localization will be used.
     * @throws FileNotFoundException
     *             if the {@code File} is not a normal and writable {@code File}, or if a
     *             new {@code File} cannot be created, or if any error rises when opening or
     *             creating the {@code File}.
     * @throws SecurityException
     *             if there is a {@code SecurityManager} in place which denies permission
     *             to write to the {@code File} in {@code checkWrite(file.getPath())}.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(File file, String csn, Locale l)
            throws FileNotFoundException, UnsupportedEncodingException {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fout, csn);
            // BEGIN android-changed
            out = new BufferedWriter(writer, 8192);
            // END android-changed
        } catch (RuntimeException e) {
            closeOutputStream(fout);
            throw e;
        } catch (UnsupportedEncodingException e) {
            closeOutputStream(fout);
            throw e;
        }

        locale = l;
    }

    /**
     * Constructs a {@code Formatter} whose output is written to the specified {@code OutputStream}.
     *
     * The charset of the {@code Formatter} is the default charset.
     *
     * The {@code Locale} for the {@code Formatter} is the default {@code Locale}.
     *
     * @param os
     *            the stream to be used as the destination of the {@code Formatter}.
     */
    public Formatter(OutputStream os) {
        OutputStreamWriter writer = new OutputStreamWriter(os, Charset
                .defaultCharset());
        // BEGIN android-changed
        out = new BufferedWriter(writer, 8192);
        // END android-changed
        locale = Locale.getDefault();
    }

    /**
     * Constructs a {@code Formatter} with the given charset,
     * and whose output is written to the specified {@code OutputStream}.
     *
     * The {@code Locale} for the {@code Formatter} is the default {@code Locale}.
     *
     * @param os
     *            the stream to be used as the destination of the {@code Formatter}.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(OutputStream os, String csn)
            throws UnsupportedEncodingException {

        this(os, csn, Locale.getDefault());
    }

    /**
     * Constructs a {@code Formatter} with the given {@code Locale} and charset,
     * and whose output is written to the specified {@code OutputStream}.
     *
     * @param os
     *            the stream to be used as the destination of the {@code Formatter}.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @param l
     *            the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
     *            then no localization will be used.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(OutputStream os, String csn, Locale l)
            throws UnsupportedEncodingException {

        OutputStreamWriter writer = new OutputStreamWriter(os, csn);
        // BEGIN android-changed
        out = new BufferedWriter(writer, 8192);
        // END android-changed

        locale = l;
    }

    /**
     * Constructs a {@code Formatter} whose output is written to the specified {@code PrintStream}.
     *
     * The charset of the {@code Formatter} is the default charset.
     *
     * The {@code Locale} for the {@code Formatter} is the default {@code Locale}.
     *
     * @param ps
     *            the {@code PrintStream} used as destination of the {@code Formatter}. If
     *            {@code ps} is {@code null}, then a {@code NullPointerException} will
     *            be raised.
     */
    public Formatter(PrintStream ps) {
        if (null == ps) {
            throw new NullPointerException();
        }
        out = ps;
        locale = Locale.getDefault();
    }

    private void checkClosed() {
        if (closed) {
            throw new FormatterClosedException();
        }
    }

    /**
     * Returns the {@code Locale} of the {@code Formatter}.
     *
     * @return the {@code Locale} for the {@code Formatter} or {@code null} for no {@code Locale}.
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    public Locale locale() {
        checkClosed();
        return locale;
    }

    /**
     * Returns the output destination of the {@code Formatter}.
     *
     * @return the output destination of the {@code Formatter}.
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    public Appendable out() {
        checkClosed();
        return out;
    }

    /**
     * Returns the content by calling the {@code toString()} method of the output
     * destination.
     *
     * @return the content by calling the {@code toString()} method of the output
     *         destination.
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    @Override
    public String toString() {
        checkClosed();
        return out.toString();
    }

    /**
     * Flushes the {@code Formatter}. If the output destination is {@link Flushable},
     * then the method {@code flush()} will be called on that destination.
     *
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    public void flush() {
        checkClosed();
        if (out instanceof Flushable) {
            try {
                ((Flushable) out).flush();
            } catch (IOException e) {
                lastIOException = e;
            }
        }
    }

    /**
     * Closes the {@code Formatter}. If the output destination is {@link Closeable},
     * then the method {@code close()} will be called on that destination.
     *
     * If the {@code Formatter} has been closed, then calling the this method will have no
     * effect.
     *
     * Any method but the {@link #ioException()} that is called after the
     * {@code Formatter} has been closed will raise a {@code FormatterClosedException}.
     */
    public void close() {
        if (!closed) {
            closed = true;
            try {
                if (out instanceof Closeable) {
                    ((Closeable) out).close();
                }
            } catch (IOException e) {
                lastIOException = e;
            }
        }
    }

    /**
     * Returns the last {@code IOException} thrown by the {@code Formatter}'s output
     * destination. If the {@code append()} method of the destination does not throw
     * {@code IOException}s, the {@code ioException()} method will always return {@code null}.
     *
     * @return the last {@code IOException} thrown by the {@code Formatter}'s output
     *         destination.
     */
    public IOException ioException() {
        return lastIOException;
    }

    /**
     * Writes a formatted string to the output destination of the {@code Formatter}.
     *
     * @param format
     *            a format string.
     * @param args
     *            the arguments list used in the {@code format()} method. If there are
     *            more arguments than those specified by the format string, then
     *            the additional arguments are ignored.
     * @return this {@code Formatter}.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, or if fewer arguments are sent than those required by
     *             the format string, or any other illegal situation.
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    public Formatter format(String format, Object... args) {
        // BEGIN android-changed
        doFormat(format, args);
        return this;
        // END android-changed
    }

    // BEGIN android-added
    /**
     * Cached transformer. Improves performance when format() is called multiple
     * times.
     */
    private Transformer transformer;
    // END android-added

    /**
     * Writes a formatted string to the output destination of the {@code Formatter}.
     *
     * @param l
     *            the {@code Locale} used in the method. If {@code locale} is
     *            {@code null}, then no localization will be applied. This
     *            parameter does not change this Formatter's default {@code Locale}
     *            as specified during construction, and only applies for the
     *            duration of this call.
     * @param format
     *            a format string.
     * @param args
     *            the arguments list used in the {@code format()} method. If there are
     *            more arguments than those specified by the format string, then
     *            the additional arguments are ignored.
     * @return this {@code Formatter}.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, or if fewer arguments are sent than those required by
     *             the format string, or any other illegal situation.
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    public Formatter format(Locale l, String format, Object... args) {
        // BEGIN android-changed
        Locale originalLocale = locale;
        try {
            this.locale = l;
            doFormat(format, args);
        } finally {
            this.locale = originalLocale;
        }
        return this;
        // END android-changed
    }

    // BEGIN android-changed
    private void doFormat(String format, Object... args) {
        checkClosed();

        // Reuse the previous transformer if the locale matches.
        if (transformer == null || !transformer.locale.equals(locale)) {
            transformer = new Transformer(this, locale);
        }

        FormatSpecifierParser fsp = new FormatSpecifierParser(format);

        int currentObjectIndex = 0;
        Object lastArgument = null;
        boolean hasLastArgumentSet = false;

        int length = format.length();
        int i = 0;
        while (i < length) {
            // Find the maximal plain-text sequence...
            int plainTextStart = i;
            int nextPercent = format.indexOf('%', i);
            int plainTextEnd = (nextPercent == -1) ? length : nextPercent;
            // ...and output it.
            if (plainTextEnd > plainTextStart) {
                outputCharSequence(format, plainTextStart, plainTextEnd);
            }
            i = plainTextEnd;
            // Do we have a format specifier?
            if (i < length) {
                FormatToken token = fsp.parseFormatToken(i + 1);

                Object argument = null;
                if (token.requireArgument()) {
                    int index = token.getArgIndex() == FormatToken.UNSET ? currentObjectIndex++ : token.getArgIndex();
                    argument = getArgument(args, index, fsp, lastArgument, hasLastArgumentSet);
                    lastArgument = argument;
                    hasLastArgumentSet = true;
                }

                CharSequence substitution = transformer.transform(token, argument);
                // The substitution is null if we called Formattable.formatTo.
                if (substitution != null) {
                    outputCharSequence(substitution, 0, substitution.length());
                }
                i = fsp.i;
            }
        }
    }
    // END android-changed

    // BEGIN android-added
    // Fixes http://code.google.com/p/android/issues/detail?id=1767.
    private void outputCharSequence(CharSequence cs, int start, int end) {
        try {
            out.append(cs, start, end);
        } catch (IOException e) {
            lastIOException = e;
        }
    }
    // END android-added

    private Object getArgument(Object[] args, int index, FormatSpecifierParser fsp,
            Object lastArgument, boolean hasLastArgumentSet) {
        if (index == FormatToken.LAST_ARGUMENT_INDEX && !hasLastArgumentSet) {
            throw new MissingFormatArgumentException("<"); //$NON-NLS-1$
        }

        if (null == args) {
            return null;
        }

        if (index >= args.length) {
            throw new MissingFormatArgumentException(fsp.getFormatSpecifierText());
        }

        if (index == FormatToken.LAST_ARGUMENT_INDEX) {
            return lastArgument;
        }

        return args[index];
    }

    private static void closeOutputStream(OutputStream os) {
        if (null == os) {
            return;
        }
        try {
            os.close();

        } catch (IOException e) {
            // silently
        }
    }

    /*
     * Complete details of a single format specifier parsed from a format string.
     */
    private static class FormatToken {
        static final int LAST_ARGUMENT_INDEX = -2;

        static final int UNSET = -1;

        static final int FLAGS_UNSET = 0;

        static final int DEFAULT_PRECISION = 6;

        static final int FLAG_ZERO = 1 << 4;

        private int argIndex = UNSET;

        // These have package access for performance. They used to be represented by an int bitmask
        // and accessed via methods, but Android's JIT doesn't yet do a good job of such code.
        // Direct field access, on the other hand, is fast.
        boolean flagAdd;
        boolean flagComma;
        boolean flagMinus;
        boolean flagParenthesis;
        boolean flagSharp;
        boolean flagSpace;
        boolean flagZero;

        private char conversionType = (char) UNSET;
        private char dateSuffix;

        private int precision = UNSET;
        private int width = UNSET;

        private StringBuilder strFlags;

        // Tests whether there were no flags, no width, and no precision specified.
        boolean isDefault() {
            // TODO: call hasDefaultFlags when the JIT can inline it.
            return !flagAdd && !flagComma && !flagMinus && !flagParenthesis && !flagSharp &&
                    !flagSpace && !flagZero && width == UNSET && precision == UNSET;
        }

        boolean hasDefaultFlags() {
            return !flagAdd && !flagComma && !flagMinus && !flagParenthesis && !flagSharp &&
                    !flagSpace && !flagZero;
        }

        boolean isPrecisionSet() {
            return precision != UNSET;
        }

        boolean isWidthSet() {
            return width != UNSET;
        }

        boolean hasArg() {
            return argIndex != UNSET;
        }

        int getArgIndex() {
            return argIndex;
        }

        void setArgIndex(int index) {
            argIndex = index;
        }

        int getWidth() {
            return width;
        }

        void setWidth(int width) {
            this.width = width;
        }

        int getPrecision() {
            return precision;
        }

        void setPrecision(int precise) {
            this.precision = precise;
        }

        String getStrFlags() {
            return (strFlags != null) ? strFlags.toString() : "";
        }

        /*
         * Sets qualified char as one of the flags. If the char is qualified,
         * sets it as a flag and returns true. Or else returns false.
         */
        boolean setFlag(int ch) {
            boolean dupe = false;
            switch (ch) {
            case '+':
                dupe = flagAdd;
                flagAdd = true;
                break;
            case ',':
                dupe = flagComma;
                flagComma = true;
                break;
            case '-':
                dupe = flagMinus;
                flagMinus = true;
                break;
            case '(':
                dupe = flagParenthesis;
                flagParenthesis = true;
                break;
            case '#':
                dupe = flagSharp;
                flagSharp = true;
                break;
            case ' ':
                dupe = flagSpace;
                flagSpace = true;
                break;
            case '0':
                dupe = flagZero;
                flagZero = true;
                break;
            default:
                return false;
            }
            if (dupe) {
                throw new DuplicateFormatFlagsException(String.valueOf(ch));
            }
            if (strFlags == null) {
                strFlags = new StringBuilder(7); // There are seven possible flags.
            }
            strFlags.append((char) ch);
            return true;
        }

        char getConversionType() {
            return conversionType;
        }

        void setConversionType(char c) {
            conversionType = c;
        }

        char getDateSuffix() {
            return dateSuffix;
        }

        void setDateSuffix(char c) {
            dateSuffix = c;
        }

        boolean requireArgument() {
            return conversionType != '%' && conversionType != 'n';
        }

        void checkMissingWidth() {
            if (flagMinus && width == UNSET) {
                throw new MissingFormatWidthException("-" + conversionType);
            }
        }

        void ensureOnlyMinus() {
            if (flagAdd || flagComma || flagParenthesis || flagSharp || flagSpace || flagZero) {
                throw new FormatFlagsConversionMismatchException(getStrFlags(), conversionType);
            }
        }

        void ensureNoPrecision() {
            if (isPrecisionSet()) {
                throw new IllegalFormatPrecisionException(precision);
            }
        }
    }

    /*
     * Transforms the argument to the formatted string according to the format
     * information contained in the format token.
     */
    private static class Transformer {
        private Formatter formatter;
        private FormatToken formatToken;
        private Object arg;
        private Locale locale;
        private DecimalFormatSymbols decimalFormatSymbols;
        private static String lineSeparator;

        // BEGIN android-changed
        // This object is mutated during use, so can't be cached safely.
        // private NumberFormat numberFormat;
        // END android-changed

        private DateTimeUtil dateTimeUtil;

        Transformer(Formatter formatter, Locale locale) {
            this.formatter = formatter;
            this.locale = (null == locale ? Locale.US : locale);
        }

        private NumberFormat getNumberFormat() {
            // BEGIN android-changed
            return LocaleCache.getNumberFormat(locale);
            // END android-changed
        }

        // BEGIN android-changed
        DecimalFormatSymbols getDecimalFormatSymbols() {
            if (decimalFormatSymbols == null) {
                decimalFormatSymbols = new DecimalFormatSymbols(locale);
            }
            return decimalFormatSymbols;
        }
        // END android-changed

        /*
         * Gets the formatted string according to the format token and the
         * argument.
         */
        CharSequence transform(FormatToken token, Object argument) {
            this.formatToken = token;
            this.arg = argument;

            // There are only two format specifiers that matter: "%d" and "%s".
            // Nothing else is common in the wild. We fast-path these two to
            // avoid the heavyweight machinery needed to cope with flags, width,
            // and precision.
            if (token.isDefault()) {
                switch (token.getConversionType()) {
                case 's':
                    if (arg == null) {
                        return "null";
                    } else if (!(arg instanceof Formattable)) {
                        return arg.toString();
                    }
                    break;
                case 'd':
                    if (arg instanceof Integer || arg instanceof Long || arg instanceof Short || arg instanceof Byte) {
                        // TODO: when we fix the rest of formatter to correctly use locale-specific
                        // digits when getDecimalFormatSymbols().getZeroDigit() != '0', we'll need
                        // to add a special case here too.
                        return arg.toString();
                    }
                }
            }

            CharSequence result;
            switch (token.getConversionType()) {
                case 'B':
                case 'b': {
                    result = transformFromBoolean();
                    break;
                }
                case 'H':
                case 'h': {
                    result = transformFromHashCode();
                    break;
                }
                case 'S':
                case 's': {
                    result = transformFromString();
                    break;
                }
                case 'C':
                case 'c': {
                    result = transformFromCharacter();
                    break;
                }
                case 'd':
                case 'o':
                case 'x':
                case 'X': {
                    if (null == arg || arg instanceof BigInteger) {
                        result = transformFromBigInteger();
                    } else {
                        result = transformFromInteger();
                    }
                    break;
                }
                case 'e':
                case 'E':
                case 'g':
                case 'G':
                case 'f':
                case 'a':
                case 'A': {
                    result = transformFromFloat();
                    break;
                }
                case '%': {
                    result = transformFromPercent();
                    break;
                }
                case 'n': {
                    result = transformFromLineSeparator();
                    break;
                }
                case 't':
                case 'T': {
                    result = transformFromDateTime();
                    break;
                }
                default: {
                    throw new UnknownFormatConversionException(String
                            .valueOf(token.getConversionType()));
                }
            }

            if (Character.isUpperCase(token.getConversionType())) {
                if (result != null) {
                    result = result.toString().toUpperCase(locale);
                }
            }
            return result;
        }

        private IllegalFormatConversionException badArgumentType() {
            throw new IllegalFormatConversionException(formatToken.getConversionType(),
                    arg.getClass());
        }

        /*
         * Transforms the Boolean argument to a formatted string.
         */
        private CharSequence transformFromBoolean() {
            formatToken.checkMissingWidth();
            formatToken.ensureOnlyMinus();
            CharSequence result;
            if (arg instanceof Boolean) {
                result = arg.toString();
            } else if (arg == null) {
                result = "false"; //$NON-NLS-1$
            } else {
                result = "true"; //$NON-NLS-1$
            }
            return padding(result, 0);
        }

        /*
         * Transforms the hash code of the argument to a formatted string.
         */
        private CharSequence transformFromHashCode() {
            formatToken.checkMissingWidth();
            formatToken.ensureOnlyMinus();
            CharSequence result;
            if (arg == null) {
                result = "null"; //$NON-NLS-1$
            } else {
                result = Integer.toHexString(arg.hashCode());
            }
            return padding(result, 0);
        }

        /*
         * Transforms the String to a formatted string.
         */
        private CharSequence transformFromString() {
            formatToken.checkMissingWidth();
            if (arg instanceof Formattable) {
                // only minus and sharp flag is valid
                if (formatToken.flagAdd || formatToken.flagComma || formatToken.flagParenthesis || formatToken.flagSpace || formatToken.flagZero) {
                    throw new IllegalFormatFlagsException(formatToken.getStrFlags());
                }
                int flag = 0;
                if (formatToken.flagMinus) {
                    flag |= FormattableFlags.LEFT_JUSTIFY;
                }
                if (formatToken.flagSharp) {
                    flag |= FormattableFlags.ALTERNATE;
                }
                if (Character.isUpperCase(formatToken.getConversionType())) {
                    flag |= FormattableFlags.UPPERCASE;
                }
                ((Formattable) arg).formatTo(formatter, flag, formatToken.getWidth(),
                        formatToken.getPrecision());
                // all actions have been taken out in the
                // Formattable.formatTo, thus there is nothing to do, just
                // returns null, which tells the Parser to add nothing to the
                // output.
                return null;
            }
            // only '-' is valid for flags if the argument is not an instance of Formattable
            formatToken.ensureOnlyMinus();
            CharSequence result = arg != null ? arg.toString() : "null";
            return padding(result, 0);
        }

        /*
         * Transforms the Character to a formatted string.
         */
        private CharSequence transformFromCharacter() {
            formatToken.checkMissingWidth();
            formatToken.ensureOnlyMinus();
            formatToken.ensureNoPrecision();

            if (arg == null) {
                return padding("null", 0);
            }
            if (arg instanceof Character) {
                return padding(String.valueOf(arg), 0);
            } else if (arg instanceof Byte || arg instanceof Short || arg instanceof Integer) {
                int codePoint = ((Number) arg).intValue();
                if (!Character.isValidCodePoint(codePoint)) {
                    throw new IllegalFormatCodePointException(codePoint);
                }
                CharSequence result = (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT)
                        ? String.valueOf((char) codePoint)
                        : String.valueOf(Character.toChars(codePoint));
                return padding(result, 0);
            } else {
                throw badArgumentType();
            }
        }

        /*
         * Transforms percent to a formatted string. Only '-' is legal flag.
         * Precision and arguments are illegal.
         */
        private CharSequence transformFromPercent() {
            formatToken.checkMissingWidth();
            formatToken.ensureOnlyMinus();
            formatToken.ensureNoPrecision();
            if (formatToken.hasArg()) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags());
            }
            return padding("%", 0);
        }

        /*
         * Transforms line separator to a formatted string. Any flag, width,
         * precision or argument is illegal.
         */
        private CharSequence transformFromLineSeparator() {
            formatToken.ensureNoPrecision();

            if (formatToken.isWidthSet()) {
                throw new IllegalFormatWidthException(formatToken.getWidth());
            }

            if (!formatToken.hasDefaultFlags() || formatToken.hasArg()) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags());
            }

            if (lineSeparator == null) {
                lineSeparator = AccessController.doPrivileged(new PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty("line.separator"); //$NON-NLS-1$
                    }
                });
            }
            return lineSeparator;
        }

        /*
         * Pads characters to the formatted string.
         */
        private CharSequence padding(CharSequence source, int startIndex) {
            boolean sourceIsStringBuilder = (source instanceof StringBuilder);

            int start = startIndex;
            int width = formatToken.getWidth();
            int precision = formatToken.getPrecision();

            int length = source.length();
            if (precision >= 0) {
                length = Math.min(length, precision);
                if (sourceIsStringBuilder) {
                    ((StringBuilder) source).setLength(length);
                } else {
                    source = source.subSequence(0, length);
                }
            }
            if (width > 0) {
                width = Math.max(source.length(), width);
            }
            if (length >= width) {
                return source;
            }

            char paddingChar = '\u0020'; // space as padding char.
            if (formatToken.flagZero) {
                if (formatToken.getConversionType() == 'd') {
                    paddingChar = getDecimalFormatSymbols().getZeroDigit();
                } else {
                    paddingChar = '0';
                }
            } else {
                // if padding char is space, always pad from the start.
                start = 0;
            }
            char[] paddingChars = new char[width - length];
            Arrays.fill(paddingChars, paddingChar);

            boolean paddingRight = formatToken.flagMinus;
            StringBuilder result = toStringBuilder(source);
            if (paddingRight) {
                result.append(paddingChars);
            } else {
                result.insert(start, paddingChars);
            }
            return result;
        }

        private StringBuilder toStringBuilder(CharSequence cs) {
            return cs instanceof StringBuilder ? (StringBuilder) cs : new StringBuilder(cs);
        }

        private StringBuilder wrapParentheses(StringBuilder result) {
            result.setCharAt(0, '('); // Replace the '-'.
            if (formatToken.flagZero) {
                formatToken.setWidth(formatToken.getWidth() - 1);
                result = (StringBuilder) padding(result, 1);
                result.append(')');
            } else {
                result.append(')');
                result = (StringBuilder) padding(result, 0);
            }
            return result;
        }

        /*
         * Transforms the Integer to a formatted string.
         */
        private CharSequence transformFromInteger() {
            int startIndex = 0;
            StringBuilder result = new StringBuilder();
            char currentConversionType = formatToken.getConversionType();

            if (formatToken.flagMinus || formatToken.flagZero) {
                if (!formatToken.isWidthSet()) {
                    throw new MissingFormatWidthException(formatToken.getStrFlags());
                }
            }
            // Combination of '+' and ' ' is illegal.
            if (formatToken.flagAdd && formatToken.flagSpace) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags());
            }
            formatToken.ensureNoPrecision();
            long value;
            if (arg instanceof Long) {
                value = ((Long) arg).longValue();
            } else if (arg instanceof Integer) {
                value = ((Integer) arg).longValue();
            } else if (arg instanceof Short) {
                value = ((Short) arg).longValue();
            } else if (arg instanceof Byte) {
                value = ((Byte) arg).longValue();
            } else {
                throw badArgumentType();
            }
            if ('d' != currentConversionType) {
                if (formatToken.flagAdd || formatToken.flagSpace || formatToken.flagComma ||
                        formatToken.flagParenthesis) {
                    throw new FormatFlagsConversionMismatchException(formatToken.getStrFlags(),
                            formatToken.getConversionType());
                }
            }

            if (formatToken.flagSharp) {
                if ('d' == currentConversionType) {
                    throw new FormatFlagsConversionMismatchException(formatToken.getStrFlags(),
                            formatToken.getConversionType());
                } else if ('o' == currentConversionType) {
                    result.append("0"); //$NON-NLS-1$
                    startIndex += 1;
                } else {
                    result.append("0x"); //$NON-NLS-1$
                    startIndex += 2;
                }
            }

            if (formatToken.flagMinus && formatToken.flagZero) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags());
            }

            if ('d' == currentConversionType) {
                if (formatToken.flagComma) {
                    NumberFormat numberFormat = getNumberFormat();
                    numberFormat.setGroupingUsed(true);
                    result.append(numberFormat.format(arg));
                } else {
                    result.append(value);
                }

                if (value < 0) {
                    if (formatToken.flagParenthesis) {
                        return wrapParentheses(result);
                    } else if (formatToken.flagZero) {
                        startIndex++;
                    }
                } else {
                    if (formatToken.flagAdd) {
                        result.insert(0, '+');
                        startIndex += 1;
                    } else if (formatToken.flagSpace) {
                        result.insert(0, ' ');
                        startIndex += 1;
                    }
                }
            } else {
                // Undo sign-extension, since we'll be using Long.to(Octal|Hex)String.
                if (arg instanceof Byte) {
                    value &= 0xffL;
                } else if (arg instanceof Short) {
                    value &= 0xffffL;
                } else if (arg instanceof Integer) {
                    value &= 0xffffffffL;
                }
                if ('o' == currentConversionType) {
                    result.append(Long.toOctalString(value));
                } else {
                    result.append(Long.toHexString(value));
                }
            }

            return padding(result, startIndex);
        }

        private CharSequence transformFromSpecialNumber() {
            if (!(arg instanceof Number) || arg instanceof BigDecimal) {
                return null;
            }

            Number number = (Number) arg;
            double d = number.doubleValue();
            String source = null;
            if (Double.isNaN(d)) {
                source = "NaN"; //$NON-NLS-1$
            } else if (d == Double.POSITIVE_INFINITY) {
                if (formatToken.flagAdd) {
                    source = "+Infinity"; //$NON-NLS-1$
                } else if (formatToken.flagSpace) {
                    source = " Infinity"; //$NON-NLS-1$
                } else {
                    source = "Infinity"; //$NON-NLS-1$
                }
            } else if (d == Double.NEGATIVE_INFINITY) {
                if (formatToken.flagParenthesis) {
                    source = "(Infinity)"; //$NON-NLS-1$
                } else {
                    source = "-Infinity"; //$NON-NLS-1$
                }
            } else {
                return null;
            }

            formatToken.setPrecision(FormatToken.UNSET);
            formatToken.flagZero = false;
            return padding(source, 0);
        }

        private CharSequence transformFromNull() {
            formatToken.flagZero = false;
            return padding("null", 0); //$NON-NLS-1$
        }

        /*
         * Transforms a BigInteger to a formatted string.
         */
        private CharSequence transformFromBigInteger() {
            int startIndex = 0;
            boolean isNegative = false;
            StringBuilder result = new StringBuilder();
            BigInteger bigInt = (BigInteger) arg;
            char currentConversionType = formatToken.getConversionType();

            if (formatToken.flagMinus || formatToken.flagZero) {
                if (!formatToken.isWidthSet()) {
                    throw new MissingFormatWidthException(formatToken.getStrFlags());
                }
            }

            // Combination of '+' & ' ' is illegal.
            if (formatToken.flagAdd && formatToken.flagSpace) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags());
            }

            // Combination of '-' & '0' is illegal.
            if (formatToken.flagZero && formatToken.flagMinus) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags());
            }

            formatToken.ensureNoPrecision();

            if ('d' != currentConversionType && formatToken.flagComma) {
                throw new FormatFlagsConversionMismatchException(formatToken.getStrFlags(),
                        currentConversionType);
            }

            if (formatToken.flagSharp && 'd' == currentConversionType) {
                throw new FormatFlagsConversionMismatchException(formatToken.getStrFlags(),
                        currentConversionType);
            }

            if (bigInt == null) {
                return transformFromNull();
            }

            isNegative = (bigInt.compareTo(BigInteger.ZERO) < 0);

            if ('d' == currentConversionType) {
                NumberFormat numberFormat = getNumberFormat();
                numberFormat.setGroupingUsed(formatToken.flagComma);
                result.append(numberFormat.format(bigInt));
            } else if ('o' == currentConversionType) {
                // convert BigInteger to a string presentation using radix 8
                result.append(bigInt.toString(8));
            } else {
                // convert BigInteger to a string presentation using radix 16
                result.append(bigInt.toString(16));
            }
            if (formatToken.flagSharp) {
                startIndex = isNegative ? 1 : 0;
                if ('o' == currentConversionType) {
                    result.insert(startIndex, "0"); //$NON-NLS-1$
                    startIndex += 1;
                } else if ('x' == currentConversionType
                        || 'X' == currentConversionType) {
                    result.insert(startIndex, "0x"); //$NON-NLS-1$
                    startIndex += 2;
                }
            }

            if (!isNegative) {
                if (formatToken.flagAdd) {
                    result.insert(0, '+');
                    startIndex += 1;
                }
                if (formatToken.flagSpace) {
                    result.insert(0, ' ');
                    startIndex += 1;
                }
            }

            /* pad paddingChar to the output */
            if (isNegative && formatToken.flagParenthesis) {
                return wrapParentheses(result);
            }
            if (isNegative && formatToken.flagZero) {
                startIndex++;
            }
            return padding(result, startIndex);
        }

        /*
         * Transforms a Float,Double or BigDecimal to a formatted string.
         */
        private CharSequence transformFromFloat() {
            StringBuilder result = new StringBuilder();
            int startIndex = 0;
            char currentConversionType = formatToken.getConversionType();

            if (formatToken.flagMinus || formatToken.flagZero) {
                if (!formatToken.isWidthSet()) {
                    throw new MissingFormatWidthException(formatToken.getStrFlags());
                }
            }

            if (formatToken.flagAdd && formatToken.flagSpace) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags());
            }

            if (formatToken.flagMinus && formatToken.flagZero) {
                throw new IllegalFormatFlagsException(formatToken.getStrFlags());
            }

            if (currentConversionType == 'e' || currentConversionType == 'E') {
                if (formatToken.flagComma) {
                    throw new FormatFlagsConversionMismatchException(formatToken.getStrFlags(),
                            currentConversionType);
                }
            } else if (currentConversionType == 'g' || currentConversionType == 'G') {
                if (formatToken.flagSharp) {
                    throw new FormatFlagsConversionMismatchException(formatToken.getStrFlags(),
                            currentConversionType);
                }
            } else if (currentConversionType == 'a' || currentConversionType == 'A') {
                if (formatToken.flagComma || formatToken.flagParenthesis) {
                    throw new FormatFlagsConversionMismatchException(formatToken.getStrFlags(),
                            currentConversionType);
                }
            }

            if (null == arg) {
                return transformFromNull();
            }

            if (!(arg instanceof Float || arg instanceof Double || arg instanceof BigDecimal)) {
                throw badArgumentType();
            }

            CharSequence specialNumberResult = transformFromSpecialNumber();
            if (null != specialNumberResult) {
                return specialNumberResult;
            }

            if (currentConversionType != 'a' && currentConversionType != 'A' &&
                    !formatToken.isPrecisionSet()) {
                formatToken.setPrecision(FormatToken.DEFAULT_PRECISION);
            }

            // output result
            DecimalFormatSymbols decimalFormatSymbols = getDecimalFormatSymbols();
            FloatUtil floatUtil = new FloatUtil(result, formatToken,
                    (DecimalFormat) getNumberFormat(), decimalFormatSymbols, arg);
            floatUtil.transform(currentConversionType);

            formatToken.setPrecision(FormatToken.UNSET);

            if (decimalFormatSymbols.getMinusSign() == result.charAt(0)) {
                if (formatToken.flagParenthesis) {
                    return wrapParentheses(result);
                }
            } else {
                if (formatToken.flagSpace) {
                    result.insert(0, ' ');
                    startIndex++;
                }
                if (formatToken.flagAdd) {
                    result.insert(0, floatUtil.getAddSign());
                    startIndex++;
                }
            }

            char firstChar = result.charAt(0);
            if (formatToken.flagZero
                    && (firstChar == floatUtil.getAddSign() || firstChar == decimalFormatSymbols.getMinusSign())) {
                startIndex = 1;
            }

            if (currentConversionType == 'a' || currentConversionType == 'A') {
                startIndex += 2;
            }
            return padding(result, startIndex);
        }

        /*
         * Transforms a Date to a formatted string.
         */
        private CharSequence transformFromDateTime() {
            formatToken.ensureNoPrecision();

            char currentConversionType = formatToken.getConversionType();

            if (formatToken.flagSharp) {
                throw new FormatFlagsConversionMismatchException(formatToken.getStrFlags(),
                        currentConversionType);
            }

            if (formatToken.flagMinus && formatToken.getWidth() == FormatToken.UNSET) {
                throw new MissingFormatWidthException("-" //$NON-NLS-1$
                        + currentConversionType);
            }

            if (null == arg) {
                return transformFromNull();
            }

            Calendar calendar;
            if (arg instanceof Calendar) {
                calendar = (Calendar) arg;
            } else {
                Date date = null;
                if (arg instanceof Long) {
                    date = new Date(((Long) arg).longValue());
                } else if (arg instanceof Date) {
                    date = (Date) arg;
                } else {
                    throw badArgumentType();
                }
                calendar = Calendar.getInstance(locale);
                calendar.setTime(date);
            }

            if (null == dateTimeUtil) {
                dateTimeUtil = new DateTimeUtil(locale);
            }
            StringBuilder result = new StringBuilder();
            // output result
            dateTimeUtil.transform(formatToken, calendar, result);
            return padding(result, 0);
        }
    }

    private static class FloatUtil {
        private final StringBuilder result;
        private final DecimalFormat decimalFormat;
        private final DecimalFormatSymbols decimalFormatSymbols;
        private final FormatToken formatToken;
        private final Object argument;

        FloatUtil(StringBuilder result, FormatToken formatToken, DecimalFormat decimalFormat,
                DecimalFormatSymbols decimalFormatSymbols, Object argument) {
            this.result = result;
            this.formatToken = formatToken;
            this.decimalFormat = decimalFormat;
            this.decimalFormatSymbols = decimalFormatSymbols;
            this.argument = argument;
        }

        void transform(char conversionType) {
            switch (conversionType) {
                case 'e':
                case 'E': {
                    transform_e();
                    break;
                }
                case 'f': {
                    transform_f();
                    break;
                }
                case 'g':
                case 'G': {
                    transform_g();
                    break;
                }
                case 'a':
                case 'A': {
                    transform_a();
                    break;
                }
                default: {
                    throw new UnknownFormatConversionException(String.valueOf(conversionType));
                }
            }
        }

        char getAddSign() {
            return '+';
        }

        void transform_e() {
            StringBuilder pattern = new StringBuilder();
            pattern.append('0');
            if (formatToken.getPrecision() > 0) {
                pattern.append('.');
                char[] zeros = new char[formatToken.getPrecision()];
                Arrays.fill(zeros, '0');
                pattern.append(zeros);
            }
            pattern.append('E');
            pattern.append("+00"); //$NON-NLS-1$
            decimalFormat.applyPattern(pattern.toString());
            String formattedString = decimalFormat.format(argument);
            result.append(formattedString.replace('E', 'e'));

            // if the flag is sharp and decimal separator is always given
            // out.
            if (formatToken.flagSharp && formatToken.getPrecision() == 0) {
                int indexOfE = result.indexOf("e"); //$NON-NLS-1$
                result.insert(indexOfE, decimalFormatSymbols.getDecimalSeparator());
            }
        }

        void transform_g() {
            int precision = formatToken.getPrecision();
            precision = (0 == precision ? 1 : precision);
            formatToken.setPrecision(precision);

            if (0.0 == ((Number) argument).doubleValue()) {
                precision--;
                formatToken.setPrecision(precision);
                transform_f();
                return;
            }

            boolean requireScientificRepresentation = true;
            double d = ((Number) argument).doubleValue();
            d = Math.abs(d);
            if (Double.isInfinite(d)) {
                precision = formatToken.getPrecision();
                precision--;
                formatToken.setPrecision(precision);
                transform_e();
                return;
            }
            BigDecimal b = new BigDecimal(d, new MathContext(precision));
            d = b.doubleValue();
            long l = b.longValue();

            if (d >= 1 && d < Math.pow(10, precision)) {
                if (l < Math.pow(10, precision)) {
                    requireScientificRepresentation = false;
                    precision -= String.valueOf(l).length();
                    precision = precision < 0 ? 0 : precision;
                    l = Math.round(d * Math.pow(10, precision + 1));
                    if (String.valueOf(l).length() <= formatToken
                            .getPrecision()) {
                        precision++;
                    }
                    formatToken.setPrecision(precision);
                }

            } else {
                l = b.movePointRight(4).longValue();
                if (d >= Math.pow(10, -4) && d < 1) {
                    requireScientificRepresentation = false;
                    precision += 4 - String.valueOf(l).length();
                    l = b.movePointRight(precision + 1).longValue();
                    if (String.valueOf(l).length() <= formatToken
                            .getPrecision()) {
                        precision++;
                    }
                    l = b.movePointRight(precision).longValue();
                    if (l >= Math.pow(10, precision - 4)) {
                        formatToken.setPrecision(precision);
                    }
                }
            }
            if (requireScientificRepresentation) {
                precision = formatToken.getPrecision();
                precision--;
                formatToken.setPrecision(precision);
                transform_e();
            } else {
                transform_f();
            }
        }

        void transform_f() {
            // TODO: store a default DecimalFormat we can clone?
            String pattern = "0.000000";
            if (formatToken.flagComma || formatToken.getPrecision() != 6) {
                StringBuilder patternBuilder = new StringBuilder();
                if (formatToken.flagComma) {
                    patternBuilder.append(',');
                    int groupingSize = decimalFormat.getGroupingSize();
                    if (groupingSize > 1) {
                        char[] sharps = new char[groupingSize - 1];
                        Arrays.fill(sharps, '#');
                        patternBuilder.append(sharps);
                    }
                }
                patternBuilder.append(0);
                if (formatToken.getPrecision() > 0) {
                    patternBuilder.append('.');
                    char[] zeros = new char[formatToken.getPrecision()];
                    Arrays.fill(zeros, '0');
                    patternBuilder.append(zeros);
                }
                pattern = patternBuilder.toString();
            }
            // TODO: if DecimalFormat.toPattern was cheap, we could make this cheap (preferably *in* DecimalFormat).
            decimalFormat.applyPattern(pattern);
            result.append(decimalFormat.format(argument));
            // if the flag is sharp and decimal separator is always given
            // out.
            if (formatToken.flagSharp && formatToken.getPrecision() == 0) {
                result.append(decimalFormatSymbols.getDecimalSeparator());
            }
        }

        void transform_a() {
            if (argument instanceof Float) {
                Float F = (Float) argument;
                result.append(Float.toHexString(F.floatValue()));

            } else if (argument instanceof Double) {
                Double D = (Double) argument;
                result.append(Double.toHexString(D.doubleValue()));
            } else {
                // BigInteger is not supported.
                throw new IllegalFormatConversionException(
                        formatToken.getConversionType(), argument.getClass());
            }

            if (!formatToken.isPrecisionSet()) {
                return;
            }

            int precision = formatToken.getPrecision();
            precision = (0 == precision ? 1 : precision);
            int indexOfFirstFractionalDigit = result.indexOf(".") + 1; //$NON-NLS-1$
            int indexOfP = result.indexOf("p"); //$NON-NLS-1$
            int fractionalLength = indexOfP - indexOfFirstFractionalDigit;

            if (fractionalLength == precision) {
                return;
            }

            if (fractionalLength < precision) {
                char zeros[] = new char[precision - fractionalLength];
                Arrays.fill(zeros, '0');
                result.insert(indexOfP, zeros);
                return;
            }
            result.delete(indexOfFirstFractionalDigit + precision, indexOfP);
        }
    }

    private static class DateTimeUtil {
        private Calendar calendar;

        private Locale locale;

        private StringBuilder result;

        private DateFormatSymbols dateFormatSymbols;

        DateTimeUtil(Locale locale) {
            this.locale = locale;
        }

        void transform(FormatToken formatToken, Calendar aCalendar,
                StringBuilder aResult) {
            this.result = aResult;
            this.calendar = aCalendar;
            char suffix = formatToken.getDateSuffix();

            switch (suffix) {
                case 'H': {
                    transform_H();
                    break;
                }
                case 'I': {
                    transform_I();
                    break;
                }
                case 'M': {
                    transform_M();
                    break;
                }
                case 'S': {
                    transform_S();
                    break;
                }
                case 'L': {
                    transform_L();
                    break;
                }
                case 'N': {
                    transform_N();
                    break;
                }
                case 'k': {
                    transform_k();
                    break;
                }
                case 'l': {
                    transform_l();
                    break;
                }
                case 'p': {
                    transform_p(true);
                    break;
                }
                case 's': {
                    transform_s();
                    break;
                }
                case 'z': {
                    transform_z();
                    break;
                }
                case 'Z': {
                    transform_Z();
                    break;
                }
                case 'Q': {
                    transform_Q();
                    break;
                }
                case 'B': {
                    transform_B();
                    break;
                }
                case 'b':
                case 'h': {
                    transform_b();
                    break;
                }
                case 'A': {
                    transform_A();
                    break;
                }
                case 'a': {
                    transform_a();
                    break;
                }
                case 'C': {
                    transform_C();
                    break;
                }
                case 'Y': {
                    transform_Y();
                    break;
                }
                case 'y': {
                    transform_y();
                    break;
                }
                case 'j': {
                    transform_j();
                    break;
                }
                case 'm': {
                    transform_m();
                    break;
                }
                case 'd': {
                    transform_d();
                    break;
                }
                case 'e': {
                    transform_e();
                    break;
                }
                case 'R': {
                    transform_R();
                    break;
                }

                case 'T': {
                    transform_T();
                    break;
                }
                case 'r': {
                    transform_r();
                    break;
                }
                case 'D': {
                    transform_D();
                    break;
                }
                case 'F': {
                    transform_F();
                    break;
                }
                case 'c': {
                    transform_c();
                    break;
                }
                default: {
                    throw new UnknownFormatConversionException(String
                            .valueOf(formatToken.getConversionType())
                            + formatToken.getDateSuffix());
                }
            }
        }

        private void transform_e() {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            result.append(day);
        }

        private void transform_d() {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            result.append(paddingZeros(day, 2));
        }

        private void transform_m() {
            int month = calendar.get(Calendar.MONTH);
            // The returned month starts from zero, which needs to be
            // incremented by 1.
            month++;
            result.append(paddingZeros(month, 2));
        }

        private void transform_j() {
            int day = calendar.get(Calendar.DAY_OF_YEAR);
            result.append(paddingZeros(day, 3));
        }

        private void transform_y() {
            int year = calendar.get(Calendar.YEAR);
            year %= 100;
            result.append(paddingZeros(year, 2));
        }

        private void transform_Y() {
            int year = calendar.get(Calendar.YEAR);
            result.append(paddingZeros(year, 4));
        }

        private void transform_C() {
            int year = calendar.get(Calendar.YEAR);
            year /= 100;
            result.append(paddingZeros(year, 2));
        }

        private void transform_a() {
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            result.append(getDateFormatSymbols().getShortWeekdays()[day]);
        }

        private void transform_A() {
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            result.append(getDateFormatSymbols().getWeekdays()[day]);
        }

        private void transform_b() {
            int month = calendar.get(Calendar.MONTH);
            result.append(getDateFormatSymbols().getShortMonths()[month]);
        }

        private void transform_B() {
            int month = calendar.get(Calendar.MONTH);
            result.append(getDateFormatSymbols().getMonths()[month]);
        }

        private void transform_Q() {
            long milliSeconds = calendar.getTimeInMillis();
            result.append(milliSeconds);
        }

        private void transform_s() {
            long milliSeconds = calendar.getTimeInMillis();
            milliSeconds /= 1000;
            result.append(milliSeconds);
        }

        private void transform_Z() {
            TimeZone timeZone = calendar.getTimeZone();
            result.append(timeZone
                    .getDisplayName(
                            timeZone.inDaylightTime(calendar.getTime()),
                            TimeZone.SHORT, locale));
        }

        private void transform_z() {
            long offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
            char sign = '+';
            if (offset < 0) {
                sign = '-';
                offset = -offset;
            }
            result.append(sign);
            result.append(paddingZeros(offset / 3600000, 2));
            result.append(paddingZeros((offset % 3600000) / 60000, 2));
        }

        private void transform_p(boolean isLowerCase) {
            int i = calendar.get(Calendar.AM_PM);
            String s = getDateFormatSymbols().getAmPmStrings()[i];
            if (isLowerCase) {
                s = s.toLowerCase(locale);
            }
            result.append(s);
        }

        private void transform_N() {
            long nanosecond = calendar.get(Calendar.MILLISECOND) * 1000000L;
            result.append(paddingZeros(nanosecond, 9));
        }

        private void transform_L() {
            int millisecond = calendar.get(Calendar.MILLISECOND);
            result.append(paddingZeros(millisecond, 3));
        }

        private void transform_S() {
            int second = calendar.get(Calendar.SECOND);
            result.append(paddingZeros(second, 2));
        }

        private void transform_M() {
            int minute = calendar.get(Calendar.MINUTE);
            result.append(paddingZeros(minute, 2));
        }

        private void transform_l() {
            int hour = calendar.get(Calendar.HOUR);
            if (0 == hour) {
                hour = 12;
            }
            result.append(hour);
        }

        private void transform_k() {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            result.append(hour);
        }

        private void transform_I() {
            int hour = calendar.get(Calendar.HOUR);
            if (0 == hour) {
                hour = 12;
            }
            result.append(paddingZeros(hour, 2));
        }

        private void transform_H() {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            result.append(paddingZeros(hour, 2));
        }

        private void transform_R() {
            transform_H();
            result.append(':');
            transform_M();
        }

        private void transform_T() {
            transform_H();
            result.append(':');
            transform_M();
            result.append(':');
            transform_S();
        }

        private void transform_r() {
            transform_I();
            result.append(':');
            transform_M();
            result.append(':');
            transform_S();
            result.append(' ');
            transform_p(false);
        }

        private void transform_D() {
            transform_m();
            result.append('/');
            transform_d();
            result.append('/');
            transform_y();
        }

        private void transform_F() {
            transform_Y();
            result.append('-');
            transform_m();
            result.append('-');
            transform_d();
        }

        private void transform_c() {
            transform_a();
            result.append(' ');
            transform_b();
            result.append(' ');
            transform_d();
            result.append(' ');
            transform_T();
            result.append(' ');
            transform_Z();
            result.append(' ');
            transform_Y();
        }

        // TODO: this doesn't need a temporary StringBuilder!
        private static String paddingZeros(long number, int length) {
            int len = length;
            StringBuilder result = new StringBuilder();
            result.append(number);
            int startIndex = 0;
            if (number < 0) {
                len++;
                startIndex = 1;
            }
            len -= result.length();
            if (len > 0) {
                char[] zeros = new char[len];
                Arrays.fill(zeros, '0');
                result.insert(startIndex, zeros);
            }
            return result.toString();
        }

        private DateFormatSymbols getDateFormatSymbols() {
            if (null == dateFormatSymbols) {
                dateFormatSymbols = new DateFormatSymbols(locale);
            }
            return dateFormatSymbols;
        }
    }

    private static class FormatSpecifierParser {
        private String format;
        private int length;

        private int startIndex;
        private int i;

        /**
         * Constructs a new parser for the given format string.
         */
        FormatSpecifierParser(String format) {
            this.format = format;
            this.length = format.length();
        }

        /**
         * Returns a FormatToken representing the format specifier starting at 'offset'.
         * @param offset the first character after the '%'
         */
        FormatToken parseFormatToken(int offset) {
            this.startIndex = offset;
            this.i = offset;
            return parseArgumentIndexAndFlags(new FormatToken());
        }

        /**
         * Returns a string corresponding to the last format specifier that was parsed.
         * Used to construct error messages.
         */
        String getFormatSpecifierText() {
            return format.substring(startIndex, i);
        }

        private int peek() {
            return (i < length) ? format.charAt(i) : -1;
        }

        private char advance() {
            if (i >= length) {
                throw new UnknownFormatConversionException(getFormatSpecifierText());
            }
            return format.charAt(i++);
        }

        private FormatToken parseArgumentIndexAndFlags(FormatToken token) {
            // Parse the argument index, if there is one.
            int position = i;
            int ch = peek();
            if (Character.isDigit(ch)) {
                int number = nextInt();
                if (peek() == '$') {
                    // The number was an argument index.
                    advance(); // Swallow the '$'.
                    if (number == FormatToken.UNSET) {
                        throw new MissingFormatArgumentException(getFormatSpecifierText());
                    }
                    // k$ stands for the argument whose index is k-1 except that
                    // 0$ and 1$ both stand for the first element.
                    token.setArgIndex(Math.max(0, number - 1));
                } else {
                    if (ch == '0') {
                        // The digit zero is a format flag, so reparse it as such.
                        i = position;
                    } else {
                        // The number was a width. This means there are no flags to parse.
                        return parseWidth(token, number);
                    }
                }
            } else if (ch == '<') {
                token.setArgIndex(FormatToken.LAST_ARGUMENT_INDEX);
                advance();
            }

            // Parse the flags.
            while (token.setFlag(peek())) {
                advance();
            }

            // What comes next?
            ch = peek();
            if (Character.isDigit(ch)) {
                return parseWidth(token, nextInt());
            } else if (ch == '.') {
                return parsePrecision(token);
            } else {
                return parseConversionType(token);
            }
        }

        // We pass the width in because in some cases we've already parsed it.
        // (Because of the ambiguity between argument indexes and widths.)
        private FormatToken parseWidth(FormatToken token, int width) {
            token.setWidth(width);
            int ch = peek();
            if (ch == '.') {
                return parsePrecision(token);
            } else {
                return parseConversionType(token);
            }
        }

        private FormatToken parsePrecision(FormatToken token) {
            advance(); // Swallow the '.'.
            int ch = peek();
            if (Character.isDigit(ch)) {
                token.setPrecision(nextInt());
                return parseConversionType(token);
            } else {
                // The precision is required but not given by the format string.
                throw new UnknownFormatConversionException(getFormatSpecifierText());
            }
        }

        private FormatToken parseConversionType(FormatToken token) {
            char conversionType = advance(); // A conversion type is mandatory.
            token.setConversionType(conversionType);
            if (conversionType == 't' || conversionType == 'T') {
                char dateSuffix = advance(); // A date suffix is mandatory for 't' or 'T'.
                token.setDateSuffix(dateSuffix);
            }
            return token;
        }

        // Parses an integer (of arbitrary length, but typically just one digit).
        private int nextInt() {
            long value = 0;
            while (i < length && Character.isDigit(format.charAt(i))) {
                value = 10 * value + (format.charAt(i++) - '0');
                if (value > Integer.MAX_VALUE) {
                    return failNextInt();
                }
            }
            return (int) value;
        }

        // Swallow remaining digits to resync our attempted parse, but return failure.
        private int failNextInt() {
            while (Character.isDigit(peek())) {
                advance();
            }
            return FormatToken.UNSET;
        }
    }
}
