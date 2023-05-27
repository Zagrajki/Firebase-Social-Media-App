package com.example.firebaseappphoto.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseappphoto.R;
import com.example.firebaseappphoto.models.ModelMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.MyHolder>{

    private static final int MY_MESSAGE = 0;
    private static final int THEIR_MESSAGE = 1;
    private final Context context;
    private final List<ModelMessage> messages;

    public AdapterMessage(Context context, List<ModelMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = viewType == MY_MESSAGE ?
                LayoutInflater.from(context).inflate(R.layout.one_my_message, parent, false) :
                LayoutInflater.from(context).inflate(R.layout.one_their_message, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        if (position==messages.size()-1 && messages.get(position).getSender().equals(FirebaseAuth.getInstance().getUid())) {
            if (messages.get(position).isSeen()) {
                holder.tvSeen.setText(String.format("%s","Seen"));
            }
            else {
                holder.tvSeen.setText(String.format("%s","Delivered"));
            }
        }
        else {
            holder.tvSeen.setVisibility(View.GONE);
        }
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(messages.get(position).getTimestamp()));
        String time = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        holder.tvTime.setText(time);
        if (messages.get(position).getType().equals("text")){
            holder.ivMessageContent.setVisibility(View.GONE);
            holder.tvMessageContent.setVisibility(View.VISIBLE);
            holder.tvMessageContent.setText(messages.get(position).getMessage());
        }
        else {
            holder.ivMessageContent.setVisibility(View.VISIBLE);
            holder.tvMessageContent.setVisibility(View.GONE);
            try {
                Picasso.get().load(messages.get(position).getMessage()).into(holder.ivMessageContent);
            }
            catch (Exception e){
                holder.ivMessageContent.setImageResource(R.drawable.ic_image_black);
            }
        }
        holder.lMessage.setOnLongClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("Messages").orderByChild("timestamp").equalTo(messages.get(position).getTimestamp())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                if (Objects.equals(ds.child("sender").getValue(), FirebaseAuth.getInstance().getUid())) {
                                    new AlertDialog.Builder(context).setMessage("Are you sure to delete this message?")
                                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                            .setPositiveButton("Delete", (dialog, which) -> {
                                                if(messages.get(position).getType().equals("photo")){
                                                    FirebaseStorage.getInstance().getReferenceFromUrl(messages.get(position).getMessage()).delete();
                                                }
                                                ds.getRef().removeValue();
                                            })
                                            .create().show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSender().equals(FirebaseAuth.getInstance().getUid())) {
            return MY_MESSAGE;
        }
        else {
            return THEIR_MESSAGE;
        }
    }

    static class MyHolder extends RecyclerView.ViewHolder{
        private final ImageView ivMessageContent;
        private final TextView tvMessageContent, tvSeen, tvTime;
        private final LinearLayout lMessage;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            ivMessageContent = itemView.findViewById(R.id.ivMessageContent);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvSeen = itemView.findViewById(R.id.tvSeen);
            tvTime = itemView.findViewById(R.id.tvTime);
            lMessage = itemView.findViewById(R.id.lMessage);
        }
    }
}
