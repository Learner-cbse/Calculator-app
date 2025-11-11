package com.company.calculatar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.company.calculatar.R;


public class HelpsActivity extends AppCompatActivity {

    private boolean wasInBackground = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helps);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

    }

    // Detect real backgrounding
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

            Intent intent = new Intent(this, CalculatorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();  // Prevent back to this activity
        }
    }

}