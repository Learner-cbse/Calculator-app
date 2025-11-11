package com.company.calculatar.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.company.calculatar.R;
import com.google.android.material.button.MaterialButton;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.Stack;

public class CalculatorActivity extends AppCompatActivity {

    private TextView inputDisplay, outputDisplay;
    private DecimalFormat decimalFormat;
    private boolean hasPersistentError = false;
    private boolean shouldLockOnResume = false;

    private static final String PIN_FILE_NAME = ".secret_pin.txt";
    private static final String PREFS = "PinPrefs";
    private static final String KEY_ATTEMPTS = "wrong_attempts";
    private static final String KEY_LOCK_TIME = "lock_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Notification permission check for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        decimalFormat = new DecimalFormat("#.######");
        inputDisplay = findViewById(R.id.input);
        outputDisplay = findViewById(R.id.output);

        int[] digitButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnPoint
        };

        View.OnClickListener digitClickListener = v -> {
            MaterialButton btn = (MaterialButton) v;
            inputDisplay.append(btn.getText().toString());
            setTypingMode();
            evaluateLive();
        };

        for (int id : digitButtons) {
            findViewById(id).setOnClickListener(digitClickListener);
        }

        findViewById(R.id.add).setOnClickListener(v -> {
            appendOperator("+");
            setTypingMode();
        });
        findViewById(R.id.subtract).setOnClickListener(v -> {
            appendOperator("-");
            setTypingMode();
        });
        findViewById(R.id.multiply).setOnClickListener(v -> {
            appendOperator("*");
            setTypingMode();
        });
        findViewById(R.id.division).setOnClickListener(v -> {
            appendOperator("/");
            setTypingMode();
        });
        findViewById(R.id.percent).setOnClickListener(v -> {
            appendOperator("/100");
            setTypingMode();
        });

        findViewById(R.id.equal).setOnClickListener(v -> handleEqual());

        findViewById(R.id.clear).setOnClickListener(v -> {
            String text = inputDisplay.getText().toString();
            if (!text.isEmpty()) {
                inputDisplay.setText(text.substring(0, text.length() - 1));
                setTypingMode();
                evaluateLive();
            }
        });

        findViewById(R.id.off).setOnClickListener(v -> {
            inputDisplay.setText("");
            outputDisplay.setText("");
            hasPersistentError = false;
            setTypingMode();
        });

        if (!isPinSet()) {
            promptSetPin();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        shouldLockOnResume = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldLockOnResume) {
            inputDisplay.setText("");
            outputDisplay.setText("");
            hasPersistentError = false;
            setTypingMode();
            shouldLockOnResume = false;
        }
    }

    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
        inputDisplay.setText("");
        outputDisplay.setText("");
        finishAffinity(); // Secure exit — closes all activities
    }


    private void handleEqual() {
        String expr = inputDisplay.getText().toString().replaceAll("\\s+", "");

        if (expr.length() == 6 && expr.matches("\\d{6}")) {
            if (isLocked()) {
                long timeLeft = getRemainingLockTime() / 1000;
                Toast.makeText(this, "Locked. Try again in " + timeLeft + " sec", Toast.LENGTH_SHORT).show();
                return;
            }

            String storedHash = loadSavedPinHash();
            if (storedHash != null && storedHash.equals(hashPin(expr))) {
                resetWrongAttempts();
                startActivity(new Intent(CalculatorActivity.this, LoginActivity.class));
                return;
            } else {
                incrementWrongAttempts();

                // Act like it's a normal calculator:
                try {
                    double result = evaluate(expr);
                    outputDisplay.setTextColor(Color.BLACK);
                    outputDisplay.setText("=" + decimalFormat.format(result));
                    setResultMode();
                    hasPersistentError = false;
                } catch (Exception e) {
                    outputDisplay.setTextColor(Color.RED);
                    outputDisplay.setText("Error");
                    hasPersistentError = true;
                }

                return;
            }

        }

        if (isInvalidExpression(expr)) {
            outputDisplay.setTextColor(Color.RED);
            outputDisplay.setText("Error");
            hasPersistentError = true;
            return;
        }

        try {
            double result = evaluate(expr);
            outputDisplay.setTextColor(Color.BLACK);
            outputDisplay.setText("=" + decimalFormat.format(result));
            setResultMode();
            hasPersistentError = false;
        } catch (Exception e) {
            outputDisplay.setTextColor(Color.RED);
            outputDisplay.setText("Error");
            hasPersistentError = true;
        }
    }

    private void promptSetPin() {
        android.widget.EditText pinInput = new android.widget.EditText(this);
        pinInput.setHint("Enter 6-digit PIN");
        pinInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Set PIN")
                .setMessage("Enter a 6-digit PIN to unlock hidden mode.")
                .setView(pinInput)
                .setCancelable(false)
                .setPositiveButton("Save", (dialog, which) -> {
                    String pin = pinInput.getText().toString().trim();
                    if (pin.length() == 6 && pin.matches("\\d+")) {
                        savePinHash(hashPin(pin));
                        Toast.makeText(this, "PIN saved!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Invalid PIN. Try again.", Toast.LENGTH_LONG).show();
                        promptSetPin();
                    }
                })
                .show();
    }

    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void savePinHash(String hashedPin) {
        try (FileOutputStream fos = openFileOutput(PIN_FILE_NAME, MODE_PRIVATE)) {
            fos.write(hashedPin.getBytes());
        } catch (Exception e) {
            Toast.makeText(this, "Error saving PIN", Toast.LENGTH_SHORT).show();
        }
    }

    private String loadSavedPinHash() {
        try (FileInputStream fis = openFileInput(PIN_FILE_NAME);
             Scanner scanner = new Scanner(fis)) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isPinSet() {
        File file = new File(getFilesDir(), PIN_FILE_NAME);
        return file.exists();
    }

    private void wipeAppData() {
        File pinFile = new File(getFilesDir(), PIN_FILE_NAME);
        if (pinFile.exists()) pinFile.delete();

        getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply();

        inputDisplay.setText("");
        outputDisplay.setText("");

        Toast.makeText(this, "Too many wrong attempts. Data wiped.", Toast.LENGTH_LONG).show();

        Intent i = new Intent(getApplicationContext(), CalculatorActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void incrementWrongAttempts() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        int attempts = prefs.getInt(KEY_ATTEMPTS, 0) + 1;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_ATTEMPTS, attempts);

        long lockDurationMillis = 0;

        switch (attempts) {
            case 3: lockDurationMillis = 12 * 60 * 60 * 1000L; break;
            case 4: lockDurationMillis = 24 * 60 * 60 * 1000L; break;
            case 5: lockDurationMillis = 2 * 24 * 60 * 60 * 1000L; break;
            case 6: lockDurationMillis = 3 * 24 * 60 * 60 * 1000L; break;
            case 7: lockDurationMillis = 4 * 24 * 60 * 60 * 1000L; break;
            case 8: lockDurationMillis = 5 * 24 * 60 * 60 * 1000L; break;
            case 9: lockDurationMillis = 6 * 24 * 60 * 60 * 1000L; break;
            case 10:
                wipeAppData();
                return;
        }

        if (lockDurationMillis > 0) {
            editor.putLong(KEY_LOCK_TIME, SystemClock.elapsedRealtime() + lockDurationMillis);
        }

        editor.apply();
    }

    private void resetWrongAttempts() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putInt(KEY_ATTEMPTS, 0)
                .remove(KEY_LOCK_TIME)
                .apply();
    }

    private boolean isLocked() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        long lockTime = prefs.getLong(KEY_LOCK_TIME, 0);
        return SystemClock.elapsedRealtime() < lockTime;
    }

    private long getRemainingLockTime() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        long lockTime = prefs.getLong(KEY_LOCK_TIME, 0);
        return Math.max(0, lockTime - SystemClock.elapsedRealtime());
    }

    private void appendOperator(String op) {
        String current = inputDisplay.getText().toString();
        if (current.isEmpty() && !op.equals("-")) return;

        inputDisplay.append(op);
        evaluateLive();
    }

    private void evaluateLive() {
        String expr = inputDisplay.getText().toString();

        if (isInvalidExpression(expr)) {
            outputDisplay.setTextColor(Color.RED);
            outputDisplay.setText("Error");
            hasPersistentError = true;
            return;
        } else if (hasPersistentError) {
            hasPersistentError = false;
        }

        try {
            double result = evaluate(expr);
            outputDisplay.setTextColor(Color.parseColor("#444444"));
            outputDisplay.setText("=" + decimalFormat.format(result));
        } catch (Exception e) {
            outputDisplay.setText("");
        }
    }

    private boolean isInvalidExpression(String expr) {
        return expr.matches(".*[.][+*/].*") ||
                expr.matches(".*[+*/]{2,}.*") ||
                expr.endsWith(".") ||
                expr.startsWith(".") ||
                expr.matches(".*[+*/]-?$");
    }

    private void setTypingMode() {
        inputDisplay.setTextSize(45);
        outputDisplay.setTextSize(25);
        if (!hasPersistentError) {
            outputDisplay.setTextColor(Color.parseColor("#444444"));
        }
    }

    private void setResultMode() {
        inputDisplay.setTextSize(25);
        outputDisplay.setTextSize(45);
        outputDisplay.setTextColor(Color.BLACK);
    }

    private double evaluate(String expression) {
        return evaluatePostfix(toPostfix(expression));
    }

    private String toPostfix(String infix) {
        StringBuilder output = new StringBuilder();
        Stack<Character> ops = new Stack<>();
        int i = 0;
        while (i < infix.length()) {
            char c = infix.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                while (i < infix.length() &&
                        (Character.isDigit(infix.charAt(i)) || infix.charAt(i) == '.')) {
                    output.append(infix.charAt(i));
                    i++;
                }
                output.append(' ');
                continue;
            }

            if (isOperator(c)) {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(c)) {
                    output.append(ops.pop()).append(' ');
                }
                ops.push(c);
            }
            i++;
        }

        while (!ops.isEmpty()) {
            output.append(ops.pop()).append(' ');
        }

        return output.toString();
    }

    private double evaluatePostfix(String postfix) {
        Stack<Double> stack = new Stack<>();
        String[] tokens = postfix.trim().split("\\s+");

        for (String token : tokens) {
            if (token.length() == 1 && isOperator(token.charAt(0))) {
                double b = stack.pop();
                double a = stack.pop();
                switch (token.charAt(0)) {
                    case '+': stack.push(a + b); break;
                    case '-': stack.push(a - b); break;
                    case '*': stack.push(a * b); break;
                    case '/': stack.push(a / b); break;
                }
            } else {
                stack.push(Double.parseDouble(token));
            }
        }

        return stack.pop();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calculator_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_help) {
            startActivity(new Intent(this, HelpsActivity.class));
            return true;
        } else if (id == R.id.action_privacy) {
            startActivity(new Intent(this, PrivacyPolicyActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
