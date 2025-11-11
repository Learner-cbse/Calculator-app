package com.company.calculatar.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.company.calculatar.AppUser;
import com.company.calculatar.AppUtils;

import com.company.calculatar.PreferenceManager;
import com.company.calculatar.R;
import com.company.calculatar.constants.StringConstants;
import com.company.calculatar.databinding.ActivityCreateUserBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CreateUserActivity extends AppCompatActivity {

    private ActivityCreateUserBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityCreateUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        encodedImage = getDefaultBase64Image();
        setListeners();
        setUpUI();
    }

    private void setListeners() {
        binding.createUserBtn.setOnClickListener(v -> {
            if (isValidInput()) {
                checkEmailAndCreate();
            }
        });
    }

    private void checkEmailAndCreate() {
        setLoading(true);
        String email = binding.etEmail.getText().toString().trim();
        db.collection(StringConstants.KEY_USER)
                .whereEqualTo(StringConstants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        setLoading(false);
                        showToast("Email already exists.");
                    } else {
                        createUser();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showToast("Check failed: " + e.getMessage());
                });
    }

    private void createUser() {
        auth.signInAnonymously().addOnSuccessListener(authResult -> {
            String GUID = "GUID" + UUID.randomUUID().toString()
                    .replaceAll("[^a-zA-Z0-9]", "")
                    .substring(0, 16)
                    .toUpperCase();

            String rawPassword = binding.etPassword.getText().toString().trim();
            String hashedPassword = AppUtils.hashPassword(rawPassword);

            HashMap<String, Object> user = new HashMap<>();
            user.put(StringConstants.KEY_USER_ID, GUID);
            user.put(StringConstants.NAME, binding.etName.getText().toString().trim());
            user.put(StringConstants.KEY_EMAIL, hashedPassword);
            user.put(StringConstants.KEY_PASSWORD, binding.etPassword.getText().toString().trim()); // stored as plain for anonymous login
            user.put(StringConstants.AVATAR, encodedImage);
            user.put(StringConstants.KEY_USER_ABOUT, "Hey there! I'm member of Love Chat.");
            user.put(StringConstants.KEY_TIMESTAMP, Timestamp.now());

            db.collection(StringConstants.KEY_USER)
                    .document(GUID)
                    .set(user)
                    .addOnSuccessListener(unused -> {
                        setLoading(false);
                        preferenceManager.putBoolean(StringConstants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(StringConstants.KEY_USER_ID, GUID);
                        preferenceManager.putString(StringConstants.NAME, binding.etName.getText().toString().trim());
                        preferenceManager.putString(StringConstants.KEY_EMAIL, binding.etEmail.getText().toString().trim());
                        preferenceManager.putString(StringConstants.AVATAR, encodedImage);


                        AppUtils.fetchDefaultObjects(new AppUtils.UserListCallback() {
                            @Override
                            public void onSuccess(List<AppUser> users) {
                                // Use default user if needed
                                if (!users.isEmpty()) {
                                    AppUser defaultUser = users.get(0);
                                    preferenceManager.putString("default_user_uid", defaultUser.getUid());
                                    preferenceManager.putString("default_user_name", defaultUser.getName());
                                }

                                // Go to HomeActivity after fetching default users
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(CreateUserActivity.this, "Failed to fetch default users: " + error, Toast.LENGTH_SHORT).show();

                                // Still go to HomeActivity even if default users fail
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });


                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        showToast("User creation failed: " + e.getMessage());
                    });
        }).addOnFailureListener(e -> {
            setLoading(false);
            showToast("Anonymous sign-in failed: " + e.getMessage());
        });
    }

    private boolean isValidInput() {
        if (binding.etName.getText().toString().trim().length() > 6) {
            showToast("Enter at least 6 characters");
            return false;
        } else if (binding.etEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email(name@example.com)");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.getText().toString().trim()).matches()) {
            showToast("Invalid email(name@example.com)");
            return false;
        } else if (!binding.etPassword.getText().toString().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            showToast("8 characters,a-z,A-z,0-9");
            return false;
        } else if (binding.etConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;
        } else if (!binding.etPassword.getText().toString().equals(binding.etConfirmPassword.getText().toString())) {
            showToast("Passwords do not match");
            return false;
        }
        return true;
    }

    private String getDefaultBase64Image() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private void setUpUI() {
        if (AppUtils.isNightMode(this)) {
            AppUtils.changeTextColorToWhite(this, binding.tvTitle);
            AppUtils.changeTextColorToWhite(this, binding.tvDes2);
            binding.etEmail.setTextColor(getResources().getColor(R.color.white));
            binding.etName.setTextColor(getResources().getColor(R.color.white));
            binding.etPassword.setTextColor(getResources().getColor(R.color.white));
            binding.etConfirmPassword.setTextColor(getResources().getColor(R.color.white));
        } else {
            AppUtils.changeTextColorToBlack(this, binding.tvTitle);
            AppUtils.changeTextColorToBlack(this, binding.tvDes2);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setLoading(boolean isLoading) {
        binding.createUserBtn.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        binding.createUserPb.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
