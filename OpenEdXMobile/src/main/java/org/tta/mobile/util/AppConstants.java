package org.tta.mobile.util;

public enum AppConstants {
    ;

    @Deprecated // This is not a constant. Should move it to the activity and use savedInstanceState.
    public static boolean videoListDeleteMode = false;

    public static final String VIDEO_FORMAT_M3U8 = ".m3u8";
    public static final String VIDEO_FORMAT_MP4 = ".mp4";

    /**
     * This class defines the names of various directories which are used for
     * storing application data.
     */
    public static final class Directories {
        /**
         * The name of the directory which is used to store downloaded videos.
         */
        public static final String VIDEOS = "videos";
        /**
         * The name of the directory which is used to store subtitles of the
         * downloaded videos.
         */
        public static final String SUBTITLES = "subtitles";
    }
}
