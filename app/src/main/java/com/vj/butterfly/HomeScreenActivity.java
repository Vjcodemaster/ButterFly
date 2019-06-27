package com.vj.butterfly;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import app_utility.DataBaseHelper;
import app_utility.DatabaseHandler;
import app_utility.MessageService;
import app_utility.OnChatInterfaceListener;
import app_utility.PermissionHandler;
import app_utility.SharedPreferenceClass;
import app_utility.StaticReferenceClass;
import library.CircleImageView;

import static android.view.View.GONE;
import static app_utility.PermissionHandler.APP_PERMISSION;
import static app_utility.StaticReferenceClass.ACCENT;
import static app_utility.StaticReferenceClass.GREEN;
import static app_utility.StaticReferenceClass.OFFLINE_ONLY;
import static app_utility.StaticReferenceClass.OFFLINE_TYPING;
import static app_utility.StaticReferenceClass.ONLINE;
import static app_utility.StaticReferenceClass.ONLINE_ONLY;
import static app_utility.StaticReferenceClass.ONLINE_TYPING;
import static app_utility.StaticReferenceClass.PICTURE_REQUEST_CODE;
import static app_utility.StaticReferenceClass.TYPING;
import static app_utility.StaticReferenceClass.WHITE;

public class HomeScreenActivity extends AppCompatActivity implements OnChatInterfaceListener {
    LottieAnimationView lottieAnimationView;

    Fragment newFragment;
    FragmentTransaction transaction;
    String sBackStackParent;
    LinearLayout llChatItem;

    TextView tvLastMessage, tvName, tvThink, tvTime;

    TextView tvTitle, tvSubtitle;
    CircleImageView civDPTB, civDP;
    Toolbar toolbar;
    int STATUS_FLAG;

    ImageButton ibEdit;

    private DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle mDrawerToggle;

    DatabaseHandler dbh;
    public static OnChatInterfaceListener onChatInterfaceListener;
    public static boolean isUserSelectingImage = false;
    SharedPreferenceClass sharedPreferenceClass;
    boolean isVisibleToUser;
    private int nPermissionFlag = 0;

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
        tvName = findViewById(R.id.tv_name);
        tvThink = findViewById(R.id.tv_think);
        tvTime = findViewById(R.id.tv_time);

        civDP = findViewById(R.id.civ_dp);

