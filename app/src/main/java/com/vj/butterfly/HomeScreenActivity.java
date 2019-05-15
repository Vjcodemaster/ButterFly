package com.vj.butterfly;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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

    TextView tvTitle, tvSubtitle;
    Toolbar toolbar;

    private DrawerLayout mDrawerLayout;
    NavigationView navigationView;

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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.app_name, R.string.app_name
        );

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        navigationView = findViewById(R.id.nav_view);

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_nav_settings:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //handles open and close of home button of actionbar/toolbar
            case android.R.id.home:
                if (!mDrawerLayout.isDrawerOpen(GravityCompat.START))
                    mDrawerLayout.openDrawer(GravityCompat.START);
                else
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        switch (sCase) {
            case "TYPING":
                if (nFlag == 1) {
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
