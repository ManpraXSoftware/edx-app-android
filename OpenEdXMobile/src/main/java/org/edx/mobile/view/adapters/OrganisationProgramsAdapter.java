package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.databinding.ItemOrganisationProgramBinding;
import org.edx.mobile.discovery.model.AuthoringOrganisations;
import org.edx.mobile.discovery.model.ProgramResultList;
import org.edx.mobile.programs.MyProgramListModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrganisationProgramsAdapter extends RecyclerView.Adapter<OrganisationProgramsAdapter.ProgramViewHolder> {

    private final Context context;
    private final OnRecyclerItemClickListener<ProgramResultList> listener;
    private final String fallbackOrganisationName;
    private List<ProgramResultList> programs = new ArrayList<>();
    private final List<Integer> colorPalette = new ArrayList<>();
    private int nextColorIndex = 0;
    private int count = 0;

    public OrganisationProgramsAdapter(@NonNull Context context,
                                       @NonNull OnRecyclerItemClickListener<ProgramResultList> listener,
                                       @NonNull String fallbackOrganisationName) {
        this.context = context;
        this.listener = listener;
        this.fallbackOrganisationName = fallbackOrganisationName;
        String[] colorValues = context.getResources().getStringArray(R.array.subject_colors_name);
        for (String colorValue : colorValues) {
            try {
                colorPalette.add(Color.parseColor(colorValue));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed color entries
            }
        }
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrganisationProgramBinding binding = ItemOrganisationProgramBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProgramViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        ProgramResultList program = programs.get(position);
        holder.bind(program);
    }

    @Override
    public int getItemCount() {
        return programs == null ? 0 : programs.size();
    }

    public void setPrograms(@NonNull List<ProgramResultList> programs) {
        this.programs = programs;
        nextColorIndex = 0;
        notifyDataSetChanged();
    }

    class ProgramViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrganisationProgramBinding binding;

        ProgramViewHolder(@NonNull ItemOrganisationProgramBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull ProgramResultList program) {
            String[] colorsTxt = context.getResources().getStringArray(R.array.subject_colors_name);
            List<Integer> colors = new ArrayList<Integer>();
            for (int i = 0; i < colorsTxt.length; i++) {
                int newColor = Color.parseColor(colorsTxt[i]);
                colors.add(newColor);
            }
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
            binding.programColorCode.setBackground(gradientDrawable);

            binding.programName.setText(getProgramTitle(program));
            binding.organisationName.setText(getOrganisationName(program));
            binding.programLanguage.setText(getCourseCountLabel(program));
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, program);
                }
            });
        }

        private String getProgramTitle(@NonNull ProgramResultList program) {
            if (!TextUtils.isEmpty(program.getConverted_title())) {
                return program.getConverted_title();
            }
            return program.getTitle();
        }

        private String getOrganisationName(@NonNull ProgramResultList program) {
            if (program.getAuthoring_organizations() != null && !program.getAuthoring_organizations().isEmpty()) {
                List<String> names = new ArrayList<>();
                for (AuthoringOrganisations author : program.getAuthoring_organizations()) {
                    if (!TextUtils.isEmpty(author.getName())) {
                        names.add(author.getName());
                    }
                }
                if (!names.isEmpty()) {
                    return TextUtils.join(", ", names);
                }
            }
            return fallbackOrganisationName;
        }

        private String getCourseCountLabel(@NonNull ProgramResultList program) {
            int count = program.getCourses() == null ? 0 : program.getCourses().size();
            return context.getResources().getQuantityString(R.plurals.organisation_program_course_count, count, count);
        }

        private Drawable createColorStripDrawable() {
            Drawable baseDrawable = ContextCompat.getDrawable(context, R.drawable.tags_side_background);
            if (baseDrawable instanceof LayerDrawable) {
                LayerDrawable layerDrawable = (LayerDrawable) baseDrawable.mutate();
                Drawable gradient = layerDrawable.findDrawableByLayerId(R.id.gradientDrawble);
                if (gradient instanceof GradientDrawable) {
                    ((GradientDrawable) gradient).setColor(pickNextColor());
                }
                return layerDrawable;
            }
            return baseDrawable;
        }

        private int pickNextColor() {
            if (colorPalette.isEmpty()) {
                return ContextCompat.getColor(context, R.color.organisation_color);
            }
            int color = colorPalette.get(nextColorIndex);
            nextColorIndex = (nextColorIndex + 1) % colorPalette.size();
            return color;
        }
    }
}

