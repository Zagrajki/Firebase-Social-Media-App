package com.example.firebaseappphoto.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseappphoto.MessagesActivity;
import com.example.firebaseappphoto.R;
import com.example.firebaseappphoto.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private final Context context;
    private final List<ModelUser> users;
    private final HashMap<String, String> latestMessMap;

    public AdapterChat(Context context, List<ModelUser> users) {
        this.context = context;
        this.users = users;
        latestMessMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_chat, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        if (users.get(position).getStatus().equals("online")){
            holder.ivStatus.setImageResource(R.drawable.status_online);
        }
        else {
            holder.ivStatus.setImageResource(R.drawable.status_offline);
        }
        try {
            Picasso.get().load(users.get(position).getProfilePhoto()).into(holder.ivProfilePhoto);
        }
        catch (Exception e){
            holder.ivProfilePhoto.setImageResource(R.drawable.ic_person_dark);
        }
        holder.tvName.setText(users.get(position).getName());
        holder.tvLatestMess.setText(latestMessMap.get(users.get(position).getUid()));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessagesActivity.class);
            intent.putExtra("uid", users.get(position).getUid());
            context.startActivity(intent);
        });

    }

    public void setLatestMessMap(String userId, String latestMess){
        latestMessMap.put(userId, latestMess);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{
        private final ImageView ivProfilePhoto, ivStatus;
        private final TextView tvLatestMess, tvName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePhoto = itemView.findViewById(R.id.ivProfilePhoto);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvLatestMess = itemView.findViewById(R.id.tvLatestMess);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
