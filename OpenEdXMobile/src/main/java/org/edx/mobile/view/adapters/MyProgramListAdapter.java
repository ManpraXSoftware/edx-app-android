package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.databinding.RowProgramEnrolledItemBinding;
import org.edx.mobile.discovery.model.EnrollAndUnenrollData;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.programs.MyProgramListModel;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyProgramListAdapter extends RecyclerView.Adapter<MyProgramListAdapter.ProgramViewHolder> {
    private Context context;
    private List<MyProgramListModel> myProgramList;
    private OnRecyclerItemClickListener listener;
    private int count = 0;

    @Inject
    LoginPrefs loginPrefs;

    public MyProgramListAdapter(Context context, OnRecyclerItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

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
        holder.itemBinding.programName.setText(model.getProgramName());


        holder.itemBinding.unenrollButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                holder.itemBinding.unenrollButton.setEnabled(false);
                String strings = "course-v1:VisionEmpower+VE_TIK_G10_MATH_P2_CH11+2021,course-v1:VisionEmpower+VE_TIK_G10_MATH_P2_CH12+2021,course-v1:VisionEmpower+VE_TIK_MATH_G10_P1_CH07+2021,course-v1:VisionEmpower+VE_TIK_MATH_G10_P2_CH09+2021,course-v1:VisionEmpower+VE_TIK_MATH_G10_P2_CH10+2021,course-v1:VisionEmpower+VE_TIK_MATH_G10_P2_CH13+2021,course-v1:VisionEmpower+VE_TIK_M_G10_P1_CH08+2020,course-v1:VisionEmpower+VE_TIK_M_G10_P1_CH1+2021,course-v1:VisionEmpower+VE_TIK_M_G10_P1_CH2+2021,course-v1:VisionEmpower+VE_TIK_M_G10_P1_CH3+2021,course-v1:VisionEmpower+VE_TIK_M_G10_P1_CH6+2021,course-v1:VisionEmpower+VE_TIK_M_G10_P2-CH15+2021,course-v1:VisionEmpower+VE_TIK_M_G10_P2_CH14+2021,course-v1:VisionEmpower+VE_TIK_M_G9_P1_CH04+2020,course-v1:VisionEmpower+VE_TIK_M_G9_P1_CH05+2020";
                EnrollAndUnenrollData.DataCreation dataCreation = new EnrollAndUnenrollData.DataCreation();
                dataCreation.setCourses(strings);
                dataCreation.setAction("unenroll");
                dataCreation.setProgram_uuid(model.getProgramUUid());
                dataCreation.setProgram_name(model.getProgramName());
                listener.onItemClick(view,dataCreation);

            }
        });
        holder.itemBinding.tagCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, model);
            }
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
