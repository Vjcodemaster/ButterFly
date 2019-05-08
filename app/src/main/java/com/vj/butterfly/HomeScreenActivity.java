package com.vj.butterfly;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import app_utility.DataBaseHelper;
import app_utility.DatabaseHandler;
import app_utility.MessageService;
import app_utility.OnChatInterfaceListener;

public class HomeScreenActivity extends AppCompatActivity implements OnChatInterfaceListener {
    LottieAnimationView lottieAnimationView;

    Fragment newFragment;
    FragmentTransaction transaction;
    String sBackStackParent;
    LinearLayout llChatItem;

    TextView tvLastMessage, tvThink, tvTime;

    DatabaseHandler dbh;
    public static OnChatInterfaceListener onChatInterfaceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        initClasses();
        initViews();
        initListeners();
        //Log.e("asdasd", TimestampUtil.getCurrentTimestampStringFormat());
        //loadChatFragment();
        Intent in = new Intent(HomeScreenActivity.this, MessageService.class);
        startService(in);
    }

    private void initClasses() {
        onChatInterfaceListener = this;
        dbh = new DatabaseHandler(HomeScreenActivity.this);
    }

    private void initViews() {
        lottieAnimationView = findViewById(R.id.lottie_view);
        llChatItem = findViewById(R.id.ll_chat_item);
        tvLastMessage = findViewById(R.id.tv_last_message);
        tvThink = findViewById(R.id.tv_think);
        tvTime = findViewById(R.id.tv_time);
    }

    private void initListeners() {
        llChatItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadChatFragment();
            }
        });
        lottieAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeScreenActivity.this, "Vibrate", Toast.LENGTH_SHORT).show();
            }
        });

        ArrayList<String> alMessageAndTime = new ArrayList<>(dbh.lastMessageAndTime());

        if (alMessageAndTime.size() == 2) {
            tvLastMessage.setText(alMessageAndTime.get(0));
            tvTime.setText(alMessageAndTime.get(1));
        }
        //if(dbh.lastMessage())
    }

    @Override
    public void onChat(String sCase, String sMessage, int nFlag, DataBaseHelper e) {
        switch (sCase){
            case "TYPING":
                if(nFlag==1){
                    tvThink.setTextColor(getResources().getColor(R.color.colorGreen));
                    tvThink.setText(getResources().getString(R.string.typing));
                } else {
                    tvThink.setTextColor(getResources().getColor(R.color.colorNextToWhite));
                    tvThink.setText(getResources().getString(R.string.is_thinking));
                }
                break;
        }
    }

    private void loadChatFragment() {
        newFragment = ChatFragment.newInstance("", "");
        sBackStackParent = newFragment.getClass().getName();
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.fl_container, newFragment, sBackStackParent);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        Toast.makeText(HomeScreenActivity.this, "Offline", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(HomeScreenActivity.this, "Online", Toast.LENGTH_SHORT).show();
    }
}
