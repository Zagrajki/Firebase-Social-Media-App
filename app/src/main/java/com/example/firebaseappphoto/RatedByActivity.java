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

import com.example.firebaseappphoto.adapters.AdapterUser;
import com.example.firebaseappphoto.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class RatedByActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<ModelUser> users;
    private String id;
    private DatabaseReference ratesRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rated_by);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Rated By");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(RatedByActivity.this));
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        users = new ArrayList<>();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        ratesRef = FirebaseDatabase.getInstance().getReference("Rates").child(id);
        ratesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    usersRef.child(ds.getRef().getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            users.add(snapshot.getValue(ModelUser.class));
                            recyclerView.setAdapter(new AdapterUser(RatedByActivity.this, users));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
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
            FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("status")
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

}