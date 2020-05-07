package org.tta.mobile.util;

import android.os.Bundle;

import androidx.annotation.NonNull;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1Util {
    private static final Logger logger = new Logger(Sha1Util.class);

    /**
     * @param text The plain text to hash.
     * @return SHA1 hash of the given text or the plain text if hashing failed.
     */
    public static String SHA1(@NonNull String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, Sha1Util.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "SHA1");
            parameters.putString(Constants.KEY_DATA, "text = " + text);
            Logger.logCrashlytics(e, parameters);
            logger.error(e);
            return text;
        } catch (NullPointerException e){
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, Sha1Util.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "SHA1");
            parameters.putString(Constants.KEY_DATA, "text = " + text);
            Logger.logCrashlytics(e, parameters);
            logger.error(e);
            return "";
        }
    }

    public static String convertToHex(@NonNull byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                        : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }
}
