//package org.edx.mobile.view.grid;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
//public class ChatBotGridAdapter extends BaseAdapter {
//
//    private Context context;
//    private final String[] items;
//
//    public ChatBotGridAdapter(Context context, String[] items) {
//        this.context = context;
//        this.items = items;
//    }
//
//    @Override
//    public int getCount() {
//        return items.length;
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return items[position];
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = inflater.inflate(R.layout.grid_item, null);
//        }
//        TextView textView = convertView.findViewById(R.id.textView);
//
//        textView.setText(items[position]);
//
//        return convertView;
//    }
//}
