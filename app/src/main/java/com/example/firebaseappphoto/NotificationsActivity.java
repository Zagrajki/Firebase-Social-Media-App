package com.example.firebaseappphoto;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseappphoto.adapters.AdapterNotification;
import com.example.firebaseappphoto.models.ModelNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<ModelNotification> notifications;
    private String myUid;

    private DatabaseReference myNotificationsRef;
    private ValueEventListener myNotificationsValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Notifications");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        myUid = FirebaseAuth.getInstance().getUid();
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotificationsActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        notifications = new ArrayList<>();
        myNotificationsRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("Notifications");
        myNotificationsValueEventListener = myNotificationsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notifications.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            notifications.add(ds.getValue(ModelNotification.class));
                        }
                        recyclerView.setAdapter(new AdapterNotification(NotificationsActivity.this, notifications));
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
        if(myNotificationsRef != null && myNotificationsValueEventListener != null){
            myNotificationsRef.removeEventListener(myNotificationsValueEventListener);
        }
        recyclerView.setAdapter(null);
        super.onDestroy();
    }
}