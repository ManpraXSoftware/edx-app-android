package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.databinding.RowProgramBinding;
import org.edx.mobile.discovery.model.ProgramResponseModel;

import java.util.List;

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
        this.programResultLists = programResultLists;
        this.progamNameselect = selectedProgram;
        notifyDataSetChanged();
    }
    public void setProgramEnroll(boolean enroll,String programSelectedUid){
        if (programSelectedUid!=null){
            for (ProgramResponseModel.Program programResultList : programResultLists){
                if (programResultList.getUuid().equals(programSelectedUid)){
                    //programResultList.setProgramEnroll(enroll);
                }
            }
        }
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
