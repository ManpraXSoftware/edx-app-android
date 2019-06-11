package org.tta.mobile.tta.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class UrlUtil {

    public static String urldecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("decode error ", e.toString());
        }
        return null;
    }

}
