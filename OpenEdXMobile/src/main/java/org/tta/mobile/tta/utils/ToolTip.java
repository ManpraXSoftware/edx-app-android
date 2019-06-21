package org.tta.mobile.tta.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.Gravity;

import org.tta.mobile.R;

public class ToolTip {
    @StringRes
    private final int textResourceId;
    @Nullable
    private final CharSequence text;
    private final int textGravity;
    private final int textColor;
    private final float textSize;
    private final Typeface typeface;
    private final int typefaceStyle;
    private final int lines;
    private final int backgroundColor;
    private final int leftPadding;
    private final int rightPadding;
    private final int topPadding;
    private final int bottomPadding;
    private final float radius;

    private ToolTip(@StringRes int textResourceId, @Nullable CharSequence text, int textGravity,
                    int textColor, float textSize, Typeface typeface, int typefaceStyle,
                    int lines, int backgroundColor, int leftPadding, int rightPadding,
                    int topPadding, int bottomPadding, float radius) {
        this.textResourceId = textResourceId;
        this.text = text;
        this.textGravity = textGravity;
        this.textColor = textColor;
        this.textSize = textSize;
        this.typeface = typeface;
        this.typefaceStyle = typefaceStyle;
        this.lines = lines;
        this.backgroundColor = backgroundColor;
        this.leftPadding = leftPadding;
        this.rightPadding = rightPadding;
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        this.radius = radius;
    }

    @StringRes
    public int getTextResourceId() {
        return textResourceId;
    }

    @Nullable
    public CharSequence getText() {
        return text;
    }

    public int getTextGravity() {
        return textGravity;
    }

    @ColorInt
    public int getTextColor() {
        return textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    @NonNull
    public Typeface getTypeface() {
        return typeface;
    }

    public int getTypefaceStyle() {
        return typefaceStyle;
    }

    public int getLines() {
        return lines;
    }

    @ColorInt
    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getLeftPadding() {
        return leftPadding;
    }

    public int getRightPadding() {
        return rightPadding;
    }

    public int getTopPadding() {
        return topPadding;
    }

    public int getBottomPadding() {
        return bottomPadding;
    }

    public float getCornerRadius() {
        return radius;
    }

    /**
     * Used to build a tool tip.
     */
    public static class Builder {
        @StringRes
        private int textResourceId = 0;
        private CharSequence text;
        private int textGravity = Gravity.NO_GRAVITY;
        private int textColor = R.color.primary_cyan;
        private float textSize = 80.0F;
        private Typeface typeface = Typeface.SANS_SERIF;
        private int typefaceStyle = Typeface.NORMAL;
        private int lines = 0;
        private int backgroundColor = R.color.gray_3;
        private int leftPadding = 5;
        private int rightPadding = 5;
        private int topPadding = 5;
        private int bottomPadding = 5;
        private float radius = 0.0F;
//        private Drawable drawable = ContextCompat.getDrawable(, R.drawable.ic_arrow_back_blue_400_24dp)

        /**
         * Creates a new builder.
         */
        public Builder() {
        }

        /**
         * Sets the text of the tool tip. If both the resource ID and the char sequence are set, the
         * char sequence will be used.
         */
        public Builder withText(@StringRes int text) {
            this.textResourceId = text;
            return this;
        }

        /**
         * Sets the text of the tool tip. If both the resource ID and the char sequence are set, the
         * char sequence will be used.
         */
        public Builder withText(CharSequence text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the text gravity of the tool tip. The default value is {@link Gravity#NO_GRAVITY}.
         */
        public Builder withTextGravity(int gravity) {
            this.textGravity = gravity;
            return this;
        }

        /**
         * Sets the text color for the tool tip. The default color is white.
         */
        public Builder withTextColor(@ColorInt int textColor) {
            this.textColor = textColor;
            return this;
        }

        /**
         * Sets the text size in pixel for the tool tip. The default size is 13.
         */
        public Builder withTextSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        /**
         * Sets the typeface for the tool tip. The default value is {@link Typeface#DEFAULT}.
         */
        public Builder withTypeface(Typeface typeface) {
            if (typeface != null) {
                this.typeface = typeface;
            }
            return this;
        }

        /**
         * Sets the typeface style for the tool tip. The default value is {@link Typeface#NORMAL}.
         */
        public Builder withTypefaceStyle(int style) {
            this.typefaceStyle = style;
            return this;
        }

        /**
         * Sets the exact lines for the tool tip. The default value is unset.
         * */
        public Builder withLines(int lines) {
            this.lines = lines;
            return this;
        }

        /**
         * Sets the background color for the tool tip. The default color is black.
         */
        public Builder withBackgroundColor(@ColorInt int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * Sets the padding in pixel for the tool tip. The default padding is 0.
         */
        public Builder withPadding(int leftPadding, int rightPadding, int topPadding, int bottomPadding) {
            this.leftPadding = leftPadding;
            this.rightPadding = rightPadding;
            this.topPadding = topPadding;
            this.bottomPadding = bottomPadding;
            return this;
        }

        /**
         * Sets the corner radius in pixel for the tool tip. The default value is 0.
         */
        public Builder withCornerRadius(float radius) {
            this.radius = radius;
            return this;
        }

//        public Builder withDrawable(Drawable drawable) {
//            this.drawable = drawable;
//            return this;
//        }

        /**
         * Creates a tool tip.
         */
        public ToolTip build() {
            return new ToolTip(textResourceId, text, textGravity, textColor, textSize, typeface,
                    typefaceStyle, lines, backgroundColor, leftPadding, rightPadding, topPadding,
                    bottomPadding, radius);
        }
    }
}