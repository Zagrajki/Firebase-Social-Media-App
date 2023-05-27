package com.example.firebaseappphoto;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseappphoto.adapters.AdapterMessage;
import com.example.firebaseappphoto.models.ModelMessage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView ivProfilePhoto;
    private TextView tvName, tvStatus;
    private EditText etMessage;
    private ImageButton btnForward, btnSendPhoto;

    private List<ModelMessage> messages;
    private AdapterMessage adapterChat;

    private String uid, myUid;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private Uri photoUri = null;

    private DatabaseReference usersRef;
    private ValueEventListener usersValueEventListener;
    private DatabaseReference messagesRef;
    private ValueEventListener messagesValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        setSupportActionBar(findViewById(R.id.toolbar));
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvName = findViewById(R.id.tvName);
        tvStatus = findViewById(R.id.tvStatus);
        etMessage = findViewById(R.id.etMessage);
        btnForward = findViewById(R.id.btnForward);
        btnSendPhoto = findViewById(R.id.btnSendPhoto);
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        myUid = FirebaseAuth.getInstance().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if(result){
                        try (Cursor cursor = getContentResolver().query(photoUri,
                                new String[]{MediaStore.Images.Media.DATA}, null, null, null)) {
                            //try (InputStream inputStream = getContext().getContentResolver().openInputStream(photoUri)) {
                            int column_idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            ExifInterface exif = new ExifInterface(cursor.getString(column_idx));
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            Bitmap rotatedBitmap;
                            switch (orientation) {
                                case ExifInterface.ORIENTATION_UNDEFINED:
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotatePhoto(photoUri, 90);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotatePhoto(photoUri, 180);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotatePhoto(photoUri, 270);
                                    break;
                                default:
                                    rotatedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                            }
                            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, getContentResolver().openOutputStream(photoUri));
                            try {
                                sendPhotoMessage();
                            } catch (IOException e) {
                                Toast.makeText(MessagesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(this, "Failed...", Toast.LENGTH_SHORT).show();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    photoUri = result;
                    try {
                        sendPhotoMessage();
                    } catch (Exception e) {
                        Toast.makeText(MessagesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        usersValueEventListener = usersRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Picasso.get().load((String)snapshot.child("profilePhoto").getValue()).into(ivProfilePhoto);
                    }
                    catch (Exception e){
                        ivProfilePhoto.setImageResource(R.drawable.ic_person_dark);
                    }
                    tvName.setText((String)snapshot.child("name").getValue());
                    String typingStatus = ""+ snapshot.child("typingTo").getValue();
                    if (typingStatus.equals(myUid)) {
                        tvStatus.setText(String.format("%s","typing..."));
                    }
                    else {
                        String status = ""+ snapshot.child("status").getValue();
                        if (status.equals("online")) {
                            tvStatus.setText(status);
                        }
                        else {
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(status));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                            tvStatus.setText(String.format("Last online: %s", dateTime));
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessagesActivity.this, "Hello thjer!", Toast.LENGTH_SHORT).show();
            }
        });
        btnForward.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(MessagesActivity.this, "Message can't be empty", Toast.LENGTH_SHORT).show();
            }
            else {
                sendMessage(message);
                etMessage.setText("");
            }
        });
        btnSendPhoto.setOnClickListener(v -> startAddingPhoto());
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() ==0) {
                    usersRef.child(myUid).child("typingTo").setValue("none");
                }
                else {
                    usersRef.child(myUid).child("typingTo").setValue(uid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        messages = new ArrayList<>();
        messagesRef = FirebaseDatabase.getInstance().getReference("Messages");
        messagesValueEventListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelMessage message = ds.getValue(ModelMessage.class);
                    if(message != null) {
                        if (message.getRecipient().equals(myUid) && message.getSender().equals(uid)) {
                            messages.add(message);
                            HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                            hasSeenHashMap.put("isSeen", true);
                            ds.getRef().updateChildren(hasSeenHashMap);
                        } else if (message.getRecipient().equals(uid) && message.getSender().equals(myUid)) {
                            messages.add(message);
                        }
                    }
                }
                adapterChat = new AdapterMessage(MessagesActivity.this, messages);
                adapterChat.notifyDataSetChanged();
                recyclerView.setAdapter(adapterChat);
                recyclerView.smoothScrollToPosition(Objects.requireNonNull(recyclerView.getAdapter()).getItemCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(!isGranted)
                    Toast.makeText(MessagesActivity.this, "Camera permission not granted.", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<String> requestWriteExternalStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(!isGranted)
                    Toast.makeText(MessagesActivity.this, "Storage permission not granted.", Toast.LENGTH_SHORT).show();
            });

    private Bitmap rotatePhoto(Uri uri, float angle) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    private void startAddingPhoto() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_camera_or_gallery, null);
        android.app.AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();
        view.findViewById(R.id.ivCameraCameraOrGallery).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                cameraLauncher.launch(photoUri);
            }
            else {
                requestWriteExternalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                    cameraLauncher.launch(photoUri);
                }
            }
            alertDialog.dismiss();
        });
        view.findViewById(R.id.ivGalleryCameraOrGallery).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                galleryLauncher.launch("image/*");
            }
            else {
                requestWriteExternalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    galleryLauncher.launch("image/*");
                }
            }
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    private void sendMessage(String message) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("isSeen", false);
        hashMap.put("message", message);
        hashMap.put("recipient", uid);
        hashMap.put("sender", myUid);
        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        hashMap.put("type", "text");
        messagesRef.push().setValue(hashMap).addOnSuccessListener(aVoid -> updateChatlist());
    }

    private void sendPhotoMessage() throws IOException {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending photo...");
        progressDialog.show();
        String timeStamp = ""+System.currentTimeMillis();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri).compress(Bitmap.CompressFormat.PNG, 100, baos);
        FirebaseStorage.getInstance().getReference("PhotoMessages/"+timeStamp).putBytes(baos.toByteArray())
                .addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    if (uriTask.isSuccessful()){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", myUid);
                        hashMap.put("recipient", uid);
                        hashMap.put("message", uriTask.getResult().toString());
                        hashMap.put("timestamp", timeStamp);
                        hashMap.put("type", "photo");
                        hashMap.put("isSeen", false);
                        messagesRef.push().setValue(hashMap).addOnSuccessListener(aVoid -> updateChatlist());
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(MessagesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateChatlist(){
        DatabaseReference theirChatlist = FirebaseDatabase.getInstance().getReference("Chatlist").child(uid).child(myUid);
        theirChatlist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    theirChatlist.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference myChatlist = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(uid);
        myChatlist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    myChatlist.child("id").setValue(uid);
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
            usersRef.child(uid).removeEventListener(usersValueEventListener);
        }
        if(messagesRef != null && messagesValueEventListener != null){
            messagesRef.removeEventListener(messagesValueEventListener);
        }
        super.onDestroy();
    }
}