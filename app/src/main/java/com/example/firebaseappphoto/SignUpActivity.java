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
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText tietName, tietEmail, tietPassword, tietConfirmPassword,
            tietAddressName, tietAddress, tietZipCode, tietTown,
            tietContactEmail, tietTelephoneNumber, tietAccountsDescription;
    private ImageView ivProfilePhoto;
    private TextView tvPassword1, tvPassword2, tvChangePassword;
    private Button btnSignUp;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private Uri photoUri;
    private boolean newProfilePhoto;
    private FirebaseAuth firebaseAuth;
    private boolean existingAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Create a New Account");
        }
        ivProfilePhoto = findViewById(R.id.ivProfilePhotoSignUp);
        tvChangePassword = findViewById(R.id.tvChangePasswordSignUp);
        tvPassword1 = findViewById(R.id.tvPassword1SignUp);
        tvPassword2 = findViewById(R.id.tvPassword2SignUp);
        tietConfirmPassword = findViewById(R.id.tietPassword2SignUp);
        tietEmail = findViewById(R.id.tietEmailSignUp);
        tietName = findViewById(R.id.tietNameSignUp);
        tietPassword = findViewById(R.id.tietPassword1SignUp);

        tietAddressName = findViewById(R.id.tietAddressNameSignUp);
        tietAddress = findViewById(R.id.tietAddressSignUp);
        tietZipCode = findViewById(R.id.tietZipCodeSignUp);
        tietTown = findViewById(R.id.tietTownSignUp);

        tietContactEmail = findViewById(R.id.tietContactEmailSignUp);
        tietTelephoneNumber = findViewById(R.id.tietTelephoneNumberSignUp);

        tietAccountsDescription = findViewById(R.id.tietAccountsDescriptionSignUp);

        btnSignUp = findViewById(R.id.btnSignUpSignUp);

        photoUri = null;

        firebaseAuth = FirebaseAuth.getInstance();

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if(result){
                        try (Cursor cursor = SignUpActivity.this.getContentResolver().query(photoUri,
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
                                    rotatedBitmap = MediaStore.Images.Media.getBitmap(SignUpActivity.this.getContentResolver(), photoUri);
                            }
                            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, SignUpActivity.this.getContentResolver().
                                    openOutputStream(photoUri));
                            newProfilePhoto = true;
                            ivProfilePhoto.setImageURI(photoUri);
                        }
                        catch (Exception e) {
                            Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(SignUpActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    newProfilePhoto = true;
                    photoUri = result;
                    ivProfilePhoto.setImageURI(result);
                });

        ivProfilePhoto.setOnClickListener(v -> startAddingPhoto());
        ivProfilePhoto.setOnLongClickListener(v -> {
            newProfilePhoto = true;
            photoUri = null;
            ivProfilePhoto.setImageDrawable(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.ic_person_plus_dark));
            return true;
        });

        existingAccount = false;

        if(getIntent().getStringExtra("account").equals("existing")){
            existingAccount = true;
            ((TextView)findViewById(R.id.tvSignUpSignUp)).setText("Update your data");
            btnSignUp.setText("Update");
            actionBar.setTitle("Update your data");
            tvChangePassword.setVisibility(View.VISIBLE);
            tvPassword1.setText(R.string.old_password);
            tvPassword2.setText(R.string.new_password);
            newProfilePhoto = false;
            FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profilePhoto = ""+ snapshot.child("profilePhoto").getValue();
                        String name = ""+ snapshot.child("name").getValue();
                        String email = ""+ snapshot.child("email").getValue();
                        String addressName = ""+ snapshot.child("addressName").getValue();
                        String address = ""+ snapshot.child("address").getValue();
                        String zipCode = ""+ snapshot.child("zipCode").getValue();
                        String town = ""+ snapshot.child("town").getValue();
                        String contactEmail = ""+ snapshot.child("contactEmail").getValue();
                        String telephoneNumber = ""+ snapshot.child("telephoneNumber").getValue();
                        String accountsDescription = ""+ snapshot.child("accountDescription").getValue();

                        tietName.setText(name);
                        tietEmail.setText(email);
                        tietAddressName.setText(addressName);
                        tietAddress.setText(address);
                        tietZipCode.setText(zipCode);
                        tietTown.setText(town);
                        tietContactEmail.setText(contactEmail);
                        tietTelephoneNumber.setText(telephoneNumber);
                        tietAccountsDescription.setText(accountsDescription);

                        try {
                            Picasso.get().load(profilePhoto).into(ivProfilePhoto);
                        }
                        catch (Exception e) {
                            ivProfilePhoto.setImageResource(R.drawable.ic_person_plus_dark);
                        }
                    }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        btnSignUp.setOnClickListener(v -> {
            String name = tietName.getText().toString().trim();
            String email = tietEmail.getText().toString().trim();
            String password = tietPassword.getText().toString().trim();
            String confirmPassword = tietConfirmPassword.getText().toString().trim();
            String addressName = tietAddressName.getText().toString().trim();
            String address = tietAddress.getText().toString().trim();
            String zipCode = tietZipCode.getText().toString().trim();
            String town = tietTown.getText().toString().trim();
            String contactEmail = tietContactEmail.getText().toString().trim();
            String telephoneNumber = tietTelephoneNumber.getText().toString().trim();
            String accountDescription = tietAccountsDescription.getText().toString().trim();
            if (!addressName.equals("") || !address.equals("") || !zipCode.equals("") || !town.equals("")) {
                if (addressName.equals("")) {
                    tietAddressName.setError("Complete your address information");
                    tietAddressName.setFocusable(true);
                }
                else if (address.equals("")) {
                    tietAddress.setError("Complete your address information");
                    tietAddress.setFocusable(true);
                }
                else if (zipCode.equals("")) {
                    tietZipCode.setError("Complete your address information");
                    tietZipCode.setFocusable(true);
                }
                else if (town.equals("")) {
                    tietTown.setError("Complete your address information");
                    tietTown.setFocusable(true);
                }
            }
            if (!name.equals("")) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!existingAccount) {
                        if (password.length()>=6) {
                            if (password.equals(confirmPassword)){
                                signUp(name, email, password, addressName, address, zipCode,
                                        town, contactEmail, telephoneNumber, accountDescription);
                            }
                            else {
                                tietConfirmPassword.setError("It's not the same as a given password");
                                tietConfirmPassword.setFocusable(true);
                            }
                        }
                        else {
                            tietPassword.setError("Password length at least 6 characters");
                            tietPassword.setFocusable(true);
                        }
                    }
                    else {
                        if(password.equals("") || confirmPassword.equals("")) {
                            changeData(name, email, password, confirmPassword, addressName, address, zipCode,
                                    town, contactEmail, telephoneNumber, accountDescription);
                        }
                        else if (password.length()>=6) {
                            if(confirmPassword.length()>=6) {
                                changeData(name, email, password, confirmPassword, addressName, address, zipCode,
                                        town, contactEmail, telephoneNumber, accountDescription);
                            }
                            else {
                                tietConfirmPassword.setError("Password length at least 6 characters");
                                tietConfirmPassword.setFocusable(true);
                            }
                        }
                        else {
                            tietPassword.setError("Password length at least 6 characters");
                            tietPassword.setFocusable(true);
                        }
                    }
                }
                else {
                    tietEmail.setError("Invalid email");
                    tietEmail.setFocusable(true);
                }
            }
            else {
                tietName.setError("Entry your name");
                tietName.setFocusable(true);
            }
        });
    }

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(!isGranted)
                Toast.makeText(SignUpActivity.this, "Camera permission not granted.", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<String> requestWriteExternalStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if(!isGranted)
                    Toast.makeText(SignUpActivity.this, "Storage permission not granted.", Toast.LENGTH_SHORT).show();
            });

    private Bitmap rotatePhoto(Uri uri, float angle) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(SignUpActivity.this.getContentResolver(), uri);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    private void startAddingPhoto() {
        View view = LayoutInflater.from(SignUpActivity.this).inflate(R.layout.dialog_camera_or_gallery, null);
        AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).setView(view).create();
        view.findViewById(R.id.ivCameraCameraOrGallery).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    SignUpActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    SignUpActivity.this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                cameraLauncher.launch(photoUri);
            }
            else {
                requestWriteExternalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                if (ContextCompat.checkSelfPermission(
                        SignUpActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        SignUpActivity.this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                    cameraLauncher.launch(photoUri);
                }
            }
            alertDialog.dismiss();
        });
        view.findViewById(R.id.ivGalleryCameraOrGallery).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    SignUpActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                galleryLauncher.launch("image/*");
            }
            else {
                requestWriteExternalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (ContextCompat.checkSelfPermission(
                        SignUpActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    galleryLauncher.launch("image/*");
                }
            }
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    private void uploadProfilePhoto(FirebaseUser user, DatabaseReference userdata) {
        FirebaseStorage.getInstance().getReference("ProfilePhotos/"+user.getUid()).putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("profilePhoto", uriTask.getResult().toString());
                    userdata.updateChildren(hashMap).addOnSuccessListener(aVoid ->
                            Toast.makeText(SignUpActivity.this, "Profile photo updated.", Toast.LENGTH_SHORT).show());

                })
                .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void updateEmail(String password, String email, FirebaseUser user, DatabaseReference userdata) {
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(aVoid -> user.updateEmail(email)
                        .addOnSuccessListener(aVoid1 -> {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            userdata.updateChildren(hashMap);
                            Toast.makeText(SignUpActivity.this, "Email updated.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, "Email not updated: "
                                +e.getMessage(), Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, "Email not updated: "
                        +e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updatePassword(String oldPassword, String newPassword, FirebaseUser user) {
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(aVoid -> user.updatePassword(newPassword)
                        .addOnSuccessListener(aVoid1 -> Toast.makeText(SignUpActivity.this, "Password updated.",
                                Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, "Password not updated: "
                                +e.getMessage(), Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, "Password not updated: "
                        +e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void changeData(String name, String email, String oldPassword, String newPassword, String addressName, String address, String zipCode, String town, String contactEmail, String telephoneNumber, String accountDescription) {
        ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setMessage("Updating data...");
        progressDialog.show();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(!oldPassword.equals("") && !newPassword.equals("")) {
            updatePassword(oldPassword, newPassword, user);
        }
        HashMap<String, Object> hashMap = new HashMap<>();
        DatabaseReference userdata = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        if(!oldPassword.equals("") && !email.equals(user.getEmail())) {
            updateEmail(oldPassword, newPassword, user, userdata);
        }
        if (newProfilePhoto) {
            if (photoUri != null) {
                uploadProfilePhoto(user, userdata);
            }
            else {
                hashMap.put("profilePhoto", "");
            }
        }
        hashMap.put("name", name);
        hashMap.put("status", "online");
        hashMap.put("writingTo", "none");
        hashMap.put("uid", user.getUid());
        hashMap.put("addressName", addressName);
        hashMap.put("address", address);
        hashMap.put("zipCode", zipCode);
        hashMap.put("town", town);
        hashMap.put("contactEmail", contactEmail);
        hashMap.put("telephoneNumber", telephoneNumber);
        hashMap.put("accountDescription", accountDescription);
        userdata.updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Your data has been updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void signUp(String name, String email, String password, String addressName, String address, String zipCode, String town, String contactEmail, String telephoneNumber, String accountDescription) {
        ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setMessage("Signing up...");
        progressDialog.show();
            firebaseAuth.createUserWithEmailAndPassword(email, password).
                    addOnCompleteListener(SignUpActivity.this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            DatabaseReference userdata = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                            HashMap<Object, String> hashMap = new HashMap<>();
                            if (photoUri != null) {
                                uploadProfilePhoto(user, userdata);
                            }
                            else {
                                hashMap.put("profilePhoto", "");
                            }
                            hashMap.put("email", user.getEmail());
                            hashMap.put("name", name);
                            hashMap.put("status", "online");
                            hashMap.put("writingTo", "none");
                            hashMap.put("uid", user.getUid());
                            hashMap.put("addressName", addressName);
                            hashMap.put("address", address);
                            hashMap.put("zipCode", zipCode);
                            hashMap.put("town", town);
                            hashMap.put("contactEmail", contactEmail);
                            hashMap.put("telephoneNumber", telephoneNumber);
                            hashMap.put("accountDescription", accountDescription);
                            hashMap.put("countPosted", "0");
                            hashMap.put("countRated", "0");
                            hashMap.put("countCommented", "0");
                            hashMap.put("countRates", "0");
                            hashMap.put("countComments", "0");
                            hashMap.put("countAchievements", "0");
                            hashMap.put("countWatchers", "0");
                            hashMap.put("countWatching", "0");
                            hashMap.put("averageRate", "0");
                            HashMap<String, String> hashMapAchievements = new HashMap<>();
                            hashMapAchievements.put("Post 5 posts", "0");
                            hashMapAchievements.put("Post 10 posts", "0");
                            hashMapAchievements.put("Post 20 posts", "0");
                            hashMapAchievements.put("Add 5 comments", "0");
                            hashMapAchievements.put("Add 10 comments", "0");
                            hashMapAchievements.put("Add 20 comments", "0");
                            hashMapAchievements.put("Rate 5 times", "0");
                            hashMapAchievements.put("Rate 10 times", "0");
                            hashMapAchievements.put("Rate 20 times", "0");
                            FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("Achievements").setValue(hashMapAchievements);
                            progressDialog.dismiss();
                            startActivity(new Intent(SignUpActivity.this, PanelActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(e -> {
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
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
