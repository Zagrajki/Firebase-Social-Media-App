package com.example.firebaseappphoto.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseappphoto.R;
import com.example.firebaseappphoto.models.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.MyHolder> {

    private final Context context;
    private final List<ModelComment> comments;
    private final String myUid, targetId, type;
    private final HashMap<DatabaseReference, ValueEventListener> vels;

    public AdapterComment(Context context, List<ModelComment> comments, String myUid, String targetId, String type) {
        this.context = context;
        this.comments = comments;
        this.myUid = myUid;
        this.targetId = targetId;
        this.type = type;
        this.vels = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_comment, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        vels.forEach(Query::removeEventListener);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(comments.get(position).getUid());
        vels.put(userRef, userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Picasso.get().load((String)snapshot.child("profilePhoto").getValue()).into(holder.ivProfilePhoto);
                }
                catch (Exception e){
                    holder.ivProfilePhoto.setImageResource(R.drawable.ic_person_dark);
                }
                String name = (String)snapshot.child("name").getValue();
                holder.tvName.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
        holder.tvComment.setText(comments.get(position).getComment());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(comments.get(position).getTimestamp()));
        String time = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        holder.tvTimestamp.setText(time);
        holder.itemView.setOnLongClickListener(v -> {
            if (myUid.equals(comments.get(position).getUid())){
                new AlertDialog.Builder(v.getRootView().getContext()).setMessage("Are you sure to delete this comment?")
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Yes", (dialog, which) -> deleteComment(comments.get(position).getCid()))
                        .create().show();
            }
            else {
                Toast.makeText(context, "You can delete only your comments.", Toast.LENGTH_SHORT).show();
            }
            return false;
        });
    }

    private void deleteComment(String cid) {
        DatabaseReference ref;
        switch(type){
            case "post":
                ref = FirebaseDatabase.getInstance().getReference("Posts").child(targetId);
                break;
            case "user":
                ref = FirebaseDatabase.getInstance().getReference("Users").child(targetId);
                break;
            default:
                return;
        }
        ref.child("Comments").child(cid).removeValue().addOnSuccessListener(aVoid ->
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String decrementedCountComments = String.format("%s", Integer.parseInt(String.format("%s",
                        snapshot.child("countComments").getValue())) - 1);
                ref.child("countComments").setValue(decrementedCountComments);
                updateUserStats(-1, "countCommented");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
    }

    private void updateUserStats(int change, String stat){
        DatabaseReference userAccount = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        userAccount.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int newValue = Integer.parseInt(""+snapshot.child(stat).getValue())+change;
                switch (change){
                    case 1:{
                        switch(stat){
                            case "countCommented":{
                                switch(newValue){
                                    case 5:{
                                        userAccount.child("Achievements").child("Add 5 comments").setValue("1");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child("countAchievements").getValue())+1);
                                        break;
                                    }
                                    case 10:{
                                        userAccount.child("Achievements").child("Add 10 comments").setValue("1");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child("countAchievements").getValue())+1);
                                        break;
                                    }
                                    case 20:{
                                        userAccount.child("Achievements").child("Add 20 comments").setValue("1");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child("countAchievements").getValue())+1);
                                        break;
                                    }
                                }
                                break;
                            }
                            case "countPosted":{
                                switch(newValue){
                                    case 5:{
                                        userAccount.child("Achievements").child("Post 5 posts").setValue("1");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child("countAchievements").getValue())+1);
                                        break;
                                    }
                                    case 10:{
                                        userAccount.child("Achievements").child("Post 10 posts").setValue("1");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child("countAchievements").getValue())+1);
                                        break;
                                    }
                                    case 20:{
                                        userAccount.child("Achievements").child("Post 20 posts").setValue("1");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child("countAchievements").getValue())+1);
                                        break;
                                    }
                                }
                                break;
                            }
                            case "countRated":{
                                switch(newValue){
                                    case 5:{
                                        userAccount.child("Achievements").child("Rate 5 times").setValue("1");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child("countAchievements").getValue())+1);
                                        break;
                                    }
                                    case 10:{
                                        userAccount.child("Achievements").child("Rate 10 times").setValue("1");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child("countAchievements").getValue())+1);
                                        break;
                                    }
                                    case 20:{
                                        userAccount.child("Achievements").child("Rate 20 times").setValue("1");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child("countAchievements").getValue())+1);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        break;
                    }
                    case -1:{
                        switch(stat){
                            case "countCommented":{
                                switch(newValue){
                                    case 4:{
                                        userAccount.child("Achievements").child("Add 5 comments").setValue("0");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child(stat).getValue())-1);
                                        break;
                                    }
                                    case 9:{
                                        userAccount.child("Achievements").child("Add 10 comments").setValue("0");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child(stat).getValue())-1);
                                        break;
                                    }
                                    case 19:{
                                        userAccount.child("Achievements").child("Add 20 comments").setValue("0");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child(stat).getValue())-1);
                                        break;
                                    }
                                }
                                break;
                            }
                            case "countPosted":{
                                switch(newValue){
                                    case 4:{
                                        userAccount.child("Achievements").child("Post 5 posts").setValue("0");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child(stat).getValue())-1);
                                        break;
                                    }
                                    case 9:{
                                        userAccount.child("Achievements").child("Post 10 posts").setValue("0");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child(stat).getValue())-1);
                                        break;
                                    }
                                    case 19:{
                                        userAccount.child("Achievements").child("Post 20 posts").setValue("0");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child(stat).getValue())-1);
                                        break;
                                    }
                                }
                                break;
                            }
                            case "countRated":{
                                switch(newValue){
                                    case 4:{
                                        userAccount.child("Achievements").child("Rate 5 times").setValue("0");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child(stat).getValue())-1);
                                        break;
                                    }
                                    case 9:{
                                        userAccount.child("Achievements").child("Rate 10 times").setValue("0");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child(stat).getValue())-1);
                                        break;
                                    }
                                    case 19:{
                                        userAccount.child("Achievements").child("Rate 20 times").setValue("0");
                                        userAccount.child("countAchievements").setValue(Integer.parseInt(""+snapshot.child(stat).getValue())-1);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                userAccount.child(stat).setValue(""+(Integer.parseInt(""+snapshot.child(stat).getValue())+change));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{
        private final ImageView ivProfilePhoto;
        private final TextView tvComment, tvName, tvTimestamp;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePhoto = itemView.findViewById(R.id.ivProfilePhoto);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvName = itemView.findViewById(R.id.tvName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
