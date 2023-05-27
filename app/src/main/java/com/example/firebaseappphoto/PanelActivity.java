package com.example.firebaseappphoto;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class PanelActivity extends AppCompatActivity {
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(PanelActivity.this, MainActivity.class));
            finish();
        }
        else {
            FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("status")
                    .setValue("online");
            getSharedPreferences("SP_USER", MODE_PRIVATE).edit().putString("UID", user.getUid()).apply();
        }

        BottomNavigationView navigationView = findViewById(R.id.bottom_menu);
        navigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if(itemId == R.id.bottom_menu_main){
                actionBar.setTitle("FirebaseApp Photo");
                getSupportFragmentManager().beginTransaction().replace(R.id.inside, new MainFragment(), "").commit();
                return true;
            } else if (itemId ==R.id.bottom_menu_your_profile){
                actionBar.setTitle("Your profile");
                getSupportFragmentManager().beginTransaction().replace(R.id.inside, new MyProfileFragment(), "").commit();
                return true;
            } else if (itemId ==R.id.bottom_menu_new_post){
                actionBar.setTitle("New post");
                getSupportFragmentManager().beginTransaction().replace(R.id.inside, new NewPostFragment(), "").commit();
                return true;
            } else if (itemId ==R.id.bottom_menu_chat){
                actionBar.setTitle("Chat");
                getSupportFragmentManager().beginTransaction().replace(R.id.inside, new ChatsFragment(), "").commit();
                return true;
            } else if (itemId ==R.id.bottom_menu_users){
                actionBar.setTitle("Users");
                getSupportFragmentManager().beginTransaction().replace(R.id.inside, new UsersFragment(), "").commit();
                return true;
            }
            return false;
        });

        actionBar.setTitle("FirebaseApp Photo");
        getSupportFragmentManager().beginTransaction().replace(R.id.inside, new MainFragment(), "").commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}