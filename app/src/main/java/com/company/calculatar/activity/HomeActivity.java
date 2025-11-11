package com.company.calculatar.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.company.calculatar.AppUtils;
import com.company.calculatar.PreferenceManager;
import com.company.calculatar.R;
import com.company.calculatar.constants.StringConstants;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private ImageView darkMode, lightMode, logout;
    private LinearLayout parentView;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        darkMode = findViewById(R.id.dark_mode);
        lightMode = findViewById(R.id.light_mode);
        logout = findViewById(R.id.logout);
        parentView = findViewById(R.id.parent_view);
        preferenceManager = new PreferenceManager(getApplicationContext());

        setUpUI();

        findViewById(R.id.chats).setOnClickListener(view -> handleIntent(StringConstants.CONVERSATIONS));
        findViewById(R.id.users).setOnClickListener(view -> handleIntent(StringConstants.USERS));
        findViewById(R.id.groups).setOnClickListener(view -> handleIntent(StringConstants.GROUPS));
        findViewById(R.id.messages).setOnClickListener(view -> handleIntent(StringConstants.MESSAGES));
        findViewById(R.id.shared).setOnClickListener(view -> handleIntent(StringConstants.SHARED));
        findViewById(R.id.calls).setOnClickListener(view -> handleIntent(StringConstants.CALLS));

        logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            preferenceManager.clear(); // Clear saved user data
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finishAffinity();
        });

        darkMode.setOnClickListener(view -> toggleDarkMode());
        lightMode.setOnClickListener(view -> toggleDarkMode());
    }

    private void setUpUI() {
        if (AppUtils.isNightMode(this)) {
            AppUtils.changeIconTintToWhite(this, darkMode);
            AppUtils.changeIconTintToWhite(this, lightMode);
            AppUtils.changeIconTintToWhite(this, logout);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.app_background_dark));
            parentView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_background_dark)));
            darkMode.setVisibility(View.GONE);
            lightMode.setVisibility(View.VISIBLE);
        } else {
            AppUtils.changeIconTintToBlack(this, darkMode);
            AppUtils.changeIconTintToBlack(this, lightMode);
            AppUtils.changeIconTintToBlack(this, logout);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.app_background));
            parentView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_background)));
            darkMode.setVisibility(View.VISIBLE);
            lightMode.setVisibility(View.GONE);
        }
    }

    private void toggleDarkMode() {
        if (AppUtils.isNightMode(this)) {
            AppUtils.switchLightMode();
        } else {
            AppUtils.switchDarkMode();
        }
        recreate(); // Refresh UI
    }

    private void handleIntent(String module) {
        Intent intent = new Intent(this, ComponentListActivity.class);
        intent.putExtra(StringConstants.MODULE, module);
        startActivity(intent);
    }
}
