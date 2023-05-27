package com.example.firebaseappphoto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.firebaseappphoto.adapters.AdapterComment;
import com.example.firebaseappphoto.adapters.AdapterPost;
import com.example.firebaseappphoto.models.ModelComment;
import com.example.firebaseappphoto.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    private List<ModelPost> post; //1-element list
    private List<ModelComment> comments;
    private RecyclerView postRecyclerView, recyclerView;
    private String uid, myUid, pid;

    private EditText etComment;
    private ImageView ivCommenterPhoto;

    private DatabaseReference postRef;
    private ValueEventListener postValueEventListener;
    private DatabaseReference meRef;
    private ValueEventListener meValueEventListener;
    private DatabaseReference commentsRef;
    private ValueEventListener commentsValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        pid = intent.getStringExtra("pid");
        myUid = FirebaseAuth.getInstance().getUid();
        postRecyclerView = findViewById(R.id.postRecyclerView);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(PostActivity.this));
        post = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(PostActivity.this));
        comments = new ArrayList<>();
        etComment = findViewById(R.id.etComment);
        ivCommenterPhoto = findViewById(R.id.ivCommenterPhoto);
        postRef = FirebaseDatabase.getInstance().getReference("Posts").child(pid);
        postValueEventListener = postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                post.clear();
                uid = ""+snapshot.child("uid").getValue();
                post.add(snapshot.getValue(ModelPost.class));
                postRecyclerView.setAdapter(new AdapterPost(PostActivity.this, post, false, false));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        meRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        meValueEventListener = meRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Picasso.get().load(""+snapshot.child("profilePhoto").getValue()).into(ivCommenterPhoto);
                }
                catch (Exception e) {
                    ivCommenterPhoto.setImageResource(R.drawable.ic_person_dark);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        commentsRef = FirebaseDatabase.getInstance().getReference("Posts").child(pid).child("Comments");
        commentsValueEventListener = commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    comments.add(ds.getValue(ModelComment.class));
                }
                recyclerView.setAdapter(new AdapterComment(PostActivity.this, comments, myUid, pid, "post"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        findViewById(R.id.btnForward).setOnClickListener(v -> publishComment());
    }

    private void publishComment() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding comment...");
        String comment = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(pid).child("Comments");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cid", timeStamp+"_"+myUid);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        ref.child(timeStamp+"_"+myUid).setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    etComment.setText("");
                    updateCommentStats();
                    updateUserStats(1, "countCommented");
                    sendCommentNotification();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateCommentStats() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(pid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String countComments = ""+ snapshot.child("countComments").getValue();
                    int newCountCommentVal = Integer.parseInt(countComments) + 1;
                    ref.child("countComments").setValue(""+newCountCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendCommentNotification() {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("nid", timestamp+"_"+myUid);
        hashMap.put("pidOrUid", pid);
        hashMap.put("type", "post");
        hashMap.put("timestamp", timestamp);
        hashMap.put("notification", "commented on your post");
        hashMap.put("uid", myUid);
        FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Notifications")
                .child(timestamp+"_"+myUid).setValue(hashMap);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_notifications){
            startActivity(new Intent(this, NotificationsActivity.class));
        }
        else if(itemId == R.id.action_update_profile){
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.putExtra("account", "existing");
            startActivity(intent);
        }
        else if(itemId == R.id.action_sign_out){
            FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("status")
                    .setValue(String.valueOf(System.currentTimeMillis()));
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        if(postRef != null && postValueEventListener != null){
            postRef.removeEventListener(postValueEventListener);
        }
        if(meRef != null && meValueEventListener != null){
            meRef.removeEventListener(meValueEventListener);
        }
        if(commentsRef != null && commentsValueEventListener != null){
            commentsRef.removeEventListener(commentsValueEventListener);
        }
        recyclerView.setAdapter(null);
        super.onDestroy();
    }
}