package org.tta.mobile.tta.utils;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import org.tta.mobile.R;
import org.tta.mobile.module.registration.model.RegistrationOption;
import org.tta.mobile.module.registration.view.RegistrationOptionSpinner;
import org.tta.mobile.tta.ui.custom.FormEditText;
import org.tta.mobile.tta.ui.custom.FormMultiSpinner;
import org.tta.mobile.tta.ui.custom.FormSpinner;

import java.util.List;

public class ViewUtil {

    public static TextView addHeading(ViewGroup parent, String heading){
        TextView tv = new TextView(parent.getContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(
                (int) parent.getContext().getResources().getDimension(R.dimen._40px),
                (int) parent.getContext().getResources().getDimension(R.dimen._50px),
                (int) parent.getContext().getResources().getDimension(R.dimen._40px),
                0);
        tv.setLayoutParams(params);
        tv.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.primary_navy));
        tv.setTextSize(20);
        tv.setTypeface(ResourcesCompat.getFont(parent.getContext(), R.font.hind_medium));
        tv.setText(heading);
        parent.addView(tv);
        return tv;
    }

    public static TextView addSubHeading(ViewGroup parent, String subHeading){
        TextView tv = new TextView(parent.getContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(
                (int) parent.getContext().getResources().getDimension(R.dimen._40px),
                (int) parent.getContext().getResources().getDimension(R.dimen._16px),
                (int) parent.getContext().getResources().getDimension(R.dimen._40px),
                0);
        tv.setLayoutParams(params);
        tv.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.primary_navy));
        tv.setTextSize(15);
        tv.setTypeface(ResourcesCompat.getFont(parent.getContext(), R.font.hind_medium));
        tv.setText(subHeading);
        parent.addView(tv);
        return tv;
    }

    public static View addEmptySpace(ViewGroup parent, int height){
        View view = new View(parent.getContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, height);
        view.setLayoutParams(params);
        parent.addView(view);
        return view;
    }

    public static FormEditText addFormEditText(ViewGroup parent, String hint){
        FormEditText editText = new FormEditText(parent.getContext());
        editText.setHint(hint);
        parent.addView(editText);
        return editText;
    }

    public static Button addButton(ViewGroup parent, String text){
        Button button = new Button(parent.getContext());
        LayoutParams params = new LayoutParams(
                (int) parent.getContext().getResources().getDimension(R.dimen.btn_width),
                (int) parent.getContext().getResources().getDimension(R.dimen.btn_height)
        );
        params.topMargin = (int) parent.getContext().getResources().getDimension(R.dimen._40px);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        button.setLayoutParams(params);
        button.setGravity(Gravity.CENTER);
        button.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.white));
        button.setTypeface(ResourcesCompat.getFont(parent.getContext(), R.font.hind_semibold));
        button.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.btn_selector_filled));
        button.setText(text);
        parent.addView(button);
        return button;
    }

    public static FormSpinner addOptionSpinner(ViewGroup parent, String label, @NonNull List<RegistrationOption> options, @Nullable RegistrationOption defaultOption){
        FormSpinner spinner = new FormSpinner(parent.getContext());
        spinner.setLabel(label);
        spinner.setItems(options, defaultOption);
        parent.addView(spinner);
        return spinner;
    }

    public static FormMultiSpinner addMultiOptionSpinner(ViewGroup parent, String label, @NonNull List<RegistrationOption> options, @Nullable List<RegistrationOption> defaultOptions){
        FormMultiSpinner spinner = new FormMultiSpinner(parent.getContext());
        spinner.setLabel(label);
        spinner.setItems(options, defaultOptions);
        parent.addView(spinner);
        return spinner;
    }

    public static TextView addLinkText(ViewGroup parent, String linkText){
        TextView tv = new TextView(parent.getContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(
                (int) parent.getContext().getResources().getDimension(R.dimen._40px),
                (int) parent.getContext().getResources().getDimension(R.dimen._16px),
                (int) parent.getContext().getResources().getDimension(R.dimen._40px),
                0);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        tv.setLayoutParams(params);
        tv.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.secondary_blue));
        tv.setTextSize(12);
        tv.setTypeface(ResourcesCompat.getFont(parent.getContext(), R.font.hind_medium));
        tv.setText(linkText);
        tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        parent.addView(tv);
        return tv;
    }

}
