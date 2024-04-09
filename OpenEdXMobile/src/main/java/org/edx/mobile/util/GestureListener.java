package org.edx.mobile.util;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.interfaces.ActivityProvider;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.programs.MyProgramListModel;
import org.edx.mobile.view.MainBottomDashboardFragment;
import org.edx.mobile.view.NewProgramFragment;

import androidx.annotation.NonNull;

import static org.edx.mobile.view.ProgramActivity.MYPROGRAMFLAG;
import static org.edx.mobile.view.ProgramActivity.PROGRAM;
import static org.edx.mobile.view.ProgramActivity.PROGRAM_CONVERTED;
import static org.edx.mobile.view.ProgramActivity.PROGRAM_UUID;
public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private TextView textView;
    private Object object;
    private Context context;

    OnNavigateListener onNavigateListener;

    private long startClickTime; // Track click start time for accurate detection
    private static final int CLICK_DURATION_THRESHOLD = 3000; // Adjust based on UI responsiveness

    public GestureListener(TextView textView,Object object, Context context, OnNavigateListener onNavigateListener) {
        this.textView = textView;
        this.object = object;
        this.context = context;
        this.onNavigateListener=onNavigateListener;
      //  this.activityProvider = activityProvider;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        startClickTime = System.currentTimeMillis(); // Capture click start time
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // No action needed here
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // Handle double tap event
        Log.d("GestureDetector", "Double tap detected!");
        return true;
    }



    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        long clickDuration = System.currentTimeMillis() - startClickTime;


        onNavigateListener.navigateToAnotherScreen(object);
        return true;
    }




    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // Cancel the click if there's significant movement (scroll)
        startClickTime = 0; // Reset click start time
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // Handle long press event
        String textToCopy = textView.getText().toString();
        ClipboardServiceHolder.getClipboardService(context).copyText(textToCopy);
        Toast.makeText(context, R.string.text_copied, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // No action needed here
        return true;
    }
}
