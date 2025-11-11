package com.company.calculatar;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.company.calculatar.activity.HomeActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AppUtils {

    private static List<com.company.calculatar.AppUser> userList = new ArrayList<>();

    public interface UserListCallback {
        void onSuccess(List<com.company.calculatar.AppUser> users);
        void onError(String error);
    }


    public static void fetchDefaultObjects(UserListCallback callback) {
        if (!userList.isEmpty()) {
            callback.onSuccess(userList);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<AppUser> users = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String uid = doc.getString("uid");
                            String name = doc.getString("name");
                            String avatar = doc.getString("avatar");

                            if (uid != null && name != null) {
                                users.add(new AppUser(uid, name, avatar));
                            }
                        }
                        userList = users;
                        callback.onSuccess(users);
                    } else {
                        String error = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Unknown Firestore error";
                        callback.onError(error);
                    }
                });
    }
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // fallback
        }
    }

    public static void goToHomeAfterFetchingUsers(Context context, PreferenceManager pref) {
        fetchDefaultObjects(new UserListCallback() {
            @Override
            public void onSuccess(List<AppUser> users) {
                if (!users.isEmpty()) {
                    AppUser defaultUser = users.get(0);
                    pref.putString("default_user_uid", defaultUser.getUid());
                    pref.putString("default_user_name", defaultUser.getName());
                }
                goToHome(context);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(context, "Failed to fetch default users: " + error, Toast.LENGTH_SHORT).show();
                goToHome(context);
            }

            private void goToHome(Context ctx) {
                Intent intent = new Intent(ctx, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ctx.startActivity(intent);
            }
        });
    }



    public static String loadJSONFromAsset(Context context) {
        try {
            InputStream is = context.getAssets().open("SampleUsers.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void switchLightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void switchDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public static boolean isNightMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void changeIconTintToWhite(Context context, ImageView imageView) {
        imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
    }

    public static void changeIconTintToBlack(Context context, ImageView imageView) {
        imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)));
    }

    public static void changeTextColorToWhite(Context context, TextView textView) {
        textView.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    public static void changeTextColorToBlack(Context context, TextView textView) {
        textView.setTextColor(ContextCompat.getColor(context, R.color.black));
    }
}
