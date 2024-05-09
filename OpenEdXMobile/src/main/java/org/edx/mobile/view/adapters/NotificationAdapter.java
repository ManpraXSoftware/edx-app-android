package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.interfaces.OnNavigateListener;
import org.edx.mobile.programs.NotificationModel;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationModel.NotificationData>  notificationsList = new ArrayList<>();
    OnNavigateListener onNavigateListener;

    Context context;

    public NotificationAdapter(Context context, OnNavigateListener onNavigateListener) {
        this.context = context;
        this.onNavigateListener=onNavigateListener;
    }
    public void setNotifications(List<NotificationModel.NotificationData>  newNotifications) {
        notificationsList.clear();
        notificationsList.addAll(newNotifications);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel.NotificationData  notification = notificationsList.get(position);
        holder.notificationTitleTextView.setText(notification.getTitle());
        holder.notificationDescriptionTextView.setText(notification.getMessage());

        String notificationCardString=notification.getTitle()+" "+notification.getMessage();

        holder.notificationCardCardView.setContentDescription(notificationCardString);

        holder.notificationCardCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavigateListener.navigateToAnotherScreen(notification);
            }
        });

        /*holder.notificationTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavigateListener.navigateToAnotherScreen(notification);
            }
        });
        holder.notificationDescriptionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavigateListener.navigateToAnotherScreen(notification);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        private TextView notificationTitleTextView;
        private TextView notificationDescriptionTextView;

        private CardView notificationCardCardView;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTitleTextView = itemView.findViewById(R.id.notificationTitleTextView);
            notificationDescriptionTextView=itemView.findViewById(R.id.notificationDescriptionTextView);
            notificationCardCardView=itemView.findViewById(R.id.notification_card);
        }

        void bind(String notification) {
            notificationTitleTextView.setText(notification);
        }
    }
}