package org.edx.mobile.view.adapters;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.databinding.RowProgramBinding;
import org.edx.mobile.discovery.model.ProgramResponseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class
ProgramModelAdapter extends RecyclerView.Adapter<ProgramModelAdapter.ProgramViewHolder> {

    private Context context;
    private OnRecyclerItemClickListener listener;
    private List<ProgramResponseModel.Program> programResultLists;
    private String progamNameselect;
    ClipboardService clipboardService;
    public ProgramModelAdapter(Context context, OnRecyclerItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        clipboardService = ClipboardServiceHolder.getClipboardService(context.getApplicationContext());
        return new ProgramModelAdapter.ProgramViewHolder(RowProgramBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        final ProgramResponseModel.Program model = programResultLists.get(position);
        holder.itemBinding.programName.setText(model.getTitle());
        if (progamNameselect != null) {
            if (progamNameselect.equals(holder.itemBinding.programName.getText().toString())) {
                holder.itemBinding.programName.setSelected(true);
            } else {
                holder.itemBinding.programName.setSelected(false);
            }
        }
        if (holder.itemBinding.programName.isSelected()) {
            listener.onItemClick(holder.itemBinding.programName, model);
        }
        holder.itemBinding.programName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!holder.itemBinding.programName.isSelected()) {
                    progamNameselect = holder.itemBinding.programName.getText().toString();
                    notifyDataSetChanged();
                }
            }
        });
        holder.itemBinding.programName.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                String textToCopy = holder.itemBinding.programName.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });


    }

    public void setPrograms(List<ProgramResponseModel.Program> programResultLists, String selectedProgram) {
        this.programResultLists =programResultLists;
        this.progamNameselect = selectedProgram;
        // Optional: Add logging to track the final list
        for (int i = 0; i < this.programResultLists.size(); i++) {
            ProgramResponseModel.Program program = this.programResultLists.get(i);
            Log.d("ProgramAdapter", i + " Program: " + program.getTitle() + ", Language: " + program.getProgramLanguage());
        }

        notifyDataSetChanged();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<ProgramResponseModel.Program> sortProgramsByLanguage(List<ProgramResponseModel.Program> programs, String currentLanguage) {
        // Remove locale suffix if present
        if (currentLanguage.contains("-IN")) {
            currentLanguage = currentLanguage.substring(0, currentLanguage.length() - 3);
        }

        // Group programs by language
        Map<String, List<ProgramResponseModel.Program>> groupedByLanguage = programs.stream()
                .collect(Collectors.groupingBy(ProgramResponseModel.Program::getProgramLanguage));

        // Prepare result list to maintain order
        List<ProgramResponseModel.Program> sortedPrograms = new ArrayList<>();

        // If selected language is English
        if (currentLanguage.equals("en")) {
            // 1. Add only English programs first
            List<ProgramResponseModel.Program> englishPrograms = groupedByLanguage.getOrDefault("en", new ArrayList<>());
            sortedPrograms.addAll(englishPrograms);

            // 2. Add programs in other languages
            List<String> otherLanguages = groupedByLanguage.keySet().stream()
                    .filter(lang -> !lang.equals("en"))
                    .sorted() // Sort other languages alphabetically
                    .collect(Collectors.toList());

            for (String language : otherLanguages) {
                sortedPrograms.addAll(groupedByLanguage.get(language));
            }
        }
        // If selected language is not English
        else {
            // 1. Add programs in user's selected language
            List<ProgramResponseModel.Program> selectedLanguagePrograms = groupedByLanguage.getOrDefault(currentLanguage, new ArrayList<>());
            sortedPrograms.addAll(selectedLanguagePrograms);

            // 2. Add English language programs
            List<ProgramResponseModel.Program> englishPrograms = groupedByLanguage.getOrDefault("en", new ArrayList<>());
            sortedPrograms.addAll(englishPrograms);

            // 3. Add programs in other languages (excluding selected language and English)
            String finalCurrentLanguage = currentLanguage;
            List<String> otherLanguages = groupedByLanguage.keySet().stream()
                    .filter(lang -> !lang.equals(finalCurrentLanguage) && !lang.equals("en"))
                    .sorted() // Sort other languages alphabetically
                    .collect(Collectors.toList());

            for (String language : otherLanguages) {
                sortedPrograms.addAll(groupedByLanguage.get(language));
            }
        }

        System.out.println("Current Language: " + currentLanguage);
        System.out.println("Grouped Languages: " + groupedByLanguage.keySet());
        System.out.println("Sorted Programs: " + sortedPrograms.stream()
                .map(p -> p.getProgramLanguage())
                .collect(Collectors.toList()));
        int i=0;
        for (ProgramResponseModel.Program program : sortedPrograms) {
            System.out.println(i+" sortedPrograms "+program.getTitle());
            System.out.println("sortedPrograms "+program.getLanguage());
            System.out.println("sortedPrograms "+program.getProgramLanguage());
            i++;
        }

        return sortedPrograms;
    }

    public void setProgramEnroll(boolean enroll,String programSelectedUid){
//        if (programSelectedUid!=null){
//            for (ProgramResponseModel.Program programResultList : programResultLists){
//                if (programResultList.getUuid().equals(programSelectedUid)){
//                    //programResultList.setProgramEnroll(enroll);
//                }
//            }
//        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return programResultLists == null ? 0 : programResultLists.size();
    }

    public class ProgramViewHolder extends RecyclerView.ViewHolder {
        private RowProgramBinding itemBinding;

        public ProgramViewHolder(RowProgramBinding rowProgramBinding) {
            super(rowProgramBinding.getRoot());
            this.itemBinding = rowProgramBinding;

        }
    }

}
