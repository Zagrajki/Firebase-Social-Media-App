package com.example.firebaseappphoto;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseappphoto.adapters.AdapterComment;
import com.example.firebaseappphoto.models.ModelComment;
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

public class ProfileOpinionsActivity extends AppCompatActivity {

    private LinearLayout lAverageRateProfileOpinions;
    private TextView tvRates, tvAverageRateProfileOpinions;
    private ImageView ivRate1, ivRate2, ivRate3, ivRate4, ivRate5, ivAverageRate1, ivAverageRate2, ivAverageRate3, ivAverageRate4, ivAverageRate5;

    private String uid, myUid, commenterPhoto;

    private ProgressDialog pd;

    private RecyclerView recyclerView;

    private List<ModelComment> comments;

    private EditText etComment;
    private ImageButton btnForward;
    private ImageView ivCommenterPhoto;

    private DatabaseReference meRef;
    private ValueEventListener meValueEventListener;
    private DatabaseReference commentsRef;
    private ValueEventListener commentsValueEventListener;
    private DatabaseReference usersRef;
    private ValueEventListener usersValueEventListener;
    private DatabaseReference ratesRef;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_opinions);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile Opinions");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        myUid = FirebaseAuth.getInstance().getUid();
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProfileOpinionsActivity.this));
        lAverageRateProfileOpinions = findViewById(R.id.lAverageRateProfileOpinions);
        tvRates = findViewById(R.id.tvRates);
        tvAverageRateProfileOpinions = findViewById(R.id.tvAverageRateProfileOpinions);
        ivRate1 = findViewById(R.id.ivRate1ProfileOpinions);
        ivRate2 = findViewById(R.id.ivRate2ProfileOpinions);
        ivRate3 = findViewById(R.id.ivRate3ProfileOpinions);
        ivRate4 = findViewById(R.id.ivRate4ProfileOpinions);
        ivRate5 = findViewById(R.id.ivRate5ProfileOpinions);
        ivAverageRate1 = findViewById(R.id.ivAverageRate1ProfileOpinions);
        ivAverageRate2 = findViewById(R.id.ivAverageRate2ProfileOpinions);
        ivAverageRate3 = findViewById(R.id.ivAverageRate3ProfileOpinions);
        ivAverageRate4 = findViewById(R.id.ivAverageRate4ProfileOpinions);
        ivAverageRate5 = findViewById(R.id.ivAverageRate5ProfileOpinions);
        etComment = findViewById(R.id.etComment);
        btnForward = findViewById(R.id.btnForward);
        ivCommenterPhoto = findViewById(R.id.ivCommenterPhoto);
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        ratesRef = FirebaseDatabase.getInstance().getReference("Rates");
        comments = new ArrayList<>();

        meRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        meValueEventListener = meRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commenterPhoto = ""+snapshot.child("profilePhoto").getValue();
                try {
                    Picasso.get().load(commenterPhoto).into(ivCommenterPhoto);
                }
                catch (Exception e) {
                    ivCommenterPhoto.setImageResource(R.drawable.ic_person_dark);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        commentsRef = usersRef.child("Comments");
        commentsValueEventListener = commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    comments.add(ds.getValue(ModelComment.class));
                    recyclerView.setAdapter(new AdapterComment(ProfileOpinionsActivity.this, comments, myUid, uid, "user"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        usersValueEventListener = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String countComments = snapshot.child("countComments").getValue()+" comments";
                ((TextView)findViewById(R.id.tvCountComments)).setText(countComments);
                setRates(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        lAverageRateProfileOpinions.setOnClickListener(v -> {
            Intent intent1 = new Intent(ProfileOpinionsActivity.this, RatedByActivity.class);
            intent1.putExtra("id", uid);
            startActivity(intent1);
        });
        ivRate1.setOnClickListener(v -> rateBtnClick(1));
        ivRate2.setOnClickListener(v -> rateBtnClick(2));
        ivRate3.setOnClickListener(v -> rateBtnClick(3));
        ivRate4.setOnClickListener(v -> rateBtnClick(4));
        ivRate5.setOnClickListener(v -> rateBtnClick(5));

        btnForward.setOnClickListener(v -> publishComment());
    }

    private void setRates(DataSnapshot snapshot) {
        String countRatesText = "Average from "+snapshot.child("countRates").getValue()+" rates:";
        String averageRateProfileOpinions = snapshot.child("averageRate").getValue()+"/5.0";
        tvRates.setText(countRatesText);
        tvAverageRateProfileOpinions.setText(averageRateProfileOpinions);
        long averageRate = Math.round(Double.parseDouble(""+snapshot.child("averageRate").getValue()));
        switch ((int) averageRate) {
            case 5: {
                ivAverageRate5.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_rate));
            }
            case 4: {
                ivAverageRate4.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_rate));
            }
            case 3: {
                ivAverageRate3.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_rate));
            }
            case 2: {
                ivAverageRate2.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_rate));
            }
            case 1: {
                ivAverageRate1.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_rate));
                break;
            }
        }
        switch ((int) averageRate) {
            case 0: {
                ivAverageRate1.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_outline_dark));
            }
            case 1: {
                ivAverageRate2.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_outline_dark));
            }
            case 2: {
                ivAverageRate3.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_outline_dark));
            }
            case 3: {
                ivAverageRate4.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_outline_dark));
            }
            case 4: {
                ivAverageRate5.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_outline_dark));
                break;
            }
        }
        ratesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(uid) && snapshot.child(uid).hasChild(myUid)) {
                        int u = Integer.parseInt("" + snapshot.child(uid).child(myUid).child("rate").getValue());
                        switch (u) {
                            case 5: {
                                ivRate5.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_rate));
                            }
                            case 4: {
                                ivRate4.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_rate));
                            }
                            case 3: {
                                ivRate3.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_rate));
                            }
                            case 2: {
                                ivRate2.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_rate));
                            }
                            case 1: {
                                ivRate1.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_rate));
                                break;
                            }
                        }
                }
                else {
                    ivRate1.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_outline_dark));
                    ivRate2.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_outline_dark));
                    ivRate3.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_outline_dark));
                    ivRate4.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_outline_dark));
                    ivRate5.setImageDrawable(ContextCompat.getDrawable(ProfileOpinionsActivity.this, R.drawable.ic_star_outline_dark));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void rateBtnClick(int rate) {
        int[] countRates = new int[1];
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countRates[0] = Integer.parseInt(""+snapshot.child("countRates").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ratesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(uid).hasChild(myUid)){
                        countRates[0]--;
                        ratesRef.child(uid).child(myUid).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    updateUserStats(-1,"countRated");
                                    finishUpdatingRates(countRates[0]);
                                })
                                .addOnFailureListener(e -> Toast.makeText(ProfileOpinionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                    else {
                        countRates[0]++;
                        ratesRef.child(uid).child(myUid).child("rate").setValue(rate)
                                .addOnSuccessListener(aVoid -> {
                                    updateUserStats(1,"countRated");
                                    finishUpdatingRates(countRates[0]);
                                    sendNotification("rated your profile");
                                })
                                .addOnFailureListener(e -> Toast.makeText(ProfileOpinionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void finishUpdatingRates(int countRates) {
        double[] res = {0};
        ratesRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(countRates != 0) {
                    for (DataSnapshot ds: snapshot.getChildren()){
                        res[0] +=Integer.parseInt(ds.child("rate").getValue().toString());
                    }
                    res[0] = Math.round((res[0] / countRates) * 10) / 10.0;
                }
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("countRates", ""+countRates);
                hashMap.put("averageRate", ""+ res[0]);
                usersRef.updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String notification) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("nid", timestamp+"_"+myUid);
        hashMap.put("pidOrUid", uid);
        hashMap.put("type", "user");
        hashMap.put("timestamp", timestamp);
        hashMap.put("notification", notification);
        hashMap.put("uid", myUid);
        usersRef.child("Notifications").child(timestamp+"_"+myUid).setValue(hashMap);
    }

    private void publishComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment...");
        String comment = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cid", timeStamp+"_"+myUid);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);

        usersRef.child("Comments").child(timeStamp+"_"+myUid).setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    pd.dismiss();
                    Toast.makeText(ProfileOpinionsActivity.this, "Comment Added...", Toast.LENGTH_SHORT).show();
                    etComment.setText("");
                    updateCommentCount();
                    updateUserStats(1, "countCommented");
                    sendNotification("commented on your profile");
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(ProfileOpinionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCommentCount() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String countComments = ""+ snapshot.child("countComments").getValue();
                    int newCountCommentVal = Integer.parseInt(countComments) + 1;
                    usersRef.child("countComments").setValue(""+newCountCommentVal);
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
        if(usersRef != null && usersValueEventListener != null){
            usersRef.removeEventListener(usersValueEventListener);
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