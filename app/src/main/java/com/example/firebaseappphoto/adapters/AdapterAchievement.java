package com.example.firebaseappphoto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseappphoto.R;
import com.example.firebaseappphoto.models.ModelAchievement;

import java.util.List;

public class AdapterAchievement extends RecyclerView.Adapter<AdapterAchievement.MyHolder>{

    private final Context context;
    private final List<ModelAchievement> achievements;

    public AdapterAchievement(Context context, List<ModelAchievement> achievements) {
        this.context = context;
        this.achievements = achievements;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_achievement, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String name = achievements.get(position).getName();
        boolean isAchieved = achievements.get(position).isAchieved();
        holder.tvName.setText(name);
        if(isAchieved) {
            holder.ivImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_achievement_golden));
        }
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{
        private final ImageView ivImage;
        private final TextView tvName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }
}
