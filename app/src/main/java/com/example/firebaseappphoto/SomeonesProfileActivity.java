package com.example.firebaseappphoto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseappphoto.adapters.AdapterPost;
import com.example.firebaseappphoto.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class SomeonesProfileActivity extends AppCompatActivity {
    private LinearLayout lWatching, lWatchers, lRate, lAchievements;

    private List<ModelPost> posts;
    private String myUid, uid;
    private ActionBar actionBar;

    private ImageView ivProfilePhoto, ivInfoProfile, ivWriteTo;
    private TextView tvName, tvAccountDescription, tvPosted, tvWatching, tvWatchers, tvAverageRate, tvAchievements;
    private Button btnWatch;
    private RecyclerView recyclerView;

    private String telephoneNumber, contactEmail, addressName, address, zipCodeAndTown;

    private DatabaseReference userRef;
    private ValueEventListener userValueEventListener;
    private DatabaseReference postsRef;
    private ValueEventListener postsValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_someones_profile);

        actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        myUid = FirebaseAuth.getInstance().getUid();
        uid = getIntent().getStringExtra("uid");

        recyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SomeonesProfileActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(linearLayoutManager);

        ivInfoProfile = findViewById(R.id.ivInfoProfile);
        ivWriteTo = findViewById(R.id.ivWriteTo);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvName = findViewById(R.id.tvName);
        tvAccountDescription = findViewById(R.id.tvAccountsDescription);
        btnWatch = findViewById(R.id.btnWatch);
        if(!uid.equals(myUid)){
            btnWatch.setVisibility(View.VISIBLE);
        }
        tvPosted = findViewById(R.id.tvPostsProfile);
        tvWatching = findViewById(R.id.tvWatching);
        tvWatchers = findViewById(R.id.tvWatchers);
        tvAverageRate = findViewById(R.id.tvAverageRateProfile);
        tvAchievements = findViewById(R.id.tvAchievementsProfile);
        lWatching = findViewById(R.id.lWatching);
        lWatchers = findViewById(R.id.lWatchers);
        lRate = findViewById(R.id.lRateProfile);
        lAchievements = findViewById(R.id.lAchievementsProfile);
        ivWriteTo.setOnClickListener(v -> {
            Intent intent = new Intent(SomeonesProfileActivity.this, MessagesActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        ivInfoProfile.setOnClickListener(v -> {
            View view1 = LayoutInflater.from(SomeonesProfileActivity.this).inflate(R.layout.dialog_contact, null);
            AlertDialog alertDialog = new AlertDialog.Builder(SomeonesProfileActivity.this).setView(view1).create();
            if(telephoneNumber.equals("") && contactEmail.equals("") && addressName.equals("")){
                ((TextView) view1.findViewById(R.id.tvTelephoneNumberTitle)).setText(R.string.no_contact_data);
                view1.findViewById(R.id.tvTelephoneNumber).setVisibility(View.GONE);
                view1.findViewById(R.id.tvContactEmailTitle).setVisibility(View.GONE);
                view1.findViewById(R.id.tvContactEmail).setVisibility(View.GONE);
                view1.findViewById(R.id.tvAddressTitle).setVisibility(View.GONE);
                view1.findViewById(R.id.tvAddressName).setVisibility(View.GONE);
                view1.findViewById(R.id.tvAddress).setVisibility(View.GONE);
                view1.findViewById(R.id.tvZipCodeAndTown).setVisibility(View.GONE);
            }
            else {
                if(telephoneNumber.equals("")){
                    view1.findViewById(R.id.tvTelephoneNumberTitle).setVisibility(View.GONE);
                    view1.findViewById(R.id.tvTelephoneNumber).setVisibility(View.GONE);
                }
                else {
                    ((TextView) view1.findViewById(R.id.tvTelephoneNumber)).setText(telephoneNumber);
                }
                if(contactEmail.equals("")){
                    view1.findViewById(R.id.tvContactEmailTitle).setVisibility(View.GONE);
                    view1.findViewById(R.id.tvContactEmail).setVisibility(View.GONE);
                }
                else {
                    ((TextView) view1.findViewById(R.id.tvContactEmail)).setText(contactEmail);
                }
                if(addressName.equals("")){
                    view1.findViewById(R.id.tvAddressTitle).setVisibility(View.GONE);
                    view1.findViewById(R.id.tvAddressName).setVisibility(View.GONE);
                    view1.findViewById(R.id.tvAddress).setVisibility(View.GONE);
                    view1.findViewById(R.id.tvZipCodeAndTown).setVisibility(View.GONE);
                }
                else {
                    ((TextView) view1.findViewById(R.id.tvAddressName)).setText(addressName);
                    ((TextView) view1.findViewById(R.id.tvAddress)).setText(address);
                    ((TextView) view1.findViewById(R.id.tvZipCodeAndTown)).setText(zipCodeAndTown);
                }
            }
            alertDialog.show();
        });
        lWatching.setOnClickListener(v -> {
            Intent intent = new Intent(SomeonesProfileActivity.this, WatchingActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        lWatchers.setOnClickListener(v -> {
            Intent intent = new Intent(SomeonesProfileActivity.this, WatchersActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        lRate.setOnClickListener(v -> {
            Intent intent = new Intent(SomeonesProfileActivity.this, ProfileOpinionsActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        lAchievements.setOnClickListener(v -> {
            Intent intent = new Intent(SomeonesProfileActivity.this, AchievementsActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userValueEventListener = userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = "" + snapshot.child("name").getValue();
                    String accountDescription = "" + snapshot.child("accountDescription").getValue();
                    String profilePhoto = "" + snapshot.child("profilePhoto").getValue();
                    String countPosted = "" + snapshot.child("countPosted").getValue();
                    String countWatchers = "" + snapshot.child("countWatchers").getValue();
                    String countWatching = "" + snapshot.child("countWatching").getValue();
                    String averageRate = "" + snapshot.child("averageRate").getValue() + "/5.0";
                    String countAchievements = "" + snapshot.child("countAchievements").getValue();

                    telephoneNumber = "" + snapshot.child("telephoneNumber").getValue();
                    contactEmail = "" + snapshot.child("contactEmail").getValue();
                    addressName = "" + snapshot.child("addressName").getValue();
                    address = "" + snapshot.child("address").getValue();
                    zipCodeAndTown = "" + snapshot.child("zipCode").getValue() + snapshot.child("town").getValue();

                    tvName.setText(name);
                    tvAccountDescription.setText(accountDescription);
                    try {
                        Picasso.get().load(profilePhoto).into(ivProfilePhoto);
                    } catch (Exception e) {
                        ivProfilePhoto.setImageResource(R.drawable.ic_person_dark);
                    }
                    if(snapshot.child("Watchers").hasChild(myUid)){
                        btnWatch.setText(R.string.watched);
                        btnWatch.setOnClickListener(v -> {
                            snapshot.getRef().child("Watchers").child(myUid).removeValue();
                            String countWatchers12 = ""+ snapshot.child("countWatchers").getValue();
                            int newCountWatchersVal = Integer.parseInt(countWatchers12) - 1;
                            snapshot.getRef().child("countWatchers").setValue(""+newCountWatchersVal);

                            FirebaseDatabase.getInstance().getReference("Users").child(myUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot12) {
                                    snapshot12.getRef().child(myUid).child("Watching").child(uid).removeValue();
                                    String countWatching12 = ""+ snapshot12.child("countWatching").getValue();
                                    int newCountWatchingVal = Integer.parseInt(countWatching12) - 1;
                                    snapshot12.getRef().child("countWatching").setValue(""+newCountWatchingVal);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        });
                    }
                    else{
                        btnWatch.setText(R.string.watch);
                        btnWatch.setOnClickListener(v -> {
                            snapshot.getRef().child("Watchers").child(myUid).setValue(myUid);
                            String countWatchers1 = ""+ snapshot.child("countWatchers").getValue();
                            int newCountWatchersVal = Integer.parseInt(countWatchers1) + 1;
                            snapshot.getRef().child("countWatchers").setValue(""+newCountWatchersVal);
                            FirebaseDatabase.getInstance().getReference("Users").child(myUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                            snapshot1.getRef().child("Watching").child(uid).setValue(uid);
                                            String countWatching1 = ""+ snapshot1.child("countWatching").getValue();
                                            int newCountWatchingVal = Integer.parseInt(countWatching1) + 1;
                                            snapshot1.getRef().child("countWatching").setValue(""+newCountWatchingVal);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        });
                    }
                    tvPosted.setText(countPosted);
                    tvWatching.setText(countWatching);
                    tvWatchers.setText(countWatchers);
                    tvAverageRate.setText(averageRate);
                    tvAchievements.setText(countAchievements);
                    Objects.requireNonNull(actionBar).setTitle(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        posts = new ArrayList<>();
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        postsValueEventListener = postsRef.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    posts.add(ds.getValue(ModelPost.class));
                }
                recyclerView.setAdapter(new AdapterPost(SomeonesProfileActivity.this, posts,
                        false, true));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_notifications){
            startActivity(new Intent(SomeonesProfileActivity.this, NotificationsActivity.class));
        }
        else if(itemId == R.id.action_update_profile){
            Intent intent = new Intent(SomeonesProfileActivity.this, SignUpActivity.class);
            intent.putExtra("account", "existing");
            startActivity(intent);
        }
        else if(itemId == R.id.action_sign_out){
            FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("status")
                    .setValue(String.valueOf(System.currentTimeMillis()));
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(SomeonesProfileActivity.this, MainActivity.class));
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
        if(userRef != null && userValueEventListener != null){
            userRef.removeEventListener(userValueEventListener);
        }
        if(postsRef != null && postsValueEventListener != null){
            postsRef.removeEventListener(postsValueEventListener);
        }
        recyclerView.setAdapter(null);
        super.onDestroy();
    }
}