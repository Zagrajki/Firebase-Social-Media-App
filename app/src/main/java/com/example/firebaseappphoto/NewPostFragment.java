package com.example.firebaseappphoto;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NewPostFragment extends Fragment {

    private DatabaseReference meRef;
    private ValueEventListener meValueEventListener;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(!isGranted)
                    Toast.makeText(getContext(), "Camera permission not granted.", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<String> requestWriteExternalStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(!isGranted)
                    Toast.makeText(getContext(), "Storage permission not granted.", Toast.LENGTH_SHORT).show();
            });

    EditText etDescription;
    ImageView ivPostPhoto;
    Button btnPublish;

    Uri photoUri;

    String name, myUid, profilePhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);
        myUid = FirebaseAuth.getInstance().getUid();

        etDescription = view.findViewById(R.id.petDescription);
        ivPostPhoto = view.findViewById(R.id.ivPostPhoto);
        btnPublish = view.findViewById(R.id.btnPublish);

        meRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        meValueEventListener = meRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = ""+ snapshot.child("name").getValue();
                profilePhoto = ""+ snapshot.child("profilePhoto").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if(result){
                        try (Cursor cursor = requireContext().getContentResolver().query(photoUri,
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
                                    rotatedBitmap = rotateImage(photoUri, 90);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(photoUri, 180);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(photoUri, 270);
                                    break;
                                default:
                                    rotatedBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), photoUri);
                            }
                            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, requireContext().getContentResolver().openOutputStream(photoUri));
                            ivPostPhoto.setImageURI(photoUri);
                        }
                        catch (Exception e) {
                            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(requireContext(), "Failed...", Toast.LENGTH_SHORT).show();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    photoUri = result;
                    ivPostPhoto.setImageURI(result);
                });

        ivPostPhoto.setOnClickListener(v -> startAddingImage());

        btnPublish.setOnClickListener(v -> {
            if(ivPostPhoto.getDrawable() != null) {
                publish();
            }
            else {
                Toast.makeText(requireContext(), "Add a photo.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void publish() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Publishing post...");
        progressDialog.show();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String pid = timestamp+"_"+myUid;
        String description = etDescription.getText().toString().trim();
        FirebaseStorage.getInstance().getReference("Posts/" + pid).putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("uid", myUid);
                            hashMap.put("averageRate", "0.0");
                            hashMap.put("pid", pid);
                            hashMap.put("postsDescription", description);
                            hashMap.put("photo", downloadUri);
                            hashMap.put("timestamp", timestamp);
                            hashMap.put("countRates", "0");
                            hashMap.put("countComments", "0");
                            FirebaseDatabase.getInstance().getReference("Posts").child(pid).setValue(hashMap)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(requireContext(), "Post published", Toast.LENGTH_SHORT).show();
                                        etDescription.setText("");
                                        ivPostPhoto.setImageURI(null);
                                        photoUri = null;
                                        updateUserStats(1, "countPosted");
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private Bitmap rotateImage(Uri uri, float angle) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    private void startAddingImage() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_camera_or_gallery, null);
        android.app.AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).setView(view).create();
        view.findViewById(R.id.ivCameraCameraOrGallery).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                photoUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                cameraLauncher.launch(photoUri);
            }
            else {
                requestWriteExternalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                if(ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    photoUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                    cameraLauncher.launch(photoUri);
                }
            }
            alertDialog.dismiss();
        });
        view.findViewById(R.id.ivGalleryCameraOrGallery).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                galleryLauncher.launch("image/*");
            }
            else {
                requestWriteExternalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    galleryLauncher.launch("image/*");
                }
            }
            alertDialog.dismiss();
        });
        alertDialog.show();
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
        super.onDestroyView();
    }
}