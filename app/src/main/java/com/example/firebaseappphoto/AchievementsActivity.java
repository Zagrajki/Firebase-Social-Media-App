package com.example.firebaseappphoto;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseappphoto.adapters.AdapterAchievement;
import com.example.firebaseappphoto.models.ModelAchievement;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {
    private DatabaseReference achievementsRef;
    private ValueEventListener achievementsValueEventListener;
    private RecyclerView recyclerView;
    private List<ModelAchievement> achievements;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Achievements");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(AchievementsActivity.this));
        achievements = new ArrayList<>();
        achievementsRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Achievements");
        achievementsValueEventListener = achievementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                achievements.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    achievements.add(new ModelAchievement(ds.getKey(), ("" + ds.getValue()).equals("1")));
                }
                recyclerView.setAdapter(new AdapterAchievement(AchievementsActivity.this, achievements));
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
            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).child("status")
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
        if(achievementsRef != null && achievementsValueEventListener != null){
            achievementsRef.removeEventListener(achievementsValueEventListener);
        }
        super.onDestroy();
    }
}