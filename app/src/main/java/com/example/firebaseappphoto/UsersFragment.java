package com.example.firebaseappphoto;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.firebaseappphoto.adapters.AdapterUser;
import com.example.firebaseappphoto.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<ModelUser> users;
    private String myUid;

    private DatabaseReference usersRef;
    private ValueEventListener usersValueEventListener;

    public UsersFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        myUid = FirebaseAuth.getInstance().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        users = new ArrayList<>();
        show();
        return view;
    }

    private void show() {
        if (usersRef != null) {
            if(usersValueEventListener != null){
                usersRef.removeEventListener(usersValueEventListener);
            }
            usersValueEventListener = usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    users.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelUser modelUser = ds.getValue(ModelUser.class);
                        if (!modelUser.getUid().equals(myUid)) {
                            users.add(modelUser);
                        }
                    }
                    recyclerView.setAdapter(new AdapterUser(getActivity(), users));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void search(String query) {
        if (usersRef != null) {
            if(usersValueEventListener != null){
                usersRef.removeEventListener(usersValueEventListener);
            }
            usersValueEventListener = usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    users.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelUser modelUser = ds.getValue(ModelUser.class);
                        if (!modelUser.getUid().equals(myUid) && modelUser.getName().toLowerCase().contains(query)) {
                            users.add(modelUser);
                        }
                    }
                    recyclerView.setAdapter(new AdapterUser(getActivity(), users));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).setVisible(true).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query.trim())) {
                    show();
                }
                else {
                    search(query.toLowerCase());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText.trim())) {
                    show();
                }
                else {
                    search(newText.toLowerCase());
                }
                return false;
            }
        });
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
        if (usersRef != null && usersValueEventListener != null) {
            usersRef.removeEventListener(usersValueEventListener);
        }
        super.onDestroyView();
    }
}