package com.example.firebaseappphoto.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseappphoto.PostActivity;
import com.example.firebaseappphoto.ProfileOpinionsActivity;
import com.example.firebaseappphoto.R;
import com.example.firebaseappphoto.models.ModelNotification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotification> {

    private final Context context;
    private final ArrayList<ModelNotification> notifications;
    private final HashMap<DatabaseReference, ValueEventListener> vels;

    public AdapterNotification(Context context, ArrayList<ModelNotification> notifications) {
        this.context = context;
        this.notifications = notifications;
        this.vels = new HashMap<>();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        vels.forEach(Query::removeEventListener);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_notification, parent, false);
        return new HolderNotification(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderNotification holder, int position) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(notifications.get(position).getUid());
        vels.put(userRef, userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Picasso.get().load((String)snapshot.child("profilePhoto").getValue()).into(holder.ivProfilePhoto);
                }
                catch (Exception e){
                    holder.ivProfilePhoto.setImageResource(R.drawable.ic_person_dark);
                }
                String notification = (String)snapshot.child("name").getValue()+" "+notifications.get(position).getNotification();
                holder.tvNotificationsText.setText(notification);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(notifications.get(position).getTimestamp()));
        String time = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        holder.tvTimestamp.setText(time);
        holder.itemView.setOnClickListener(v -> {
            switch(notifications.get(position).getType()){
                case "post":
                    Intent intentPost = new Intent(context, PostActivity.class);
                    intentPost.putExtra("pid", notifications.get(position).getPidOrUid());
                    context.startActivity(intentPost);
                    break;
                case "user":
                    Intent intentUser = new Intent(context, ProfileOpinionsActivity.class);
                    intentUser.putExtra("uid", notifications.get(position).getPidOrUid());
                    context.startActivity(intentUser);
                    break;
                default:
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            userRef.child("Notifications").child(notifications.get(position).getNid()).removeValue();
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class HolderNotification extends RecyclerView.ViewHolder {
        private final ImageView ivProfilePhoto;
        private final TextView tvNotificationsText, tvTimestamp;
        public HolderNotification(@NonNull View itemView) {
            super(itemView);
            ivProfilePhoto = itemView.findViewById(R.id.ivProfilePhoto);
            tvNotificationsText = itemView.findViewById(R.id.tvNotificationsText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
