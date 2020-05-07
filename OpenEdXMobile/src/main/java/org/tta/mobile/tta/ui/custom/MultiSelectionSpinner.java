package org.tta.mobile.tta.ui.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatSpinner;

import org.tta.mobile.R;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MultiSelectionSpinner extends AppCompatSpinner implements
        OnMultiChoiceClickListener {

    String[] _items = null;
    boolean[] mSelection = null;
    boolean[] mSelectionAtStart = null;
    String _itemsAtStart = null;
    ArrayAdapter<String> simple_adapter;
    private OnMultipleItemsSelectedListener listener;
    private String title;

    private int mLimit = -1;
    private int count = 0;

    public MultiSelectionSpinner(Context context) {
        super(context);

        simple_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(simple_adapter);
    }

    public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        simple_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(simple_adapter);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setListener(OnMultipleItemsSelectedListener listener) {
        this.listener = listener;
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

        if (mSelection != null && which < mSelection.length) {

            if (isChecked) {
                count++;
            } else {
                count--;
            }

            mSelection[which] = isChecked;
            simple_adapter.clear();
            simple_adapter.add(buildSelectedItemString());

        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }

    @Override
    public boolean performClick() {
        _itemsAtStart = getSelectedItemsAsString();

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMultiChoiceItems(_items, mSelection, this)
                .setPositiveButton(getContext().getString(R.string.submit), null)
                .setNegativeButton(getContext().getString(R.string.cancel), null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button pBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            pBtn.setOnClickListener(view1 -> {
                System.arraycopy(mSelection, 0, mSelectionAtStart, 0, mSelection.length);
                listener.selectedIndices(getSelectedIndices());
                listener.selectedStrings(getSelectedStrings());

                if (mLimit == -1 || count <= mLimit) {
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(),
                            String.format(getContext().getString(R.string.selection_limit_crossed), mLimit),
                            Toast.LENGTH_LONG).show();
                }
            });

            Button nBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            nBtn.setOnClickListener(view1 -> {
                simple_adapter.clear();
                simple_adapter.add(_itemsAtStart);
                System.arraycopy(mSelectionAtStart, 0, mSelection, 0, mSelectionAtStart.length);
                dialog.dismiss();
            });
        });

        dialog.setOnDismissListener(dialogInterface -> {
            simple_adapter.clear();
            simple_adapter.add(_itemsAtStart);
            System.arraycopy(mSelectionAtStart, 0, mSelection, 0, mSelectionAtStart.length);
            count = 0;
            for (boolean b: mSelectionAtStart){
                if (b){
                    count++;
                }
            }
        });

        dialog.show();
        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setItems(String[] items) {
        _items = items;
        mSelection = new boolean[_items.length];
        mSelectionAtStart = new boolean[_items.length];
        simple_adapter.clear();
        Arrays.fill(mSelection, false);
    }

    public void setItems(List<String> items) {
        _items = items.toArray(new String[items.size()]);
        mSelection = new boolean[_items.length];
        mSelectionAtStart = new boolean[_items.length];
        simple_adapter.clear();
        Arrays.fill(mSelection, false);
    }

    public void setSelection(String[] selection) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
            mSelectionAtStart[i] = false;
        }
        for (String cell : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(cell)) {
                    mSelection[j] = true;
                    mSelectionAtStart[j] = true;
                    count++;
                }
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelection(List<String> selection) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
            mSelectionAtStart[i] = false;
        }
        for (String sel : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(sel)) {
                    mSelection[j] = true;
                    mSelectionAtStart[j] = true;
                    count++;
                }
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelection(int index) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
            mSelectionAtStart[i] = false;
        }
        if (index >= 0 && index < mSelection.length) {
            mSelection[index] = true;
            mSelectionAtStart[index] = true;
            count++;
        } else {
            throw new IllegalArgumentException("Index " + index
                    + " is out of bounds.");
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelection(int[] selectedIndices) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
            mSelectionAtStart[i] = false;
        }
        for (int index : selectedIndices) {
            if (index >= 0 && index < mSelection.length) {
                mSelection[index] = true;
                mSelectionAtStart[index] = true;
                count++;
            } else {
                throw new IllegalArgumentException("Index " + index
                        + " is out of bounds.");
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelectionLimit(int limit) {
        mLimit = limit;
    }

    public List<String> getSelectedStrings() {
        List<String> selection = new LinkedList<>();
        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                selection.add(_items[i]);
            }
        }
        return selection;
    }

    public List<Integer> getSelectedIndices() {
        List<Integer> selection = new LinkedList<>();
        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                selection.add(i);
            }
        }
        return selection;
    }

    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;

                sb.append(_items[i]);
            }
        }
        return sb.toString();
    }

    public String getSelectedItemsAsString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                sb.append(_items[i]);
            }
        }
        return sb.toString();
    }

    public interface OnMultipleItemsSelectedListener {
        void selectedIndices(List<Integer> indices);

        void selectedStrings(List<String> strings);
    }
}