package org.tta.mobile.util;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;

public class TextUtils {
    private TextUtils() {
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param delimiter The delimiter to use while joining.
     * @param tokens    An array of {@link CharSequence} to be joined using the delimiter.
     * @return A {@link CharSequence} joined using the provided delimiter.
     */
    public static CharSequence join(CharSequence delimiter, Iterable<CharSequence> tokens) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        boolean firstTime = true;
        for (CharSequence token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb;
    }

    /**
     * Returns displayable styled text from the provided HTML string.
     * <br/>
     * Note: Also handles the case when a String has multiple HTML entities that translate to one
     * character e.g. {@literal &amp;#39;} which is essentially an apostrophe that should normally
     * occur as {@literal &#39;}
     *
     * @param html The source string having HTML content.
     * @return Formatted HTML.
     */
    @NonNull
    public static Spanned formatHtml(@NonNull String html) {
        final String REGEX = "(&#?[a-zA-Z0-9]+;)";
        final Pattern PATTERN = Pattern.compile(REGEX);

        Spanned formattedHtml = new SpannedString(html);
        String previousHtml = null;

        // Break the loop if there isn't an HTML entity in the text or when all the HTML entities
        // have been decoded. Also break the loop in the special case when a String having the
        // same format as an HTML entity is left but it isn't essentially a decodable HTML entity
        // e.g. &#asdfasd;
        while (PATTERN.matcher(html).find() && !html.equals(previousHtml)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                formattedHtml = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
            } else {
                formattedHtml = Html.fromHtml(html);
            }
            previousHtml = html;
            html = formattedHtml.toString();
        }

        return formattedHtml;
    }
}
