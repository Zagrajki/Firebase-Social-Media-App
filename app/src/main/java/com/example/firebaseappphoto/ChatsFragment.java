package com.example.firebaseappphoto;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.firebaseappphoto.adapters.AdapterChat;
import com.example.firebaseappphoto.models.ModelMessage;
import com.example.firebaseappphoto.models.ModelChat;
import com.example.firebaseappphoto.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<ModelChat> chats;
    private List<ModelUser> users;
    private AdapterChat adapterChat;

    private String myUid;

    private DatabaseReference chatsRef;
    private ValueEventListener chatsValueEventListener;
    private DatabaseReference usersRef;
    private ValueEventListener usersValueEventListener;
    private DatabaseReference messagesRef;
    private ValueEventListener messagesValueEventListener;

    public ChatsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        myUid = FirebaseAuth.getInstance().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid);
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        messagesRef = FirebaseDatabase.getInstance().getReference("Messages");
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        chats = new ArrayList<>();
        users = new ArrayList<>();
        chatsValueEventListener = chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    chats.add(ds.getValue(ModelChat.class));
                }
                show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    private void show() {
        if (usersRef != null && usersValueEventListener != null) {
            usersRef.removeEventListener(usersValueEventListener);
        }
        usersValueEventListener = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelUser user = ds.getValue(ModelUser.class);
                    for (ModelChat chat : chats) {
                        if (Objects.requireNonNull(user).getUid().equals(chat.getId())) {
                            users.add(user);
                            break;
                        }
                    }
                    adapterChat = new AdapterChat(getContext(), users);
                    recyclerView.setAdapter(adapterChat);
                    setLatestMess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLatestMess() {
        if (messagesRef != null && messagesValueEventListener != null) {
            messagesRef.removeEventListener(messagesValueEventListener);
        }
        messagesValueEventListener = messagesRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String prefix = "";
                String latestMess = "";
                for (int i=0; i<users.size(); i++) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelMessage message = ds.getValue(ModelMessage.class);
                        if ((message != null && message.getSender() != null && message.getRecipient() != null)){
                            if (message.getRecipient().equals(myUid) &&
                                    message.getSender().equals(users.get(i).getUid())){
                                if(!message.isSeen()){
                                    prefix="New message: ";
                                }
                                if (message.getType().equals("photo")) {
                                    latestMess = prefix+"[Photo Message]";
                                } else {
                                    latestMess = prefix+message.getMessage();
                                }
                            } else if(message.getRecipient().equals(users.get(i).getUid()) &&
                                    message.getSender().equals(myUid)) {
                                prefix = "You: ";
                                if (message.getType().equals("photo")) {
                                    latestMess = prefix+"[Photo Message]";
                                } else {
                                    latestMess = prefix+message.getMessage();
                                }
                            }
                        }
                        adapterChat.setLatestMessMap(users.get(i).getUid(), latestMess);
                        adapterChat.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        if(chatsRef != null && chatsValueEventListener != null){
            chatsRef.removeEventListener(chatsValueEventListener);
        }
        if(messagesRef != null && messagesValueEventListener != null){
            messagesRef.removeEventListener(messagesValueEventListener);
        }
        if(chatsRef != null && chatsValueEventListener != null){
            chatsRef.removeEventListener(chatsValueEventListener);
        }
        super.onDestroyView();
    }
}