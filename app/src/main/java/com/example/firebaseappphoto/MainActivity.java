package com.example.firebaseappphoto;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private ProgressDialog progressDialog;
    private TextInputEditText tietEmail, tietPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        tietEmail = findViewById(R.id.tietEmailMain);
        tietPassword = findViewById(R.id.tietPasswordMain);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        googleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build());
        findViewById(R.id.btnGoogleSignInMain).setOnClickListener(v -> activityResultLauncher.launch(new Intent(googleSignInClient.getSignInIntent())));
        findViewById(R.id.btnSignInMain).setOnClickListener(v -> {
            String email = tietEmail.getText().toString();
            String password = tietPassword.getText().toString().trim();
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if(password.length() >= 6){
                    signIn(email, password);
                }
                else {
                    tietPassword.setFocusable(true);
                    tietPassword.setError("Too short password");
                }
            }
            else {
                tietEmail.setFocusable(true);
                tietEmail.setError("Wrong email");
            }
        });
        findViewById(R.id.btnSignUpMain).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            intent.putExtra("account", "new");
            startActivity(intent);
        });
        findViewById(R.id.tvRecoverPasswordMain).setOnClickListener(v -> showRecoverPasswordDialog());
    }

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        firebaseAuthWithGoogle(GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                .getResult(ApiException.class)
                                .getIdToken());
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this,
                                e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void firebaseAuthWithGoogle(String idToken) {
        progressDialog.setMessage("Signing in...");
        progressDialog.show();
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("email", user.getEmail());
                            hashMap.put("name", user.getDisplayName());
                            hashMap.put("status", "online");
                            hashMap.put("writingTo", "none");
                            hashMap.put("uid", user.getUid());
                            hashMap.put("profilePhoto", user.getPhotoUrl().toString());
                            hashMap.put("addressName", "");
                            hashMap.put("address", "");
                            hashMap.put("zipCode", "");
                            hashMap.put("town", "");
                            hashMap.put("contactEmail", "");
                            hashMap.put("telephoneNumber", "");
                            hashMap.put("accountDescription", "");
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
                            FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("Achievements")
                                    .setValue(hashMapAchievements);
                        }
                        startActivity(new Intent(MainActivity.this, PanelActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

    private void recoverYourPassword(String emailForRecovery) {
        progressDialog.setMessage("Sending email...");
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(emailForRecovery).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(MainActivity.this, "Email has been sent.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    private void showRecoverPasswordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_recover_password, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();
        TextInputEditText tietEmailForRecovery = view.findViewById(R.id.tietEmailForRecoveryRecoverPassword);
        view.findViewById(R.id.btnRecoverPasswordRecoverPassword).setOnClickListener(v -> {
            recoverYourPassword(tietEmailForRecovery.getText().toString().trim());
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    private void signIn(String email, String password) {
        progressDialog.setMessage("Signing In...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                startActivity(new Intent(MainActivity.this, PanelActivity.class));
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnFailureListener((e) -> {
            Toast.makeText(com.example.firebaseappphoto.MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }
}