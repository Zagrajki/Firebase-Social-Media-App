package com.example.firebaseappphoto;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class MyProfileFragment extends Fragment {
    private LinearLayout lWatching, lWatchers, lRate, lAchievements;

    private List<ModelPost> posts;
    private String myUid;

    private ImageView ivProfilePhoto, ivInfoProfile, ivWriteTo;
    private TextView tvName, tvAccountDescription, tvPosted, tvWatching, tvWatchers, tvAverageRate, tvAchievements;
    private RecyclerView recyclerView;

    private String telephoneNumber, contactEmail, addressName, address, zipCodeAndTown;

    private DatabaseReference meRef;
    private ValueEventListener meValueEventListener;
    private DatabaseReference myPostsRef;
    private ValueEventListener myPostsValueEventListener;

    public MyProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(linearLayoutManager);

        ivInfoProfile = view.findViewById(R.id.ivInfoProfile);
        ivWriteTo = view.findViewById(R.id.ivWriteTo);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        tvName = view.findViewById(R.id.tvName);
        tvAccountDescription = view.findViewById(R.id.tvAccountsDescription);
        tvPosted = view.findViewById(R.id.tvPostsProfile);
        tvWatching = view.findViewById(R.id.tvWatching);
        tvWatchers = view.findViewById(R.id.tvWatchers);
        tvAverageRate = view.findViewById(R.id.tvAverageRateProfile);
        tvAchievements = view.findViewById(R.id.tvAchievementsProfile);
        lWatching = view.findViewById(R.id.lWatching);
        lWatchers = view.findViewById(R.id.lWatchers);
        lRate = view.findViewById(R.id.lRateProfile);
        lAchievements = view.findViewById(R.id.lAchievementsProfile);

        myUid = FirebaseAuth.getInstance().getUid();

        ivWriteTo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MessagesActivity.class);
            intent.putExtra("uid", myUid);
            requireContext().startActivity(intent);
        });
        ivInfoProfile.setOnClickListener(v -> {
            View view1 = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_contact, null);
            AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).setView(view1).create();
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
            Intent intent = new Intent(getActivity(), WatchingActivity.class);
            intent.putExtra("uid", myUid);
            startActivity(intent);
        });
        lWatchers.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), WatchersActivity.class);
            intent.putExtra("uid", myUid);
            startActivity(intent);
        });
        lRate.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileOpinionsActivity.class);
            intent.putExtra("uid", myUid);
            startActivity(intent);
        });
        lAchievements.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AchievementsActivity.class);
            intent.putExtra("uid", myUid);
            startActivity(intent);
        });
        meRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        meValueEventListener = meRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = "" + snapshot.child("name").getValue();
                    String accountDescription = "" + snapshot.child("accountDescription").getValue();
                    String profilePhoto = "" + snapshot.child("profilePhoto").getValue();
                    String countWatchers = "" + snapshot.child("countWatchers").getValue();
                    String countWatching = "" + snapshot.child("countWatching").getValue();
                    String countPosted = "" + snapshot.child("countPosted").getValue();
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
                    tvPosted.setText(countPosted);
                    tvWatching.setText(countWatching);
                    tvWatchers.setText(countWatchers);
                    tvAverageRate.setText(averageRate);
                    tvAchievements.setText(countAchievements);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        posts = new ArrayList<>();
        myPostsRef = FirebaseDatabase.getInstance().getReference("Posts");
        myPostsValueEventListener = myPostsRef.orderByChild("uid").equalTo(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    posts.add(ds.getValue(ModelPost.class));
                }
                recyclerView.setAdapter(new AdapterPost(getContext(), posts, false, true));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.action_notifications){
            startActivity(new Intent(requireContext(), NotificationsActivity.class));
        }
        else if(itemId == R.id.action_update_profile){
            Intent intent = new Intent(requireContext(), SignUpActivity.class);
            intent.putExtra("account", "existing");
            startActivity(intent);
        }
        else if(itemId == R.id.action_sign_out){
            FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("status")
                    .setValue(String.valueOf(System.currentTimeMillis()));
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), MainActivity.class));
            requireActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        if(meRef != null && meValueEventListener != null){
            meRef.removeEventListener(meValueEventListener);
        }
        if(myPostsRef != null && myPostsValueEventListener != null){
            myPostsRef.removeEventListener(myPostsValueEventListener);
        }
        recyclerView.setAdapter(null);
        super.onDestroyView();
    }
}