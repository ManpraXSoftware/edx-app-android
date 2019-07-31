package org.tta.mobile.tta.utils;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.wordpress_client.model.CustomFilter;
import org.tta.mobile.tta.wordpress_client.model.CustomFilterCache;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;

public class MxHelper {
    public void createToast(Context ctx, String message) {
        Toast toast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
        LinearLayout layout = (LinearLayout) toast.getView();
        if (layout.getChildCount() > 0) {
            TextView tv = (TextView) layout.getChildAt(0);
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }
        toast.show();
    }

    public String getOTPFromMesssageBody(String message_body) {
        if (message_body == null && message_body.equals(""))
            return null;

        String[] breaked_message = message_body.split(" ");
        String otp = breaked_message[breaked_message.length - 1];

        return otp;
    }

    public String getJSONStringfromCustomFilterObj(ArrayList<CustomFilter> custom_filterslist) {
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in String
        String jsonInString = new String();
        try {
            jsonInString = mapper.writeValueAsString(custom_filterslist);
        } catch (JsonProcessingException e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, MxHelper.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "getJSONStringfromCustomFilterObj");
            parameters.putString(Constants.KEY_DATA, "custom_filterslist = " + custom_filterslist);
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }
        return jsonInString;
    }

    public ArrayList<CustomFilter> getCustomFilterObjectFromJson(String json_filter_str) {
        ArrayList<CustomFilter> filter_lst = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        CustomFilter[] obj = new CustomFilter[0];
        //JSON from String to Object
        try {
            obj = mapper.readValue(json_filter_str, CustomFilter[].class);
            filter_lst = new ArrayList<>();

            if (obj != null)
                filter_lst.addAll(new ArrayList<>(Arrays.asList(obj)));
        } catch (IOException e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, MxHelper.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "getCustomFilterObjectFromJson");
            parameters.putString(Constants.KEY_DATA, "json_filter_str = " + json_filter_str);
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }

        return filter_lst;
    }

    //custom filter cached obj
    public String getJSONStringfromCustomFilterCacheObj(ArrayList<CustomFilterCache> custom_filterslist) {
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in String
        String jsonInString = new String();
        try {
            jsonInString = mapper.writeValueAsString(custom_filterslist);
        } catch (JsonProcessingException e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, MxHelper.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "getJSONStringfromCustomFilterCacheObj");
            parameters.putString(Constants.KEY_DATA, "custom_filterslist = " + custom_filterslist);
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }
        return jsonInString;
    }

    public ArrayList<CustomFilterCache> getCustomFilterCacheObjectFromJson(String json_filter_str) {
        ArrayList<CustomFilterCache> cache_filter_lst = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        CustomFilterCache[] obj = new CustomFilterCache[0];
        //JSON from String to Object
        try {
            obj = mapper.readValue(json_filter_str, CustomFilterCache[].class);
            cache_filter_lst = new ArrayList<>();

            if (obj != null)
                cache_filter_lst.addAll(new ArrayList<>(Arrays.asList(obj)));
        } catch (IOException e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, MxHelper.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "getCustomFilterCacheObjectFromJson");
            parameters.putString(Constants.KEY_DATA, "json_filter_str = " + json_filter_str);
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }

        return cache_filter_lst;
    }

    public void showLoadingProgress(ProgressBar progressWheel) {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.VISIBLE);
        }
    }

    public void hideLoadingProgress(ProgressBar progressWheel) {
        if (progressWheel != null) {
            progressWheel.setVisibility(View.GONE);
        }
    }

    public static String decodeUnicode(String encoded) {
        if (encoded == null)
            encoded = "";

        return Html.fromHtml(encoded).toString();
    }

    public static String decodeUTF_string(String encoded) {
        if (encoded == null)
            encoded = "";
        String converted = "";

        try {
            converted = URLDecoder.decode(encoded, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, MxHelper.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "decodeUTF_string");
            parameters.putString(Constants.KEY_DATA, "encoded = " + encoded);
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }

        return converted;

    }
}
