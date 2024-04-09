package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.comparator.TalkBackDetector.CustomAccessibilityDelegate;
import org.edx.mobile.R;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.databinding.RowProgramEnrolledItemBinding;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.programs.MyProgramListModel;
import org.edx.mobile.util.GestureListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyProgramListAdapter extends RecyclerView.Adapter<MyProgramListAdapter.ProgramViewHolder> {
    private Context context;
    private List<MyProgramListModel> myProgramList;
    private OnRecyclerItemClickListener listener;

    private ClipboardService clipboardService ;
    private int count = 0;
    GestureListener gestureListener1,gestureListener2;

    OnNavigateListener onNavigateListener;

    public MyProgramListAdapter(Context context, OnRecyclerItemClickListener listener,OnNavigateListener onNavigateListener){
        this.listener = listener;
        this.onNavigateListener=onNavigateListener;
        this.context=context;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        clipboardService = ClipboardServiceHolder.getClipboardService(context);

        return new MyProgramListAdapter.ProgramViewHolder(RowProgramEnrolledItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        final MyProgramListModel model = myProgramList.get(position);
        String[] colorsTxt = context.getResources().getStringArray(R.array.subject_colors_name);
        List<Integer> colors = new ArrayList<Integer>();
        for (int i = 0; i < colorsTxt.length; i++) {
            int newColor = Color.parseColor(colorsTxt[i]);
            colors.add(newColor);
        }
        int rand = new Random().nextInt(colors.size());
        Integer color;
        if (count<=9){
            color = colors.get(count);
            count++;
        }else{
            count = 0;
            color = colors.get(count);
            count++;
        }
        LayerDrawable layerDrawable = (LayerDrawable) context.getResources()
                .getDrawable(R.drawable.tags_side_background);
        GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable
                .findDrawableByLayerId(R.id.gradientDrawble);
        gradientDrawable.setColor(color);
        holder.itemBinding.programColorCode.setBackground(gradientDrawable);
        holder.itemBinding.tagsName.setText(model.getTagName());
        holder.itemBinding.tagsName.setFocusable(true);
        holder.itemBinding.tagsName.setClickable(true);
       // holder.itemBinding.tagsName.setLongClickable(false);
        holder.itemBinding.programName.setText(model.getProgramName());
        holder.itemBinding.programName.setFocusable(true);
        holder.itemBinding.programName.setClickable(true);
        //holder.itemBinding.tagsName.setLongClickable(false);
       holder.itemBinding.tagCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });
      /*  holder.itemBinding.tagsName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });

        holder.itemBinding.programName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
        });*/


       /* holder.itemBinding.tagsName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                String textToCopy = holder.itemBinding.tagsName.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(context, context.getString(R.string.text_copied), Toast.LENGTH_SHORT).show();

                return true;
            }
        });

       /* holder.itemBinding.programName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                String textToCopy = holder.itemBinding.programName.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(context, context.getString(R.string.text_copied), Toast.LENGTH_SHORT).show();

                return true;
            }
        });*/
        /*setCopyOnLongPressListener(holder.itemBinding.tagsName);
        setCopyOnLongPressListener(holder.itemBinding.programName);*/

        /*holder.itemBinding.tagsName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Delegate the touch event to the gesture detector
                // Create a gesture listener
                boolean flag=gestureDetector1.onTouchEvent(event);


                return flag;
            }
        });

        holder.itemBinding.programName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean flag=gestureDetector2.onTouchEvent(event);


                return flag;
            }
        });*/

        setGestureListeners(holder.itemBinding.tagsName, model);
        setGestureListeners(holder.itemBinding.programName, model);
    }
    private void setGestureListeners(TextView textView,Object object) {
        ViewCompat.setAccessibilityDelegate(textView, new CustomAccessibilityDelegate(object,onNavigateListener));
        GestureListener gestureListener = new GestureListener(textView,object,context,onNavigateListener);
        GestureDetector gestureDetector = new GestureDetector(context, gestureListener);
        textView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private static final long LONG_CLICK_DURATION = 3000; // Adjust as needed


    private void setCopyOnLongPressListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private Handler handler = new Handler();
            private boolean isLongClick = false;
            private long downTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downTime = System.currentTimeMillis();
                        handler.postDelayed(longClickRunnable, LONG_CLICK_DURATION);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        handler.removeCallbacks(longClickRunnable);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isLongClick) {
                            long elapsedTime = System.currentTimeMillis() - downTime;
                            if (elapsedTime < LONG_CLICK_DURATION) {
                                // Handle tap here
                                String textToCopy = ((TextView) v).getText().toString();
                                clipboardService.copyText(textToCopy);
                                Toast.makeText(v.getContext(), R.string.text_copied, Toast.LENGTH_SHORT).show();
                            }

                        }

                        isLongClick = false;
                        handler.removeCallbacks(longClickRunnable);
                        break;
                }
                return true;
            }

            private Runnable longClickRunnable = new Runnable() {
                @Override
                public void run() {
                    isLongClick = true;
                }
            };
        });
    }



    public void setMyProgramList(List<MyProgramListModel> myProgramList) {
        this.myProgramList = myProgramList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return myProgramList == null ? 0 : myProgramList.size();
    }

    public class ProgramViewHolder extends RecyclerView.ViewHolder {
        private RowProgramEnrolledItemBinding itemBinding;

        public ProgramViewHolder(RowProgramEnrolledItemBinding rowProgramEnrolledItemBinding) {
            super(rowProgramEnrolledItemBinding.getRoot());
            this.itemBinding = rowProgramEnrolledItemBinding;

        }
    }


}