        toolbar = findViewById(R.id.toolbar);
        civDPTB = toolbar.findViewById(R.id.civ_dp_tb);
        tvTitle = toolbar.findViewById(R.id.tv_title);
        tvSubtitle = toolbar.findViewById(R.id.tv_sub_title);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.app_name, R.string.app_name
        );

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        //mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorNextToWhite));

        mDrawerToggle.syncState();

        navigationView = findViewById(R.id.nav_view);

        if (navigationView != null) {
            setupDrawerContent(navigationView);

            TextView tvName = navigationView.getHeaderView(0).findViewById(R.id.tv_nav_name);
            tvName.setText(sharedPreferenceClass.getUserName());

            TextView tvThink = navigationView.getHeaderView(0).findViewById(R.id.tv_nav_think);
            tvThink.setText(sharedPreferenceClass.getThinking());
        }


        //ibEdit = navigationView.getHeaderView(0).findViewById(R.id.ib_nav_edit);

        /*ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSettingsFragment();
            }
        });*/
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_nav_settings:
                        loadSettingsFragment();
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

        civDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(HomeScreenActivity.this, ButterflyActivity.class);
                in.putExtra("name", tvName.getText().toString());
                in.putExtra("think", tvThink.getText().toString());
                in.putExtra("phone", sharedPreferenceClass.getUserPhone());
                Pair<View, String> pCIVDP = Pair.create((View) civDP, getResources().getString(R.string.image_transition));
                Pair<View, String> pNickName = Pair.create((View) tvName, getResources().getString(R.string.name_transition));
                Pair<View, String> pThink = Pair.create((View) tvThink, getResources().getString(R.string.thinking_transition));

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeScreenActivity.this, pCIVDP,
                        pNickName, pThink);
                startActivityForResult(in, 0, options.toBundle());
            }
        });

        civDPTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(HomeScreenActivity.this, ButterflyActivity.class);
                in.putExtra("name", tvName.getText().toString());
                in.putExtra("think", tvThink.getText().toString());
                in.putExtra("phone", sharedPreferenceClass.getUserPhone());
                Pair<View, String> pCIVDP = Pair.create((View) civDPTB, getResources().getString(R.string.image_transition));
                Pair<View, String> pNickName = Pair.create((View) tvTitle, getResources().getString(R.string.name_transition));
                //Pair<View, String> pThink = Pair.create((View) tvSubtitle, getResources().getString(R.string.thinking_transition));

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeScreenActivity.this, pCIVDP,
                        pNickName);
                startActivityForResult(in, 0, options.toBundle());
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
        sBackStackParent = "com.vj.butterfly.ChatFragment";
        Fragment test = getSupportFragmentManager().findFragmentByTag(sBackStackParent);
        if (test != null)
            isVisibleToUser = test.isAdded() && test.isVisible() && test.getUserVisibleHint();
        else
            isVisibleToUser = false;
        switch (sCase) {
            case "TYPING":
                /*if (isVisibleToUser) {

                    if (nFlag == 1) {
                        changeTextColor(tvSubtitle, GREEN);
                        //tvSubtitle.setTextColor(getResources().getColor(R.color.colorGreen));
                        tvSubtitle.setText(getResources().getString(R.string.typing));
                    } else {
                        //if()
                        if(STATUS_FLAG==ONLINE_TYPING){
                            changeTextColor(tvSubtitle, GREEN);
                            tvSubtitle.setText(getResources().getString(R.string.typing));
                        }
                        else if(STATUS_FLAG==ONLINE_ONLY) {
                            changeTextColor(tvSubtitle, WHITE);
                            tvSubtitle.setText(getResources().getString(R.string.online));
                        } else {
                            changeTextColor(tvSubtitle, ACCENT);
                            tvSubtitle.setText(sharedPreferenceClass.getLastSeen());
                        }
                        //tvThink.setText(getResources().getString(R.string.is_thinking));
                    }
                    tvSubtitle.setVisibility(View.VISIBLE);
                } else {
                    tvSubtitle.setVisibility(GONE);
                    if (nFlag == 1) {
                        changeTextColor(tvThink, GREEN);
                        //tvThink.setTextColor(getResources().getColor(R.color.colorGreen));
                        tvThink.setText(getResources().getString(R.string.typing));
                    } else {
                        changeTextColor(tvThink, WHITE);
                        //tvThink.setTextColor(getResources().getColor(R.color.colorNextToWhite));
                        tvThink.setText(getResources().getString(R.string.is_thinking));
                    }
                }*/
                int size = getSupportFragmentManager().getBackStackEntryCount();
                /*if (size == 0) {
                    if (MessageService.IS_TYPING) {
                        changeTextColor(tvThink, GREEN);
                        tvThink.setText(getResources().getString(R.string.typing));
                    } else {
                        changeTextColor(tvThink, WHITE);
                        tvThink.setText(getResources().getString(R.string.is_thinking));
                    }
                }*/
                boolean isFromChat = false;

                if (size >= 1) {
                    String fragmentTag = getSupportFragmentManager().findFragmentById(R.id.fl_container).getClass().getName();
                    if (fragmentTag.equals("com.vj.butterfly.ChatFragment")) {
                        isFromChat = true;
                    }
                }
                setStatusOfUser(TYPING, isFromChat);
                break;
            case "ONLINE":
                int size1 = getSupportFragmentManager().getBackStackEntryCount();
                /*if (size == 0) {
                    if (MessageService.IS_TYPING) {
                        changeTextColor(tvThink, GREEN);
                        tvThink.setText(getResources().getString(R.string.typing));
                    } else {
                        changeTextColor(tvThink, WHITE);
                        tvThink.setText(getResources().getString(R.string.is_thinking));
                    }
                }*/
                boolean isFromChat1 = false;

                if (size1 >= 1) {
                    String fragmentTag = getSupportFragmentManager().findFragmentById(R.id.fl_container).getClass().getName();
                    if (fragmentTag.equals("com.vj.butterfly.ChatFragment")) {
                        isFromChat1 = true;
                    }
                }
                setStatusOfUser(ONLINE, isFromChat1);
                /*if (isVisibleToUser) {
                    //changeTextColor(tvSubtitle, WHITE);
                    //tvSubtitle.setTextColor(getResources().getColor(R.color.colorNextToWhite));
                    tvSubtitle.setVisibility(View.VISIBLE);
                } else {
                    tvSubtitle.setVisibility(GONE);
                }

                tvSubtitle.setText(sMessage);
                if (nFlag == 0) {
                    changeTextColor(tvSubtitle, ACCENT);
                    //sharedPreferenceClass.setLastSeen(sMessage);
                } else {
                    changeTextColor(tvSubtitle, WHITE);
                }*/
                break;
            case "STATUS_RETRIEVED":
                tvSubtitle.setText(sMessage);
                if (isVisibleToUser) {
                    switch (nFlag) {
                        case ONLINE_TYPING:
                            STATUS_FLAG = ONLINE_TYPING;
                            changeTextColor(tvSubtitle, GREEN);
                            break;
                        case OFFLINE_TYPING:
                            STATUS_FLAG = OFFLINE_TYPING;
                            changeTextColor(tvSubtitle, ACCENT);
                            break;
                        case OFFLINE_ONLY:
                            STATUS_FLAG = OFFLINE_ONLY;
                            changeTextColor(tvSubtitle, ACCENT);
                            break;
                        case ONLINE_ONLY:
                            STATUS_FLAG = ONLINE_ONLY;
                            changeTextColor(tvSubtitle, WHITE);
                            break;
                    }
                    tvSubtitle.setVisibility(View.VISIBLE);
                }
                break;
            case "NEW_MESSAGE_RECEIVED":
                if (!isVisibleToUser) {
                    changeTextColor(tvLastMessage, GREEN);
                    tvLastMessage.setText(sMessage);
                } else {
                    MessageService.onChatInterfaceListener.onChat("READ", "", 1, null);
                }
                break;
        }
    }

    private void loadSettingsFragment() {
        newFragment = SettingsFragment.newInstance("", "");
        sBackStackParent = newFragment.getClass().getName();
        Fragment test = getSupportFragmentManager().findFragmentByTag(sBackStackParent);

        if (test == null) {
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
            transaction.replace(R.id.fl_container, newFragment, sBackStackParent);
            transaction.addToBackStack(null);
            transaction.commit();
            mDrawerLayout.closeDrawer(GravityCompat.START);
            tvSubtitle.setText(getResources().getString(R.string.settings));
            tvSubtitle.setVisibility(View.VISIBLE);
            changeTextColor(tvSubtitle, WHITE);
        } else {
            mDrawerLayout.closeDrawer(GravityCompat.START);
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
        civDPTB.setVisibility(View.VISIBLE);
        civDPTB.setImageResource(R.drawable.vj);
        /*if (MessageService.onChatInterfaceListener != null) {
            MessageService.onChatInterfaceListener.onChat("CHECK_STATUS", "", 0, null);
        }*/
        changeTextColor(tvLastMessage, WHITE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //int size = getSupportFragmentManager().getBackStackEntryCount();
                //if (size >= 1) {
                //setStatusOfUser(ONLINE);
                setStatusOfUser(TYPING, true);
                tvSubtitle.setVisibility(View.VISIBLE);
                //}
                ArrayList<DataBaseHelper> alPendingList = new ArrayList<>(dbh.getMessageByStatusFilter(StaticReferenceClass.DELIVERED));
                if (alPendingList.size() >= 1)
                    MessageService.onChatInterfaceListener.onChat("READ", "", 1, null);
            }
        }, 1500);

        //tvLastMessage.setTextColor(getResources().getColor(R.color.colorNextToWhite));
    }

    private void changeTextColor(TextView tv, int flag) {
        switch (flag) {
            case WHITE:
                tv.setTextColor(getResources().getColor(R.color.colorNextToWhite));
                break;
            case GREEN:
                tv.setTextColor(getResources().getColor(R.color.colorGreen));
                break;
            /*case BLUE:
                tv.setTextColor(getResources().getColor(R.color.colorBlue));
                break;*/
            case ACCENT:
                tv.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(HomeScreenActivity.this, "Offline", Toast.LENGTH_SHORT).show();
//        if (MessageService.onChatInterfaceListener != null && !isUserSelectingImage) {
//            MessageService.onChatInterfaceListener.onChat("ONLINE", "", 0, null);
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //boolean isVisibleToUser;
        //Toast.makeText(HomeScreenActivity.this, "Online", Toast.LENGTH_SHORT).show();
        /*if (!isUserSelectingImage) {
            if (MessageService.onChatInterfaceListener != null) {
                MessageService.onChatInterfaceListener.onChat("ONLINE", "", 1, null);
            }
            sBackStackParent = "com.vj.butterfly.SettingsFragment";
            int count = getSupportFragmentManager().getBackStackEntryCount();
            Fragment test = getSupportFragmentManager().findFragmentByTag(sBackStackParent);

            if (test != null)
                isVisibleToUser = test.isAdded() && test.isVisible() && test.getUserVisibleHint();
            else
                isVisibleToUser = false;


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isVisibleToUser)
                        setStatusOfUser();
                }
            }, 1500);
        }*/
        /*if (isVisibleToUser && MessageService.onChatInterfaceListener != null) {
            //MessageService.onChatInterfaceListener.onChat("CHECK_STATUS", "", 0, null);
            if(MessageService.IS_TYPING){
                changeTextColor(tvSubtitle, GREEN);
                tvSubtitle.setText(getResources().getString(R.string.typing));
            } else {
                if(MessageService.IS_ONLINE){
                    changeTextColor(tvSubtitle, WHITE);
                    tvSubtitle.setText(getResources().getString(R.string.online));
                } else {
                    changeTextColor(tvSubtitle, ACCENT);
                    tvSubtitle.setText(sharedPreferenceClass.getLastSeen());
                }
            }

        }*/
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!PermissionHandler.hasPermissions(HomeScreenActivity.this, APP_PERMISSION)) {
            ActivityCompat.requestPermissions(HomeScreenActivity.this, APP_PERMISSION, 1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int PERMISSION_ALL, String permissions[], int[] grantResults) {
        StringBuilder sMSG = new StringBuilder();
        if (PERMISSION_ALL == 1) {
            for (String sPermission : permissions) {
                switch (sPermission) {
                    case Manifest.permission.READ_CONTACTS:
                        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeScreenActivity.this, Manifest.permission.READ_CONTACTS)) {
                                sMSG.append("Contacts, ");
                                nPermissionFlag = 0;
                            } else {
                                //Never ask again selected, or device policy prohibits the app from having that permission.
                                //So, disable that feature, or fall back to another situation...
                                sMSG.append("Contacts, ");
                                nPermissionFlag = 0;
                            }
                        }
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeScreenActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                sMSG.append("Storage, ");
                                nPermissionFlag = 0;
                            } else {
                                //Never ask again selected, or device policy prohibits the app from having that permission.
                                //So, disable that feature, or fall back to another situation...
                                sMSG.append("Storage, ");
                                nPermissionFlag = 0;
                            }
                        }
                        break;
                }
            }
            if (!sMSG.toString().equals("") && !sMSG.toString().equals(" ")) {
                PermissionHandler permissionHandler = new PermissionHandler(HomeScreenActivity.this, 0, sMSG.toString(), nPermissionFlag);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICTURE_REQUEST_CODE) {
                //this.data = data;
                //new attractionNameAsyncTask().execute();
                isUserSelectingImage = false;
            }
        } else {
            //stopProgressBar();
        }
    }

    /*
    this is implemented to make sure even when notification panel is pulled down user goes offline
    this is made just in case user goes and turns off the data or wifi from notification while using application
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // handle when the user pull down the notification bar where
        // (hasFocus will ='false') & if the user pushed the
        // notification bar back to the top, then (hasFocus will ='true')
        if (!isUserSelectingImage)
            if (!hasFocus) {
                if (MessageService.onChatInterfaceListener != null) {
                    MessageService.onChatInterfaceListener.onChat("ONLINE", "", 0, null);
                }
                //Log.i("Tag", "Notification bar is pulled down");
            } else {
                //Log.i("Tag", "Notification bar is pushed up");
                if (MessageService.onChatInterfaceListener != null) {
                    MessageService.onChatInterfaceListener.onChat("ONLINE", "", 1, null);
                }
            }
        /*sBackStackParent = "com.vj.butterfly.SettingsFragment";
        //int count = getSupportFragmentManager().getBackStackEntryCount();
        Fragment test = getSupportFragmentManager().findFragmentByTag(sBackStackParent);

        if (test != null)
            isVisibleToUser = test.isAdded() && test.isVisible() && test.getUserVisibleHint();
        else
            isVisibleToUser = false;*/


        /*final Handler handler = new Handler();
        if (!isUserSelectingImage)
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //if (!isVisibleToUser)
                    //setStatusOfUser();
                }
            }, 1500);
        super.onWindowFocusChanged(hasFocus);*/
    }

    @Override
    public void onBackPressed() {
        int size = getSupportFragmentManager().getBackStackEntryCount();
        if (size == 1) {
            tvTitle.setText(getResources().getString(R.string.app_name));
            tvSubtitle.setVisibility(GONE);
            civDPTB.setVisibility(GONE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //mDrawerToggle.setDrawerIndicatorEnabled(true);
        }
        if (size == 0) {
            MessageService.onChatInterfaceListener.onChat("ONLINE", "", 0, null);
        }
        super.onBackPressed();
    }

    private void setStatusOfUser(int CASE, boolean isFromChat) {
        //int size = getSupportFragmentManager().getBackStackEntryCount();
        //String fragmentTag = null;
        switch (CASE) {
            case ONLINE:
                if (isFromChat) {
                    if (MessageService.IS_ONLINE) {
                        changeTextColor(tvSubtitle, WHITE);
                        tvSubtitle.setText(getResources().getString(R.string.online));
                    } else {
                        changeTextColor(tvSubtitle, ACCENT);
                        tvSubtitle.setText(sharedPreferenceClass.getLastSeen());
                    }
                }
                break;
            case TYPING:
                if (!isFromChat) {
                    if (MessageService.IS_TYPING) {
                        changeTextColor(tvThink, GREEN);
                        tvThink.setText(getResources().getString(R.string.typing));
                    } else {
                        changeTextColor(tvThink, WHITE);
                        tvThink.setText(getResources().getString(R.string.is_thinking));
                    }
                } else {
                    if (MessageService.IS_TYPING) {
                        changeTextColor(tvSubtitle, GREEN);
                        tvSubtitle.setText(getResources().getString(R.string.typing));
                        changeTextColor(tvThink, GREEN);
                        tvThink.setText(getResources().getString(R.string.typing));
                    } else if (MessageService.IS_ONLINE) {
                        changeTextColor(tvSubtitle, WHITE);
                        tvSubtitle.setText(getResources().getString(R.string.online));
                    } else {
                        changeTextColor(tvSubtitle, ACCENT);
                        tvSubtitle.setText(sharedPreferenceClass.getLastSeen());
                    }
                    if (!MessageService.IS_TYPING) {
                        changeTextColor(tvThink, WHITE);
                        tvThink.setText(getResources().getString(R.string.is_thinking));
                    }
                }
                break;
        }
    }


    /*private void setStatusOfUser() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        String fragmentTag = null;
        if (count >= 1) {
            fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();

            if (fragmentTag != null) {
                switch (fragmentTag) {
                    case "com.vj.butterfly.ChatFragment":
                        sBackStackParent = "com.vj.butterfly.ChatFragment";
                        break;
                    case "com.vj.butterfly.SettingsFragment":
                        sBackStackParent = "com.vj.butterfly.SettingsFragment";
                        break;
                }
            }
            Fragment test = getSupportFragmentManager().findFragmentByTag(sBackStackParent);
            //boolean isVisibleToUser;
            if (test != null)
                isVisibleToUser = test.isAdded() && test.isVisible() && test.getUserVisibleHint();
            else
                isVisibleToUser = false;
        }

        if (isVisibleToUser && MessageService.onChatInterfaceListener != null) {
            //MessageService.onChatInterfaceListener.onChat("CHECK_STATUS", "", 0, null);
            if (MessageService.IS_TYPING) {
                changeTextColor(tvSubtitle, GREEN);
                tvSubtitle.setText(getResources().getString(R.string.typing));
            } else {
                if (MessageService.IS_ONLINE) {
                    changeTextColor(tvSubtitle, WHITE);
                    if (fragmentTag != null) {
                        tvSubtitle.setText(getResources().getString(R.string.settings));
                    } else
                        tvSubtitle.setText(getResources().getString(R.string.online));
                } else {
                    changeTextColor(tvSubtitle, ACCENT);
                    tvSubtitle.setText(sharedPreferenceClass.getLastSeen());
                }
            }
            tvSubtitle.setVisibility(View.VISIBLE);
        } else if (!isVisibleToUser) {
            tvSubtitle.setVisibility(GONE);
            if (MessageService.IS_TYPING) {
                changeTextColor(tvThink, GREEN);
                tvThink.setText(getResources().getString(R.string.typing));
            } else {
                changeTextColor(tvThink, WHITE);
                tvThink.setText(getResources().getString(R.string.is_thinking));
            }
        }
    }*/
    /*private void changeVisibility(TextView tv, final int VISIBILITY){
        if(VISIBILITY == VISIBLE){
            tv.setVisibility(View.VISIBLE);
        } else{
            tv.setVisibility(GONE);
        }
    }*/
}
