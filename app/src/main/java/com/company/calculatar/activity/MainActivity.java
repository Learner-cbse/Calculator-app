package com.company.calculatar.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.company.calculatar.AppUser;
import com.company.calculatar.AppUtils;
import com.company.calculatar.PreferenceManager;
import com.company.calculatar.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView user1, user2, user3, user4;
    private ImageView user1Avatar, user2Avatar, user3Avatar, user4Avatar;
    private TextView user1Name, user2Name, user3Name, user4Name;
    private ProgressBar progressBar;
    private TextView stateMessage;
    private LinearLayout stateLayout, parentView, gridLayoutContainer;
    private AppCompatImageView ivLogo;
    private AppCompatTextView tvCometChat;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        initViews();
        applyUITheme();

        if (firebaseAuth.getCurrentUser() != null) {
            AppUtils.goToHomeAfterFetchingUsers(getApplicationContext(), preferenceManager);
        } else {
            fetchSampleUsers();
        }

        findViewById(R.id.login).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        findViewById(R.id.user1).setOnClickListener(v -> loginUser((String) user1.getTag()));
        findViewById(R.id.user2).setOnClickListener(v -> loginUser((String) user2.getTag()));
        findViewById(R.id.user3).setOnClickListener(v -> loginUser((String) user3.getTag()));
        findViewById(R.id.user4).setOnClickListener(v -> loginUser((String) user4.getTag()));
    }

    private void initViews() {
        parentView = findViewById(R.id.parent_view);
        progressBar = findViewById(R.id.progress_bar);
        stateMessage = findViewById(R.id.state_message);
        stateLayout = findViewById(R.id.state_layout);
        gridLayoutContainer = findViewById(R.id.grid_layout_container);
        user1 = findViewById(R.id.user1);
        user2 = findViewById(R.id.user2);
        user3 = findViewById(R.id.user3);
        user4 = findViewById(R.id.user4);
        user1Name = findViewById(R.id.user1_name);
        user2Name = findViewById(R.id.user2_name);
        user3Name = findViewById(R.id.user3_name);
        user4Name = findViewById(R.id.user4_name);
        user1Avatar = findViewById(R.id.user1_avatar_image);
        user2Avatar = findViewById(R.id.user2_avatar_image);
        user3Avatar = findViewById(R.id.user3_avatar_image);
        user4Avatar = findViewById(R.id.user4_avatar_image);
        ivLogo = findViewById(R.id.ivLogo);
        tvCometChat = findViewById(R.id.tvComet);

        user1.setVisibility(View.GONE);
        user2.setVisibility(View.GONE);
        user3.setVisibility(View.GONE);
        user4.setVisibility(View.GONE);
        gridLayoutContainer.setVisibility(View.INVISIBLE);
        stateLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        stateMessage.setText(R.string.please_wait);
    }

    private void applyUITheme() {
        if (AppUtils.isNightMode(this)) {
            parentView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_background_dark)));
            tvCometChat.setTextColor(getResources().getColor(R.color.app_background));
        } else {
            parentView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_background)));
            tvCometChat.setTextColor(getResources().getColor(R.color.app_background_dark));
        }
    }

    private void fetchSampleUsers() {
        CollectionReference usersRef = firestore.collection("users");

        usersRef.limit(4).get().addOnSuccessListener(querySnapshot -> {
            List<DocumentSnapshot> docs = querySnapshot.getDocuments();
            if (!docs.isEmpty()) {
                for (int i = 0; i < docs.size(); i++) {
                    DocumentSnapshot doc = docs.get(i);
                    String name = doc.getString("name");
                    String avatar = doc.getString("avatar");
                    String email = doc.getString("email");
                    setUserCard(i, name, avatar, email);
                }
                progressBar.setVisibility(View.GONE);
                stateLayout.setVisibility(View.GONE);
                gridLayoutContainer.setVisibility(View.VISIBLE);
            } else {
                stateMessage.setText(R.string.no_sample_users_available);
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            stateMessage.setText("Failed to load users: " + e.getMessage());
            progressBar.setVisibility(View.GONE);
        });
    }

    private void setUserCard(int index, String name, String avatar, String email) {
        switch (index) {
            case 0:
                user1Name.setText(name);
                Glide.with(this).load(avatar).error(R.drawable.ironman).into(user1Avatar);
                user1.setTag(email);
                user1.setVisibility(View.VISIBLE);
                break;
            case 1:
                user2Name.setText(name);
                Glide.with(this).load(avatar).error(R.drawable.captainamerica).into(user2Avatar);
                user2.setTag(email);
                user2.setVisibility(View.VISIBLE);
                break;
            case 2:
                user3Name.setText(name);
                Glide.with(this).load(avatar).error(R.drawable.spiderman).into(user3Avatar);
                user3.setTag(email);
                user3.setVisibility(View.VISIBLE);
                break;
            case 3:
                user4Name.setText(name);
                Glide.with(this).load(avatar).error(R.drawable.wolverine).into(user4Avatar);
                user4.setTag(email);
                user4.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void loginUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        // WARNING: This uses a hardcoded password for demo. Replace with secure handling in production.
        String password = "Raksha@123"; // You must store this securely for real apps

        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    AppUtils.goToHomeAfterFetchingUsers(getApplicationContext(), preferenceManager);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void createUser(View view) {
        startActivity(new Intent(this, CreateUserActivity.class));
    }
}
