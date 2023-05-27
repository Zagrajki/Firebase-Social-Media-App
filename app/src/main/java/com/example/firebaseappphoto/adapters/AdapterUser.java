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

import com.example.firebaseappphoto.SomeonesProfileActivity;
import com.example.firebaseappphoto.models.ModelUser;
import com.example.firebaseappphoto.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder>{

    private final Context context;
    private final List<ModelUser> users;

    public AdapterUser(Context context, List<ModelUser> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_user, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        try {
            Picasso.get().load(users.get(position).getProfilePhoto()).into(holder.ivProfilePhoto);
        } catch (Exception e) {
            holder.ivProfilePhoto.setImageResource(R.drawable.ic_person_dark);
        }
        holder.tvName.setText(users.get(position).getName());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SomeonesProfileActivity.class);
            intent.putExtra("uid", users.get(position).getUid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{
        private final ImageView ivProfilePhoto;
        private final TextView tvName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePhoto = itemView.findViewById(R.id.ivProfilePhoto);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
