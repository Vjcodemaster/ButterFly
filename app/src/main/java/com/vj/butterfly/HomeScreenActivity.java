package com.vj.butterfly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import app_utility.ConnectionStateMonitor;
import app_utility.DataBaseHelper;
import app_utility.MessageService;
import app_utility.OnChatInterfaceListener;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

public class HomeScreenActivity extends AppCompatActivity implements OnChatInterfaceListener {
    LottieAnimationView lottieAnimationView;

    Fragment newFragment;
    FragmentTransaction transaction;
    String sBackStackParent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        initViews();
        loadChatFragment();
        Intent in = new Intent(HomeScreenActivity.this, MessageService.class);
        startService(in);
    }

    private void initViews(){
        lottieAnimationView = findViewById(R.id.lottie_view);

        lottieAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeScreenActivity.this, "Vibrate", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChat(String sCase, String sMessage, int nFlag, DataBaseHelper e) {

    }

    private void loadChatFragment(){
        newFragment = ChatFragment.newInstance("","");
        sBackStackParent = newFragment.getClass().getName();
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, newFragment, sBackStackParent);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
