package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.inject.Inject;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.edx.mobile.R;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.player.TranscriptListener;
import org.edx.mobile.player.TranscriptManager;
import org.edx.mobile.util.LocaleUtils;
import org.edx.mobile.view.adapters.transcript.TranscriptAdapter;
import org.jetbrains.annotations.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import subtitleFile.Caption;
import subtitleFile.TimedTextObject;


public class CourseUnitYoutubePlayerFragment extends CourseUnitFragment implements TranscriptAdapter.OnTranscriptClickListener {

    private static final String ARG_VIDEO_ID = "video_id";

    private YouTubePlayerView youtubePlayerView;
    private YouTubePlayer youTubePlayer;
    private TimedTextObject subtitlesObj;

    private String videoId;

    @Inject
    private TranscriptManager transcriptManager;

    public static CourseUnitYoutubePlayerFragment newInstance(VideoBlockModel videoBlockModel) {
        CourseUnitYoutubePlayerFragment fragment = new CourseUnitYoutubePlayerFragment();
        Bundle args = new Bundle();
        final Uri uri = Uri.parse(videoBlockModel.getData().encodedVideos.getYoutubeVideoInfo().url);
        final String videoId = uri.getQueryParameter("v");

        args.putSerializable(Router.EXTRA_COURSE_UNIT, videoBlockModel);
        args.putString(ARG_VIDEO_ID, videoId);
        fragment.setArguments(args);
        return fragment;
    }
    VideoBlockModel unit;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            videoId = getArguments().getString(ARG_VIDEO_ID);
            unit = getArguments() == null ? null :
                    (VideoBlockModel) getArguments().getSerializable(Router.EXTRA_COURSE_UNIT);            //parseTranscript(transcriptJson);
        }
    }
    private View fullscreenViewRef;
    protected RecyclerView transcriptListView;
    protected TranscriptAdapter transcriptAdapter;
    private AccessibilityManager accessibilityManager;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_unit_youtube_player, container, false);
        onInit(view);
        return view;
    }
    private long lastScrollUpdateTime = 0;
    private static final long SCROLL_THROTTLE_INTERVAL_MS = 800; // delay
    void onInit(View view){
        youtubePlayerView = view.findViewById(R.id.youtube_player_view);
        transcriptListView = view.findViewById(R.id.transcript_recycler);

        IFramePlayerOptions iFramePlayerOptions = new IFramePlayerOptions.Builder()
                .controls(1)
                // enable full screen button
                .fullscreen(1)
                .build();

        accessibilityManager = (AccessibilityManager) getActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);


        youtubePlayerView.addFullscreenListener(new FullscreenListener() {
            @Override
            public void onEnterFullscreen(View fullscreenView, @NonNull Function0<Unit> exitFullscreen) {
                fullscreenViewRef = fullscreenView;
                ((ViewGroup) requireActivity().getWindow().getDecorView()).addView(fullscreenView);
                // Set landscape orientation
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                View decorView = requireActivity().getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
            }

            @Override
            public void onExitFullscreen() {
                // Remove fullscreen view
                ((ViewGroup) requireActivity().getWindow().getDecorView()).removeView(fullscreenViewRef);
                // Restore portrait orientation
                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });

        YouTubePlayerListener listener = new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NotNull YouTubePlayer player) {
                youTubePlayer = player;
                player.cueVideo(videoId, 0f);
            }
            @Override
            public void onCurrentSecond(@NotNull YouTubePlayer player, float second) {
                //try {
                    long currentTime = System.currentTimeMillis();
                    if (subtitlesObj!=null && currentTime - lastScrollUpdateTime > SCROLL_THROTTLE_INTERVAL_MS) {
                        lastScrollUpdateTime = currentTime;
                        int index = getCaptionIndexAtTime(second);
                        if (index != -1) {
                            scrollToCaption(index);
                        }
                    }
              //  }catch (Exception e){}
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                if (/*state == PlayerConstants.PlayerState.UNSTARTED || */state == PlayerConstants.PlayerState.PLAYING) {
                    if(accessibilityManager != null && accessibilityManager.isEnabled() && isFirstTime) {
                        isFirstTime = false;
                        youTubePlayer.pause();
                        new android.os.Handler().postDelayed(() -> {
                            if (youTubePlayer != null) {
                                interruptTalkBackSpeech();
                                clearTalkBackAnnouncements();
                                youTubePlayer.play();
                            }
                        }, 2000);
                    }
                   // setYoutubePlayerAccessibility(false);
                    //Log.d("YouTube", "Play button was pressed or video started automatically");
                } else if (state == PlayerConstants.PlayerState.PAUSED || state == PlayerConstants.PlayerState.ENDED) {
                   // setYoutubePlayerAccessibility(true);
                    //Log.d("YouTube", "Video paused");

                }
            }

        };

        youtubePlayerView.setEnableAutomaticInitialization(false);
        youtubePlayerView.initialize(listener, iFramePlayerOptions);


        getLifecycle().addObserver(youtubePlayerView);
        downloadTranscript();

        youtubePlayerView.addYouTubePlayerListener(listener);
    }
    boolean isFirstTime = true;
    private int getCaptionIndexAtTime(float timeInSeconds) {
        List<Caption> captions = subtitleList;
        for (int i = 0; i < captions.size(); i++) {
            Caption caption = captions.get(i);
            if (caption.start.getMseconds() <= (int)(timeInSeconds * 1000) && (caption.end == null || caption.end.getMseconds() > (int)(timeInSeconds * 1000))) {
                return i;
            }
        }
        return -1;
    }

    private void scrollToCaption(int index) {
        if (index >= 0 && index < transcriptAdapter.getItemCount()) {
            transcriptAdapter.select(index);

            RecyclerView.LayoutManager layoutManager = transcriptListView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(index, transcriptListView.getHeight() / 2);
            } else {
                transcriptListView.scrollToPosition(index);
            }
        }
    }

    protected void initTranscriptListView() {
        transcriptAdapter = new TranscriptAdapter(getContext(), environment, this);
        transcriptListView.setLayoutManager(new LinearLayoutManager(getContext()));
        transcriptListView.setAdapter(transcriptAdapter);
    }


    @Override
    public void onPause() {
        super.onPause();
        if(youTubePlayer!=null)
          youTubePlayer.pause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Fragment became visible
        } else {
            if(youTubePlayer!=null)
                youTubePlayer.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        youtubePlayerView.release();
    }

    public void downloadTranscript() {
        Activity activity = getActivity();
        if (activity != null) {
            TranscriptModel transcript = getTranscriptModel();
            String transcriptUrl = LocaleUtils.getTranscriptURL(activity, transcript);
            transcriptManager.downloadTranscriptsForVideo(transcriptUrl, (TimedTextObject transcriptTimedTextObject) -> {
                subtitlesObj = transcriptTimedTextObject;
                if (!activity.isDestroyed()) {
                    initTranscripts();
                }
            });
        }
    }

    List<Caption> subtitleList;
    private void initTranscripts() {
        if (subtitlesObj != null) {
            initTranscriptListView();
            Collection<Caption> subtitles = subtitlesObj.captions.values();
            subtitleList = new ArrayList<>(subtitles);

            transcriptAdapter.setItems(subtitleList);
            String subtitleLanguage = LocaleUtils.getCurrentDeviceLanguage(getActivity());
            if (!android.text.TextUtils.isEmpty(subtitleLanguage) &&
                    getTranscriptModel().entrySet().contains(subtitleLanguage)) {
                // loginPrefs.setSubtitleLanguage(subtitleLanguage);
            }
            // showClosedCaptionData(subtitlesObj);
        }
    }

    protected TranscriptModel getTranscriptModel() {
        TranscriptModel transcript = null;
        if (unit != null && unit.getData() != null &&
                unit.getData().transcripts != null) {
            transcript = unit.getData().transcripts;
        }
        return transcript;
    }

    @Override
    public void onTranscriptClicked(int position, Caption caption) {
        if (youTubePlayer != null && caption.start != null) {
            float seekTime = caption.start.getMseconds() / 1000f; // convert ms to seconds
            youTubePlayer.seekTo(seekTime);
            transcriptAdapter.select(position);
            transcriptListView.scrollToPosition(position);
        }
    }

    private void setYoutubePlayerAccessibility(boolean enable) {
        if (youtubePlayerView != null) {
            youtubePlayerView.setImportantForAccessibility(
                    enable ? View.IMPORTANT_FOR_ACCESSIBILITY_YES : View.IMPORTANT_FOR_ACCESSIBILITY_NO
            );
        }
    }

    private void interruptTalkBackSpeech() {
        if (accessibilityManager != null && accessibilityManager.isEnabled()) {
            try {
                // Send an INTERRUPT event to stop current TalkBack speech
                AccessibilityEvent interruptEvent = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                interruptEvent.setEventType(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);

                // Set the event source to the YouTube player view
                if (youtubePlayerView != null) {
                    interruptEvent.setSource(youtubePlayerView);
                    interruptEvent.setClassName(youtubePlayerView.getClass().getName());
                    interruptEvent.setPackageName(getActivity().getPackageName());

                    // Send the interrupt event
                    accessibilityManager.sendAccessibilityEvent(interruptEvent);

                    // Also try to interrupt using another method
                    accessibilityManager.interrupt();
                }
            } catch (Exception e) {
                Log.e("TalkBack", "Error interrupting TalkBack speech: " + e.getMessage());

                try {
                    if (youtubePlayerView != null) {
                        youtubePlayerView.clearFocus();
                        youtubePlayerView.requestFocus();
                    }
                } catch (Exception fallbackException) {
                    Log.e("TalkBack", "Fallback interrupt method also failed: " + fallbackException.getMessage());
                }
            }
        }
    }

    private void clearTalkBackAnnouncements() {
        if (youtubePlayerView != null) {
            youtubePlayerView.setContentDescription("");

            youtubePlayerView.postDelayed(() -> {
                if (youTubePlayer != null) {
                    youtubePlayerView.setContentDescription("Video Player");
                }
            }, 100);
        }
    }


}

