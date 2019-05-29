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
import app_utility.SharedPreferenceClass;
import library.CircleImageView;

import static android.view.View.GONE;

public class HomeScreenActivity extends AppCompatActivity implements OnChatInterfaceListener {
    LottieAnimationView lottieAnimationView;

    Fragment newFragment;
    FragmentTransaction transaction;
    String sBackStackParent;
    LinearLayout llChatItem;

    TextView tvLastMessage, tvThink, tvTime;

    TextView tvTitle, tvSubtitle;
    CircleImageView civDP;
    Toolbar toolbar;

    private DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle mDrawerToggle;

    DatabaseHandler dbh;
    public static OnChatInterfaceListener onChatInterfaceListener;
    SharedPreferenceClass sharedPreferenceClass;

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
        sharedPreferenceClass = new SharedPreferenceClass(HomeScreenActivity.this);
    }

    private void initViews() {
        lottieAnimationView = findViewById(R.id.lottie_view);
        llChatItem = findViewById(R.id.ll_chat_item);
        tvLastMessage = findViewById(R.id.tv_last_message);
        tvThink = findViewById(R.id.tv_think);
        tvTime = findViewById(R.id.tv_time);

        toolbar = findViewById(R.id.toolbar);
        civDP = toolbar.findViewById(R.id.civ_dp);
        tvTitle = toolbar.findViewById(R.id.tv_title);
        tvSubtitle = toolbar.findViewById(R.id.tv_sub_title);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.app_name, R.string.app_name
        );

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorNextToWhite));

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
        //MyFragmentClass test = (MyFragmentClass) getSupportFragmentManager().findFragmentByTag("testID");
        //String sBackStackParent = getSupportFragmentManager().getBackStackEntryAt(0);
        //getSupportFragmentManager().findFragmentByTag("com")
        sBackStackParent = "com.vj.butterfly.ChatFragment";
        Fragment test = getSupportFragmentManager().findFragmentByTag(sBackStackParent);
        boolean isVisibleToUser;
        if (test != null)
            isVisibleToUser = test.isAdded() && test.isVisible() && test.getUserVisibleHint();
        else
            isVisibleToUser = false;
        switch (sCase) {
            case "TYPING":
                if (isVisibleToUser) {
                    tvSubtitle.setVisibility(View.VISIBLE);
                    if (nFlag == 1) {
                        tvSubtitle.setTextColor(getResources().getColor(R.color.colorGreen));
                        tvSubtitle.setText(getResources().getString(R.string.typing));
                    } else {
                        tvSubtitle.setTextColor(getResources().getColor(R.color.colorNextToWhite));
                        tvSubtitle.setText(sharedPreferenceClass.getLastSeen());
                        //tvThink.setText(getResources().getString(R.string.is_thinking));
                    }
                } else {
                    tvSubtitle.setVisibility(GONE);
                    if (nFlag == 1) {
                        tvThink.setTextColor(getResources().getColor(R.color.colorGreen));
                        tvThink.setText(getResources().getString(R.string.typing));
                    } else {
                        tvThink.setTextColor(getResources().getColor(R.color.colorNextToWhite));
                        tvThink.setText(getResources().getString(R.string.is_thinking));
                    }
                }
                /*if (nFlag == 1) {
                    tvThink.setTextColor(getResources().getColor(R.color.colorGreen));
                    tvThink.setText(getResources().getString(R.string.typing));
                } else {
                    tvThink.setTextColor(getResources().getColor(R.color.colorNextToWhite));
                    tvThink.setText(getResources().getString(R.string.is_thinking));
                }*/
                break;
            case "ONLINE":
                if (isVisibleToUser) {
                    tvSubtitle.setTextColor(getResources().getColor(R.color.colorNextToWhite));
                    tvSubtitle.setVisibility(View.VISIBLE);
                } else {
                    tvSubtitle.setVisibility(GONE);
                }
                tvSubtitle.setText(sMessage);
                sharedPreferenceClass.setLastSeen(sMessage);
                break;
            case "STATUS_RETRIEVED":
                tvSubtitle.setText(sMessage);
                tvSubtitle.setVisibility(View.VISIBLE);
                tvSubtitle.setTextColor(getResources().getColor(R.color.colorNextToWhite));
                /*if (nFlag == 1) {
                    tvSubtitle.setTextColor(getResources().getColor(R.color.colorGreen));
                } else {
                    tvSubtitle.setTextColor(getResources().getColor(R.color.colorNextToWhite));
                }*/
                break;
        }
    }

    private void loadChatFragment() {
        //tvTitle.setText(sharedPreferenceClass.getButterflyName());
        //mDrawerToggle.setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        newFragment = ChatFragment.newInstance("", "");
        sBackStackParent = newFragment.getClass().getName();
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.fl_container, newFragment, sBackStackParent);
        transaction.addToBackStack(null);
        transaction.commit();
        tvTitle.setText("Vijay");
        civDP.setVisibility(View.VISIBLE);
        civDP.setImageResource(R.drawable.vj);
        if (MessageService.onChatInterfaceListener != null) {
            MessageService.onChatInterfaceListener.onChat("CHECK_ONLINE_STATUS", "", 0, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(HomeScreenActivity.this, "Offline", Toast.LENGTH_SHORT).show();
        if (MessageService.onChatInterfaceListener != null) {
            MessageService.onChatInterfaceListener.onChat("ONLINE", "", 0, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(HomeScreenActivity.this, "Online", Toast.LENGTH_SHORT).show();
        if (MessageService.onChatInterfaceListener != null) {
            MessageService.onChatInterfaceListener.onChat("ONLINE", "", 1, null);
        }
    }

    @Override
    public void onBackPressed() {
        int size = getSupportFragmentManager().getBackStackEntryCount();
        if (size == 1) {
            tvTitle.setText(getResources().getString(R.string.app_name));
            tvSubtitle.setVisibility(GONE);
            civDP.setVisibility(GONE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //mDrawerToggle.setDrawerIndicatorEnabled(true);
        }
        super.onBackPressed();
    }
}
