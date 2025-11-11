package com.company.calculatar.fragments.calls;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.company.calculatar.PreferenceManager;
import com.company.calculatar.R;
import com.company.calculatar.constants.StringConstants;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CallButtonFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 101;

    private FirebaseFirestore db;
    private PreferenceManager pref;
    private String callType; // voice or video

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_button, container, false);

        db = FirebaseFirestore.getInstance();
        pref = new PreferenceManager(requireContext());

        ImageButton voiceCallBtn = view.findViewById(R.id.btn_voice_call);
        ImageButton videoCallBtn = view.findViewById(R.id.btn_video_call);

        voiceCallBtn.setOnClickListener(v -> {
            callType = StringConstants.TYPE_VOICE;
            checkPermissionsAndCall();
        });

        videoCallBtn.setOnClickListener(v -> {
            callType = StringConstants.TYPE_VIDEO;
            checkPermissionsAndCall();
        });

        return view;
    }

    private void checkPermissionsAndCall() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        } else {
            initiateCall(callType);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                initiateCall(callType);
            } else {
                Toast.makeText(requireContext(), "Camera & Microphone permission are required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initiateCall(String type) {
        String callerUid = pref.getString(StringConstants.KEY_USER_ID);
        String receiverUid = pref.getString("default_user_uid"); // set during user loading

        if (callerUid == null || receiverUid == null) {
            Toast.makeText(getContext(), "User information missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String roomId = callerUid + "_" + receiverUid + "_" + System.currentTimeMillis();

        Map<String, Object> callData = new HashMap<>();
        callData.put(StringConstants.TYPE, type);
        callData.put(StringConstants.ROOM_ID, roomId);
        callData.put(StringConstants.CALLER, callerUid);
        callData.put(StringConstants.RECEIVER, receiverUid);
        callData.put(StringConstants.KEY_TIMESTAMP, System.currentTimeMillis());

        db.collection(StringConstants.CALLS)
                .document(roomId)
                .set(callData)
                .addOnSuccessListener(unused -> startJitsiCall(roomId, type))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Call failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void startJitsiCall(String roomId, String type) {
        try {
            JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
            userInfo.setDisplayName(pref.getString(StringConstants.NAME));
            userInfo.setEmail(pref.getString(StringConstants.KEY_EMAIL));

            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(roomId)
                    .setUserInfo(userInfo)
                    .setAudioMuted(false)
                    .setVideoMuted(type.equals(StringConstants.TYPE_VOICE))
                    .setWelcomePageEnabled(false)
                    .build();

            JitsiMeetActivity.launch(requireContext(), options);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Invalid Jitsi server URL", Toast.LENGTH_SHORT).show();
        }
    }
}
