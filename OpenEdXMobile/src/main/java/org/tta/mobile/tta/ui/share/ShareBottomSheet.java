package org.tta.mobile.tta.ui.share;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.tta.mobile.R;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.utils.AppUtil;
import org.tta.mobile.tta.utils.Tools;
import org.tta.mobile.util.ResourceUtil;
import org.tta.mobile.util.images.ShareUtils;

import java.util.HashMap;
import java.util.Map;

public class ShareBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = ShareBottomSheet.class.getCanonicalName();

    private String title, message, image, link;
    private ShareUtils.ShareMenuItemListener listener;

    public static ShareBottomSheet newInstance(String title, String message, String image, String link,
                                               ShareUtils.ShareMenuItemListener listener){
        ShareBottomSheet sheet = new ShareBottomSheet();
        sheet.title = title;
        sheet.message = message;
        sheet.image = image;
        sheet.link = link;
        sheet.listener = listener;
        return sheet;
    }

    //Bottom Sheet Callback
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View contentView = View.inflate(getContext(), R.layout.t_fragment_feed_share, null);

        contentView.findViewById(R.id.ivClose).setOnClickListener(v -> dismiss());
        ((TextView) contentView.findViewById(R.id.feed_title)).setText(title);
        ((TextView) contentView.findViewById(R.id.feed_meta_text)).setText(message);

        Glide.with(getContext())
                .load(image)
                .placeholder(R.drawable.placeholder_course_card_image)
                .into((ImageView) contentView.findViewById(R.id.feed_content_image));

        LinearLayout shareOptionsLayout = contentView.findViewById(R.id.feed_share_options_layout);
        addTTAOption(shareOptionsLayout);
        addFbOption(shareOptionsLayout);
        addWhatsappOption(shareOptionsLayout);
        addClipboardOption(shareOptionsLayout);

        dialog.setContentView(contentView);

        //Set the coordinator layout behavior
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        //Set callback
        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

    }

    private String getShareString(){
        Map<String, CharSequence> map = new HashMap<>();
        map.put("source_title", title);
        map.put("platform_name", getString(R.string.platform_name));
        map.put("content_name", message);
        return ResourceUtil.getFormattedString(getResources(), R.string.share_message, map)
                .toString() + "\n" + link;
    }

    private void addTTAOption(LinearLayout optionsLayout){

        ImageView imageView = addShareOption(optionsLayout);
        imageView.setImageResource(R.drawable.tta_launcher_icon);
        imageView.setOnClickListener(v -> {
            if (listener != null){
                listener.onMenuItemClick(null, ShareUtils.ShareType.TTA);
            }
        });

    }

    private void addFbOption(LinearLayout optionsLayout){
        if (AppUtil.appInstalledOrNot("com.facebook.katana", getActivity().getPackageManager())){
            ImageView imageView = addShareOption(optionsLayout);
            Drawable d = null;
            try {
                d = getActivity().getPackageManager().getActivityIcon(
                        new Intent().setAction(Intent.ACTION_SEND).setType("text/plain")
                                .setPackage("com.facebook.katana"));
            } catch (PackageManager.NameNotFoundException e) {
                Bundle parameters = new Bundle();
                parameters.putString(Constants.KEY_CLASS_NAME, ShareBottomSheet.class.getName());
                parameters.putString(Constants.KEY_FUNCTION_NAME, "addFbOption");
                parameters.putString(Constants.KEY_DATA, "Link = " + link +
                        ", package = com.facebook.katana");
                Logger.logCrashlytics(e, parameters);
                e.printStackTrace();
            }
            imageView.setImageDrawable(d);
            imageView.setOnClickListener(v -> {
                if (!AppUtil.appInstalledOrNot("com.facebook.katana", getActivity().getPackageManager())){
                    Toast.makeText(getActivity(), "App is not installed", Toast.LENGTH_LONG).show();
                    return;
                }
                if (listener != null){
                    listener.onMenuItemClick(null, ShareUtils.ShareType.FACEBOOK);
                }

                Intent intent = ShareUtils.newShareIntent(getShareString());
                intent.setPackage("com.facebook.katana");
                getActivity().startActivity(intent);
            });
        } else if (AppUtil.appInstalledOrNot("com.facebook.lite", getActivity().getPackageManager())){
            ImageView imageView = addShareOption(optionsLayout);
            Drawable d = null;
            try {
                d = getActivity().getPackageManager().getActivityIcon(
                        new Intent().setAction(Intent.ACTION_SEND).setType("text/plain")
                                .setPackage("com.facebook.lite"));
            } catch (PackageManager.NameNotFoundException e) {
                Bundle parameters = new Bundle();
                parameters.putString(Constants.KEY_CLASS_NAME, ShareBottomSheet.class.getName());
                parameters.putString(Constants.KEY_FUNCTION_NAME, "addFbOption");
                parameters.putString(Constants.KEY_DATA, "Link = " + link +
                        ", package = com.facebook.lite");
                Logger.logCrashlytics(e, parameters);
                e.printStackTrace();
            }
            imageView.setImageDrawable(d);
            imageView.setOnClickListener(v -> {
                if (!AppUtil.appInstalledOrNot("com.facebook.lite", getActivity().getPackageManager())){
                    Toast.makeText(getActivity(), "App is not installed", Toast.LENGTH_LONG).show();
                    return;
                }
                if (listener != null){
                    listener.onMenuItemClick(null, ShareUtils.ShareType.FACEBOOK);
                }

                Intent intent = ShareUtils.newShareIntent(getShareString());
                intent.setPackage("com.facebook.lite");
                getActivity().startActivity(intent);
            });
        }
    }

    private void addWhatsappOption(LinearLayout optionsLayout){
        if (AppUtil.appInstalledOrNot("com.whatsapp", getActivity().getPackageManager())){
            ImageView imageView = addShareOption(optionsLayout);
            Drawable d = null;
            try {
                d = getActivity().getPackageManager().getActivityIcon(
                        new Intent().setAction(Intent.ACTION_SEND).setType("text/plain")
                                .setPackage("com.whatsapp"));
            } catch (PackageManager.NameNotFoundException e) {
                Bundle parameters = new Bundle();
                parameters.putString(Constants.KEY_CLASS_NAME, ShareBottomSheet.class.getName());
                parameters.putString(Constants.KEY_FUNCTION_NAME, "addWhatsappOption");
                parameters.putString(Constants.KEY_DATA, "Link = " + link +
                        ", package = com.whatsapp");
                Logger.logCrashlytics(e, parameters);
                e.printStackTrace();
            }
            imageView.setImageDrawable(d);
            imageView.setOnClickListener(v -> {
                if (!AppUtil.appInstalledOrNot("com.whatsapp", getActivity().getPackageManager())){
                    Toast.makeText(getActivity(), "App is not installed", Toast.LENGTH_LONG).show();
                    return;
                }
                if (listener != null){
                    listener.onMenuItemClick(null, ShareUtils.ShareType.WHATSAPP);
                }

                Intent intent = ShareUtils.newShareIntent(getShareString());
                intent.setPackage("com.whatsapp");
                getActivity().startActivity(intent);
            });
        }
    }

    private void addClipboardOption(LinearLayout optionsLayout){
        ImageView imageView = addShareOption(optionsLayout);
        imageView.setImageResource(R.drawable.t_icon_link);
        imageView.setOnClickListener(v -> {
            if (listener != null){
                listener.onMenuItemClick(null, ShareUtils.ShareType.UNKNOWN);
            }

            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("TTA share", link);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getActivity(), getString(R.string.share_clipboard), Toast.LENGTH_LONG).show();
        });
    }

    private ImageView addShareOption(LinearLayout layout){
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Tools.dp2px(getActivity(), 24), 1);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setBackgroundResource(R.drawable.light_selectable_box_overlay);
        imageView.setClickable(true);
        imageView.setFocusable(true);
        layout.addView(imageView);
        return imageView;
    }
}
