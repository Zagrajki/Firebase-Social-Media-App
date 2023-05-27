package com.example.firebaseappphoto.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseappphoto.BuildConfig;
import com.example.firebaseappphoto.PostActivity;
import com.example.firebaseappphoto.RatedByActivity;
import com.example.firebaseappphoto.R;
import com.example.firebaseappphoto.SomeonesProfileActivity;
import com.example.firebaseappphoto.models.ModelPost;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder>{

    private final Context context;
    private final List<ModelPost> posts;
    private final boolean goingToProfile, goingToComments;
    private final String myUid;
    private final DatabaseReference ratesRef;
    private final DatabaseReference postsRef;
    private final HashMap<DatabaseReference, ValueEventListener> vels;

    public AdapterPost(Context context, List<ModelPost> posts, boolean goingToProfile, boolean goingToComments) {
        this.context = context;
        this.posts = posts;
        this.goingToProfile = goingToProfile;
        this.goingToComments = goingToComments;
        this.myUid = FirebaseAuth.getInstance().getUid();
        this.ratesRef = FirebaseDatabase.getInstance().getReference("Rates");
        this.postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        this.vels = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_post, parent, false);
        return new MyHolder(view);
    }
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        vels.forEach(Query::removeEventListener);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPost.MyHolder holder, int position) {
        String uid = posts.get(position).getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
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
        String pid = posts.get(position).getPid();
        try {
            Picasso.get().load(posts.get(position).getPhoto()).into(holder.ivPostPhoto);
        }
        catch (Exception e) {
            holder.ivPostPhoto.setImageResource(R.drawable.ic_posts_dark);
        }
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(posts.get(position).getTimestamp()));
        String timestamp = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        holder.tvCountComments.setText(String.format("%s Comments", posts.get(position).getCountComments()));
        holder.tvPostDescription.setText(posts.get(position).getPostsDescription());
        holder.tvRates.setText(String.format("%s from %s Rates", posts.get(position).getAverageRate(), posts.get(position).getCountRates()));
        holder.tvTimestamp.setText(timestamp);

        switch ((int) Math.round(Double.parseDouble(""+posts.get(position).getAverageRate()))) {
            case 5: {
                holder.ivAverageRate5.setImageResource(R.drawable.ic_star_rate);
            }
            case 4: {
                holder.ivAverageRate4.setImageResource(R.drawable.ic_star_rate);
            }
            case 3: {
                holder.ivAverageRate3.setImageResource(R.drawable.ic_star_rate);
            }
            case 2: {
                holder.ivAverageRate2.setImageResource(R.drawable.ic_star_rate);
            }
            case 1: {
                holder.ivAverageRate1.setImageResource(R.drawable.ic_star_rate);
                break;
            }
        }

        ratesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(pid)) {
                    if (snapshot.child(pid).hasChild(myUid)) {
                        int rate = Integer.parseInt("" + snapshot.child(pid).child(myUid).child("rate").getValue());
                        switch (rate) {
                            case 5: {
                                holder.ivRate5.setImageResource(R.drawable.ic_star_rate);
                            }
                            case 4: {
                                holder.ivRate4.setImageResource(R.drawable.ic_star_rate);
                            }
                            case 3: {
                                holder.ivRate3.setImageResource(R.drawable.ic_star_rate);
                            }
                            case 2: {
                                holder.ivRate2.setImageResource(R.drawable.ic_star_rate);
                            }
                            case 1: {
                                holder.ivRate1.setImageResource(R.drawable.ic_star_rate);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(uid.equals(myUid)){
            holder.ivExtra.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, holder.ivExtra, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit the description");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Delete this post");
                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id==0){
                        editDescription(pid, posts.get(position).getPostsDescription());
                    }
                    else if (id==1){
                        String photo = posts.get(position).getPhoto();
                        FirebaseDatabase.getInstance().getReference("Posts").child(pid).removeValue()
                                .addOnSuccessListener(aVoid ->{
                                    updateUserStats(-1,"countPosted");
                                    FirebaseStorage.getInstance().getReferenceFromUrl(photo).delete()
                                            .addOnSuccessListener(aVoid1 ->
                                            Toast.makeText(context, "Post deleted.", Toast.LENGTH_SHORT).show());
                                });
                    }
                    return false;
                });
                popupMenu.show();
            });
        }
        else {
            holder.ivExtra.setVisibility(View.GONE);
        }
        holder.ivRate1.setOnClickListener(v -> rateBtnClick(Integer.parseInt(posts.get(position).getCountRates()), pid, uid, 1));
        holder.ivRate2.setOnClickListener(v -> rateBtnClick(Integer.parseInt(posts.get(position).getCountRates()), pid, uid, 2));
        holder.ivRate3.setOnClickListener(v -> rateBtnClick(Integer.parseInt(posts.get(position).getCountRates()), pid, uid, 3));
        holder.ivRate4.setOnClickListener(v -> rateBtnClick(Integer.parseInt(posts.get(position).getCountRates()), pid, uid, 4));
        holder.ivRate5.setOnClickListener(v -> rateBtnClick(Integer.parseInt(posts.get(position).getCountRates()), pid, uid, 5));
        holder.ivShare.setOnClickListener(v -> share(
                ((BitmapDrawable)holder.ivPostPhoto.getDrawable()).getBitmap()));
        holder.lRates.setOnClickListener(v -> {
            Intent intent = new Intent(context, RatedByActivity.class);
            intent.putExtra("id", pid);
            context.startActivity(intent);
        });
        if(goingToProfile) {
            holder.lProfile.setOnClickListener(v -> {
                Intent intent = new Intent(context, SomeonesProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            });
        }
        if(goingToComments){
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra("pid", pid);
                context.startActivity(intent);
            });
        }
    }

    private void editDescription(String pid, String postsDescription){
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_post_description, null);
        AlertDialog alertDialog = new AlertDialog.Builder(context).setView(view).create();
        TextInputEditText tietPostsDescription = view.findViewById(R.id.tietPostsDescription);
        tietPostsDescription.setText(postsDescription);
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            postsRef.child(pid).child("postsDescription")
                    .setValue(Objects.requireNonNull(tietPostsDescription.getText()).toString().trim())
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Description updated.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    private void rateBtnClick(int nRates, String pid, String uid, int rate) {
        int[] countRates = new int[1];
        countRates[0] = nRates;
        ratesRef.child(pid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(myUid)){
                        countRates[0]--;
                        ratesRef.child(pid).child(myUid).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    updateUserStats(-1,"countRated");
                                    finishUpdatingRates(countRates[0], pid);
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                    else {
                        countRates[0]++;
                        ratesRef.child(pid).child(myUid).child("rate").setValue(rate)
                                .addOnSuccessListener(aVoid -> {
                                    updateUserStats(1,"countRated");
                                    finishUpdatingRates(countRates[0], pid);
                                    sendNotification(uid, pid, "rated your post");
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void finishUpdatingRates(int countRates, String pid) {
        double[] res = {0};
        ratesRef.child(pid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(countRates != 0) {
                    for (DataSnapshot ds: snapshot.getChildren()){
                        res[0] +=Integer.parseInt(Objects.requireNonNull(ds.child("rate").getValue()).toString());
                    }
                    res[0] = Math.round((res[0] / countRates) * 10) / 10.0;
                }
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("countRates", ""+countRates);
                hashMap.put("averageRate", ""+ res[0]);
                postsRef.child(pid).updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                                        userAccount.child("countAchievements").setValue(Integer
                                                .parseInt(""+snapshot.child("countAchievements").getValue())+1);
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

    private void sendNotification(String uid, String pidOrUid, String notification) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("nid", timestamp+"_"+myUid);
        hashMap.put("pidOrUid", pidOrUid);
        hashMap.put("type", "post");
        hashMap.put("timestamp", timestamp);
        hashMap.put("notification", notification);
        hashMap.put("uid", myUid);
        FirebaseDatabase.getInstance().getReference("Users").child(uid)
                .child("Notifications").child(timestamp+"_"+myUid).setValue(hashMap);
    }

    private void share(Bitmap bitmap) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, savePhotoAndGetUri(bitmap));
        intent.setType("image/png");
        context.startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private Uri savePhotoAndGetUri(Bitmap bitmap) {
        File folder = new File(context.getCacheDir(), "shared_photos");
        Uri uri = null;
        try {
            folder.mkdirs();
            File file = new File(folder, "shared_photo.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,  100, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider",
                    file);
        }
        catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{
        private final ImageView ivPostPhoto, ivProfilePhoto, ivShare, ivExtra,
                ivRate1, ivRate2, ivRate3, ivRate4, ivRate5,
                ivAverageRate1, ivAverageRate2, ivAverageRate3, ivAverageRate4, ivAverageRate5;
        private final LinearLayout lProfile, lRates;
        private final TextView tvName, tvCountComments, tvPostDescription, tvRates, tvTimestamp;

        public MyHolder (@NonNull View itemView) {
            super(itemView);
            lProfile = itemView.findViewById(R.id.lProfile);
            lRates = itemView.findViewById(R.id.lRatesPosts);
            ivAverageRate1 = itemView.findViewById(R.id.ivAverageRate1Posts);
            ivAverageRate2 = itemView.findViewById(R.id.ivAverageRate2Posts);
            ivAverageRate3 = itemView.findViewById(R.id.ivAverageRate3Posts);
            ivAverageRate4 = itemView.findViewById(R.id.ivAverageRate4Posts);
            ivAverageRate5 = itemView.findViewById(R.id.ivAverageRate5Posts);
            ivExtra = itemView.findViewById(R.id.ivExtra);
            ivPostPhoto = itemView.findViewById(R.id.ivPostPhoto);
            ivProfilePhoto = itemView.findViewById(R.id.ivProfilePhoto);
            ivRate1 = itemView.findViewById(R.id.ivRate1Posts);
            ivRate2 = itemView.findViewById(R.id.ivRate2Posts);
            ivRate3 = itemView.findViewById(R.id.ivRate3Posts);
            ivRate4 = itemView.findViewById(R.id.ivRate4Posts);
            ivRate5 = itemView.findViewById(R.id.ivRate5Posts);
            ivShare = itemView.findViewById(R.id.ivShare);
            tvName = itemView.findViewById(R.id.tvName);
            tvCountComments = itemView.findViewById(R.id.tvCountComments);
            tvPostDescription = itemView.findViewById(R.id.tvPostDescription);
            tvRates = itemView.findViewById(R.id.tvRates);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
