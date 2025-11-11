package com.company.calculatar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.company.calculatar.AppUtils;
import com.company.calculatar.PreferenceManager;
import com.company.calculatar.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private PreferenceManager preferenceManager;
    private boolean wasInBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();
        setUpUI();

        if (preferenceManager.getBoolean("is_signed_in", false)) {
            startActivity(new Intent(this, com.company.calculatar.activity.HomeActivity.class));
            finish();
        }
    }

    private void setListeners() {
        binding.tvSignIn.setOnClickListener(v -> {
            if (isValidInput()) {
                signIn();
            }
        });

        binding.textCreateUser.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CreateUserActivity.class))
        );
    }

    private boolean isValidInput() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.inputEmail.setError("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.setError("Enter a valid email");
            return false;
        } else {
            binding.inputEmail.setError(null);
        }

        if (password.isEmpty()) {
            binding.inputPassword.setError("Enter password");
            return false;
        } else {
            binding.inputPassword.setError(null);
        }

        return true;
    }

    private void signIn() {
        setLoading(true);
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        fetchUserProfile(user.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showToast("Login failed: " + e.getMessage());
                });
    }

    private void fetchUserProfile(String uid) {
        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        preferenceManager.putBoolean("is_signed_in", true);
                        preferenceManager.putString("user_id", uid);
                        preferenceManager.putString("name", documentSnapshot.getString("name"));
                        preferenceManager.putString("avatar", documentSnapshot.getString("avatar"));
                        preferenceManager.putString("email", documentSnapshot.getString("email"));

                        AppUtils.fetchDefaultObjects(new AppUtils.UserListCallback() {
                            @Override
                            public void onSuccess(java.util.List<com.company.calculatar.AppUser> users) {
                                goToHome();
                            }

                            @Override
                            public void onError(String error) {
                                showToast("Login succeeded, but failed to load users: " + error);
                                goToHome();
                            }
                        });
                    } else {
                        setLoading(false);
                        showToast("User profile not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showToast("Error fetching profile: " + e.getMessage());
                });
    }

    private void setLoading(boolean isLoading) {
        binding.loginProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.tvSignIn.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void goToHome() {
        startActivity(new Intent(LoginActivity.this, com.company.calculatar.activity.HomeActivity.class));
        finishAffinity();
    }

    private void setUpUI() {
        if (AppUtils.isNightMode(this)) {
            AppUtils.changeTextColorToWhite(this, binding.tvTitle);
            AppUtils.changeTextColorToWhite(this, binding.tvDes2);
            binding.etEmail.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            binding.etPassword.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            AppUtils.changeTextColorToBlack(this, binding.tvTitle);
            AppUtils.changeTextColorToBlack(this, binding.tvDes2);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity(); // Prevent navigating back
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            wasInBackground = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasInBackground) {
            wasInBackground = false;
            startActivity(new Intent(this, CalculatorActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
    }
}
