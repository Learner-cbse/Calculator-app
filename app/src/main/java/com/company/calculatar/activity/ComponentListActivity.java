package com.company.calculatar.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.company.calculatar.AppUtils;
import com.company.calculatar.R;
import com.company.calculatar.constants.StringConstants;

public class ComponentListActivity extends AppCompatActivity {
    LinearLayout parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component_list);
        parentView = findViewById(R.id.parent_view);
        TextView title = findViewById(R.id.title);

        setUpUI();

        if (getIntent() != null) {
            String module = getIntent().getStringExtra(StringConstants.MODULE);
            title.setText(module);

            switch (module) {
                case StringConstants.CONVERSATIONS:
                    findViewById(R.id.moduleChats).setVisibility(View.VISIBLE);
                    break;
                case StringConstants.USERS:
                    findViewById(R.id.moduleUsers).setVisibility(View.VISIBLE);
                    break;
                case StringConstants.GROUPS:
                    findViewById(R.id.noduleGroups).setVisibility(View.VISIBLE);
                    break;
                case StringConstants.MESSAGES:
                    findViewById(R.id.moduleMessages).setVisibility(View.VISIBLE);
                    break;
                case StringConstants.SHARED:
                    findViewById(R.id.shared).setVisibility(View.VISIBLE);
                    break;
                case StringConstants.CALLS:
                    findViewById(R.id.module_calls).setVisibility(View.VISIBLE);
                    break;
            }
        }

        // Back button
        findViewById(R.id.backIcon).setOnClickListener(view -> onBackPressed());

        // All click listeners
        int[] componentIds = new int[]{
                R.id.conversationWithMessages, R.id.conversations, R.id.contacts,
                R.id.userWithMessages, R.id.users, R.id.user_details,
                R.id.groupWithMessages, R.id.groups, R.id.create_group,
                R.id.join_protected_group, R.id.group_member, R.id.add_member,
                R.id.transfer_ownership, R.id.banned_members, R.id.group_details,
                R.id.messages, R.id.messageList, R.id.messageHeader, R.id.messageComposer, R.id.messageInformation,
                R.id.call_button, R.id.call_logs, R.id.call_logs_details, R.id.call_logs_with_details,
                R.id.call_log_participants, R.id.call_log_recording, R.id.call_log_history,
                R.id.avatar, R.id.badgeCount, R.id.messageReceipt, R.id.statusIndicator,
                R.id.list_item, R.id.text_bubble, R.id.image_bubble, R.id.video_bubble,
                R.id.audio_bubble, R.id.files_bubble, R.id.form_bubble, R.id.card_bubble,
                R.id.scheduler_bubble, R.id.media_recorder,
                R.id.soundManager, R.id.theme, R.id.localize
        };

        for (int id : componentIds) {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(v -> handleIntent(id));
            }
        }
    }

    private void setUpUI() {
        if (AppUtils.isNightMode(this)) {
            // Set icon tint to white
            setAllIconTint(true);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.app_background_dark));
            parentView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_background_dark)));
        } else {
            // Set icon tint to black
            setAllIconTint(false);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.app_background));
            parentView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_background)));
        }
    }

    private void setAllIconTint(boolean isWhite) {
        int[] iconIds = new int[]{
                R.id.backIcon, R.id.image_cwm, R.id.image_c, R.id.image_contacts,
                R.id.image_uwm, R.id.image_u, R.id.image_ud,
                R.id.image_gwm, R.id.image_g, R.id.image_cg, R.id.image_jp, R.id.image_gm,
                R.id.image_ad, R.id.image_to, R.id.image_bm, R.id.image_gd,
                R.id.image_message, R.id.image_message_header, R.id.image_message_list,
                R.id.image_message_composer, R.id.image_message_information,
                R.id.image_call_button, R.id.image_audio, R.id.image_translate,
                R.id.image_avatar, R.id.image_badge_count, R.id.image_message_receipt,
                R.id.image_status_indicator, R.id.image_text_bubble, R.id.image_image_bubble,
                R.id.image_video_bubble, R.id.image_audio_bubble, R.id.image_file_bubble,
                R.id.image_form_bubble, R.id.image_card_bubble, R.id.image_scheduler_bubble,
                R.id.image_mic, R.id.image_list_item
        };

        for (int id : iconIds) {
            if (isWhite) {
                AppUtils.changeIconTintToWhite(this, findViewById(id));
            } else {
                AppUtils.changeIconTintToBlack(this, findViewById(id));
            }
        }
    }

    private void handleIntent(int id) {
        Intent intent = new Intent(this, ComponentLaunchActivity.class);
        intent.putExtra("component", id);
        startActivity(intent);
    }
}
