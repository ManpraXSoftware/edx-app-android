package org.tta.mobile.logger;

import android.os.Bundle;

import java.io.Serializable;

import de.greenrobot.event.EventBus;

public class Logger implements Serializable {

    private final String tag;

    public Logger(Class<?> cls) {
        this.tag = cls.getName();
    }

    public Logger(String tag) {
        this.tag = tag;
    }

    /**
     * Prints the stack trace for the given throwable instance for debug build.
     *
     * @param ex
     */
    public void error(Throwable ex) {
        error(ex, false);
    }

    /**
     * Prints the stack trace for the given throwable instance for debug build.
     * Also, submits the crash report if submitCrashReport is true.
     *
     * @param ex
     * @param submitCrashReport
     */
    public void error(Throwable ex, boolean submitCrashReport) {
        LogUtil.error(this.tag, "", ex);

        if (submitCrashReport) {
            EventBus.getDefault().post(new CrashReportEvent(ex, null));
        }
    }

    public void warn(String log) {
        LogUtil.warn(this.tag, log);
    }

    public void debug(String log) {
        LogUtil.debug(this.tag, log);
    }

    public static void logCrashlytics(Throwable ex, Bundle parameters){
        EventBus.getDefault().post(new CrashReportEvent(ex, parameters));
    }

    public static class CrashReportEvent {
        private final Throwable error;
        private final Bundle parameters;

        public CrashReportEvent(Throwable error, Bundle parameters) {
            this.error = error;
            this.parameters = parameters;
        }

        public Throwable getError() {
            return error;
        }

        public Bundle getParameters() {
            return parameters;
        }
    }
}
