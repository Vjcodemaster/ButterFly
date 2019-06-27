package app_utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vj.butterfly.ChatFragment;
import com.vj.butterfly.HomeScreenActivity;
import com.vj.butterfly.R;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static androidx.core.app.NotificationCompat.PRIORITY_MAX;
import static app_utility.StaticReferenceClass.OFFLINE_ONLY;
import static app_utility.StaticReferenceClass.OFFLINE_TYPING;
import static app_utility.StaticReferenceClass.ONLINE_ONLY;
import static app_utility.StaticReferenceClass.ONLINE_TYPING;

public class MessageService extends Service implements OnChatInterfaceListener {

    Alarm alarm = new Alarm();
    String channelId = "app_utility.MessageService";
    String channelName = "message_transfer";

    private static final int NOTIFICATION_ID = 101;

    public static MessageService refOfService;
    public static OnChatInterfaceListener onChatInterfaceListener;

    NotificationManager notifyMgr;

    public DatabaseReference dbReference;

    public DatabaseHandler dbh;

    private SharedPreferenceClass sharedPreferenceClass;

    NetworkState networkState;
    ConnectionStateMonitor connectionStateMonitor;
    boolean typingStatus;

    public static boolean IS_ONLINE = false;
    public static boolean IS_TYPING = false;
    public static String sLastSeen = "";


    public MessageService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground();
        }
        refOfService = this;
        onChatInterfaceListener = this;
        FirebaseApp.initializeApp(this);
        dbReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferenceClass = new SharedPreferenceClass(getApplicationContext());
        dbh = new DatabaseHandler(getApplicationContext());

        connectionStateMonitor = new ConnectionStateMonitor();
        connectionStateMonitor.enable(getApplicationContext());

        networkState = new NetworkState();
        //expectAMessage();
        eventListener();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForeground() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), createNotificationChannel());
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.createNotificationChannel(chan);
        return channelId;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        alarm.setAlarm(this);
        return START_NOT_STICKY;
    }

    @Override
    public void onChat(String sCase, String sMessage, int nFlag, DataBaseHelper e) {
        switch (sCase) {
            case "TOAST":
                checkPreviousMessageCondition(getReadyToSendPendingMessages());
                updateSent();
                //Toast.makeText(getApplicationContext(), "Network Available", Toast.LENGTH_SHORT).show();
                break;
            case "TYPING":
                if (nFlag == 1) {
                    notifyTyping(true);
                } else {
                    notifyTyping(false);
                }
                break;
            case "MESSAGE_SENT":
                //if (networkState.isOnline() && networkState.isNetworkAvailable(getApplicationContext())) {
                checkPreviousMessageCondition(sMessage);
                updateSent();
                //}
                break;
            case "STOP_SERVICE":
                refOfService.stopForeground(true);
                refOfService.stopSelf();
                break;
            case "ONLINE":
                if (nFlag == 1) {
                    dbReference.child("chats").child("9036640528").child("online").setValue(true);
                } else {
                    String sTime = TimestampUtil.getCurrentTimestampStringFormat().split(" ")[1];
                    dbReference.child("chats").child("9036640528").child("last_seen").setValue(sTime);
                    dbReference.child("chats").child("9036640528").child("online").setValue(false);
                    dbReference.child("chats").child("9036640528").child("typing").setValue(false);
                }
                break;
            case "CHECK_STATUS":
                checkChatStatus();
                break;
            case "READ":
                dbReference.child("chats").child("9036640528").child("read").setValue(true);
                break;
            case "THINK":
                dbReference.child("chats").child("9036640528").child("think").setValue(sMessage);
                break;
            case "NAME":
                dbReference.child("chats").child("9036640528").child("name").setValue(sMessage);
                break;
        }
    }

    private void checkChatStatus() {
        dbReference.child("chats").child("9036640528").child("online").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean onlineStatus = (boolean) dataSnapshot.getValue();

                dbReference.child("chats").child("9036640528").child("typing").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        typingStatus = (boolean) dataSnapshot.getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                int nFlag = 0;
                String sMessage = "";
                if (!onlineStatus && !typingStatus) {
                    nFlag = OFFLINE_ONLY;
                    sMessage = sharedPreferenceClass.getLastSeen();
                    //HomeScreenActivity.onChatInterfaceListener.onChat("STATUS_RETRIEVED", sharedPreferenceClass.getLastSeen(), OFFLINE_ONLY, null);
                } else if (onlineStatus && typingStatus) {
                    nFlag = ONLINE_TYPING;
                    sMessage = getResources().getString(R.string.typing);
                    //HomeScreenActivity.onChatInterfaceListener.onChat("STATUS_RETRIEVED", getResources().getString(R.string.typing), ONLINE_TYPING, null);
                }
                if (!onlineStatus && typingStatus) {
                    nFlag = OFFLINE_TYPING;
                    sMessage = sharedPreferenceClass.getLastSeen();
                    //HomeScreenActivity.onChatInterfaceListener.onChat("STATUS_RETRIEVED", sharedPreferenceClass.getLastSeen(), OFFLINE_TYPING, null);
                } else if (onlineStatus && !typingStatus) {
                    nFlag = ONLINE_ONLY;
                    sMessage = getResources().getString(R.string.online);
                    //HomeScreenActivity.onChatInterfaceListener.onChat("STATUS_RETRIEVED", "online", ONLINE_ONLY, null);
                }
                //if(!sMessage.equals("") && nFlag!=0)
                HomeScreenActivity.onChatInterfaceListener.onChat("STATUS_RETRIEVED", sMessage, nFlag, null);
                /*if (onlineStatus) {
                    if (HomeScreenActivity.onChatInterfaceListener != null) {
                        HomeScreenActivity.onChatInterfaceListener.onChat("STATUS_RETRIEVED", "Online", 1, null);
                    }
                } else {
                    if (HomeScreenActivity.onChatInterfaceListener != null) {
                        HomeScreenActivity.onChatInterfaceListener.onChat("STATUS_RETRIEVED", sharedPreferenceClass.getLastSeen(), 0, null);
                    }
                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void notifyTyping(boolean b) {
        dbReference.child("chats").child("9036640528").child("typing").setValue(b);
    }


   /* private void allFieldsListener(){
        dbReference.child("chats").child("9036640528").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                assert key != null;
                switch (key){
                    case "text":
                        break;
                    case "typing":
                        break;
                    case "online":
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    private void checkPreviousMessageCondition(final String sNewMessage) {
        dbReference.child("chats").child("9036640528").child("text").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.getValue().equals("")) {
                    dbReference.child("chats").child("9036640528").child("text").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String sPreviousMsg = dataSnapshot.getValue().toString();
                            String sFinalMessage = sPreviousMsg + "##" + sNewMessage;
                            updateMessageToFirebase(sFinalMessage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    //conditionSatisfied();
                } else {
                    updateMessageToFirebase(sNewMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateSent() {
        ArrayList<DataBaseHelper> alPendingList = new ArrayList<>(dbh.getMessageByStatusFilter(StaticReferenceClass.PENDING));
        for (int i = 0; i < alPendingList.size(); i++) {
            int id = alPendingList.get(i).get_id();
            DataBaseHelper e = new DataBaseHelper(StaticReferenceClass.SENT);
            dbh.updateStatusOfMessages(e, id);
            if (ChatFragment.mListener != null)
                ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.get(i).get_id(), e);
        }
        /*if (alPendingList.size() > 0 && ChatFragment.mListener != null)
            ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.size(), null);*/
        dbReference.child("chats").child("9036640528").child("delivered").setValue(false);
        dbReference.child("chats").child("9036640528").child("read").setValue(false);
    }

    private String getReadyToSendPendingMessages() {
        StringBuilder sAllMessages = new StringBuilder();
        ArrayList<DataBaseHelper> alPendingList = new ArrayList<>(dbh.getMessageByStatusFilter(StaticReferenceClass.PENDING));
        for (int i = 0; i < alPendingList.size(); i++) {
            sAllMessages.append("##").append(alPendingList.get(i).get_message());
        }
        return sAllMessages.toString();
    }
    /*private void checkPreviousMessageCondition(final String sNewMessage) {
        dbReference.child("chats").child("9036640528").child("delivered").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().equals(false)) {
                    dbReference.child("chats").child("9036640528").child("text").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String sPreviousMsg = dataSnapshot.getValue().toString();
                            String sFinalMessage = sPreviousMsg + "##" + sNewMessage;
                            updateMessageToFirebase(sFinalMessage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    //conditionSatisfied();
                } else {
                    updateMessageToFirebase(sNewMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    private void updateMessageToFirebase(String sMessage) {
        dbReference.child("chats").child("9036640528").child("text").setValue(sMessage);
    }

    /*private void expectAMessage() {
        dbReference.child("chats").child("9036640528").child("text").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String s = dataSnapshot.getValue().toString();
                String sTimeStamp = String.valueOf(TimestampUtil.getCurrentTimestamp());
                int type = StaticReferenceClass.INCOME;
                int status = -1;

                String sFinalTime = sTimeStamp.split("\\s")[1].substring(0, 5);

                DataBaseHelper e = new DataBaseHelper(type, s, sFinalTime, status);
                if (ChatFragment.mListener != null) {
                    ChatFragment.mListener.onChat("UPDATE_MSG", "", 0, e);
                }
                dbh.addDataToMessageTable(e);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    /*private void eventListener() {
        dbReference.child("chats").child("9036640528").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                switch (dataSnapshot.getKey()) {
                    case "online":
                        if (HomeScreenActivity.onChatInterfaceListener != null) {
                            if ((boolean) dataSnapshot.getValue()) {
                                HomeScreenActivity.onChatInterfaceListener.onChat("ONLINE", getResources().getString(R.string.online), 1, null);
                            } else {
                                dbReference.child("chats").child("9036640528").child("last_seen").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String sLastSeen = (String) dataSnapshot.getValue();
                                        HomeScreenActivity.onChatInterfaceListener.onChat("ONLINE", sLastSeen, 0, null);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                        break;
                    case "typing":
                        if (HomeScreenActivity.onChatInterfaceListener != null) {
                            if ((boolean) dataSnapshot.getValue()) {
                                HomeScreenActivity.onChatInterfaceListener.onChat("TYPING", "", 1, null);
                            } else {
                                HomeScreenActivity.onChatInterfaceListener.onChat("TYPING", "", 0, null);
                            }
                        }
                        *//*if(ChatFragment.mListener!=null){
                            if ((boolean) dataSnapshot.getValue()) {
                                ChatFragment.mListener.onChat("TYPING", "", 1, null);
                            } else {
                                ChatFragment.mListener.onChat("TYPING", "", 0, null);
                            }
                        }*//*
                        break;
                        *//*case "sent":
                            if ((boolean)dataSnapshot.getValue()){
                                ArrayList<DataBaseHelper> alPendingList = new ArrayList<>(dbh.getMessageByStatusFilter(StaticReferenceClass.PENDING));
                                for (int i=0; i<alPendingList.size();i++){
                                    int id = alPendingList.get(i).get_id();
                                    DataBaseHelper e = new DataBaseHelper(StaticReferenceClass.SENT);
                                    dbh.updateStatusOfMessages(e, id);
                                }
                                ArrayList<DataBaseHelper> al = new ArrayList<>(dbh.getMessages());
                                if(ChatFragment.mListener!=null)
                                    ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.size(), null);
                                dbReference.child("chats").child("9036640528").child("sent").setValue(false);
                            }
                            break;*//*
                    case "delivered":
                        if ((boolean) dataSnapshot.getValue()) {
                            ArrayList<DataBaseHelper> alPendingList = new ArrayList<>(dbh.getMessageByStatusFilter(StaticReferenceClass.SENT));
                            for (int i = 0; i < alPendingList.size(); i++) {
                                int id = alPendingList.get(i).get_id();
                                DataBaseHelper e = new DataBaseHelper(StaticReferenceClass.DELIVERED);
                                dbh.updateStatusOfMessages(e, id);
                                if (ChatFragment.mListener != null)
                                    ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.get(i).get_id(), e);
                            }
                            *//*if (ChatFragment.mListener != null)
                                ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.size(), null);*//*
                            dbReference.child("chats").child("9036640528").child("delivered").setValue(false);
                            dbReference.child("chats").child("9036640528").child("text").setValue("");
                        }
                        break;
                    case "read":
                        if ((boolean) dataSnapshot.getValue()) {
                            ArrayList<DataBaseHelper> alPendingList = new ArrayList<>(dbh.getMessageByStatusFilter(StaticReferenceClass.DELIVERED));
                            for (int i = 0; i < alPendingList.size(); i++) {
                                int id = alPendingList.get(i).get_id();
                                DataBaseHelper e = new DataBaseHelper(StaticReferenceClass.READ);
                                dbh.updateStatusOfMessages(e, id);
                                if (ChatFragment.mListener != null)
                                    ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.get(i).get_id(), e);
                            }
                            *//*if (ChatFragment.mListener != null)
                                ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.size(), null);*//*
                            dbReference.child("chats").child("9036640528").child("read").setValue(false);
                        }
                        break;

                    case "text":
                        String s1 = dataSnapshot.getValue().toString();
                        String[] saMessages;
                        if (!s1.equals("")) {
                            if (s1.contains("#")) {
                                saMessages = s1.split("##");
                                for (String saMessage : saMessages) {
                                    getTheMessages(saMessage);
                                }
                            } else {
                                getTheMessages(s1);
                                *//*String sTimeStamp = String.valueOf(TimestampUtil.getCurrentTimestamp());
                                int type = StaticReferenceClass.INCOME;
                                int status = -1;

                                String sFinalTime = sTimeStamp.split("\\s")[1].substring(0, 5);

                                DataBaseHelper e = new DataBaseHelper(type, s1, sFinalTime, status);
                                if (ChatFragment.mListener != null) {
                                    ChatFragment.mListener.onChat("UPDATE_MSG", "", 0, e);
                                }
                                dbh.addDataToMessageTable(e);
                                dbReference.child("chats").child("9036640528").child("text").setValue("");
                                dbReference.child("chats").child("9036640528").child("delivered").setValue(true);*//*
                            }
                        }
                        break;
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/


    private void eventListener() {
        dbReference.child("chats").child("9036640528").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                switch (dataSnapshot.getKey()) {
                    case "online":
                        //if (HomeScreenActivity.onChatInterfaceListener != null) {
                            if ((boolean) dataSnapshot.getValue()) {
                                IS_ONLINE = true;
                                if (HomeScreenActivity.onChatInterfaceListener != null)
                                HomeScreenActivity.onChatInterfaceListener.onChat("ONLINE", getResources().getString(R.string.online), 1, null);
                            } else {
                                IS_ONLINE = false;
                                dbReference.child("chats").child("9036640528").child("last_seen").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String sLastSeen = (String) dataSnapshot.getValue();
                                        if (HomeScreenActivity.onChatInterfaceListener != null)
                                            HomeScreenActivity.onChatInterfaceListener.onChat("ONLINE", sLastSeen, 0, null);
                                        sharedPreferenceClass.setLastSeen(sLastSeen);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        //}
                        break;
                    case "typing":
                        //if (HomeScreenActivity.onChatInterfaceListener != null) {
                            if ((boolean) dataSnapshot.getValue()) {
                                IS_TYPING = true;
                                if (HomeScreenActivity.onChatInterfaceListener != null)
                                HomeScreenActivity.onChatInterfaceListener.onChat("TYPING", "", 1, null);
                            } else {
                                IS_TYPING = false;
                                if (HomeScreenActivity.onChatInterfaceListener != null)
                                HomeScreenActivity.onChatInterfaceListener.onChat("TYPING", "", 0, null);
                            }
                        //}
                        break;
                    case "delivered":
                        if ((boolean) dataSnapshot.getValue()) {
                            ArrayList<DataBaseHelper> alPendingList = new ArrayList<>(dbh.getMessageByStatusFilter(StaticReferenceClass.SENT));
                            for (int i = 0; i < alPendingList.size(); i++) {
                                int id = alPendingList.get(i).get_id();
                                DataBaseHelper e = new DataBaseHelper(StaticReferenceClass.DELIVERED);
                                dbh.updateStatusOfMessages(e, id);
                                if (ChatFragment.mListener != null)
                                    ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.get(i).get_id(), e);
                            }
                            dbReference.child("chats").child("9036640528").child("delivered").setValue(false);
                            dbReference.child("chats").child("9036640528").child("text").setValue("");
                        }
                        break;
                    case "read":
                        if ((boolean) dataSnapshot.getValue()) {
                            ArrayList<DataBaseHelper> alPendingList = new ArrayList<>(dbh.getMessageByStatusFilter(StaticReferenceClass.DELIVERED));
                            for (int i = 0; i < alPendingList.size(); i++) {
                                int id = alPendingList.get(i).get_id();
                                DataBaseHelper e = new DataBaseHelper(StaticReferenceClass.READ);
                                dbh.updateStatusOfMessages(e, id);
                                if (ChatFragment.mListener != null)
                                    ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.get(i).get_id(), e);
                            }
                            dbReference.child("chats").child("9036640528").child("read").setValue(false);
                        }
                        break;

                    case "text":
                        String s1 = dataSnapshot.getValue().toString();
                        String[] saMessages;
                        if (!s1.equals("")) {
                            if (s1.contains("#")) {
                                saMessages = s1.split("##");
                                for (String saMessage : saMessages) {
                                    getTheMessages(saMessage);
                                }
                            } else {
                                getTheMessages(s1);
                            }
                        }
                        break;
                    case "think":
                        String sThink = dataSnapshot.getValue().toString();
                        sharedPreferenceClass.setButterflyThink(sThink);
                        break;
                    case "name":
                        String sNickName = dataSnapshot.getValue().toString();
                        sharedPreferenceClass.setButterflyNickName(sNickName);
                        break;
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getTheMessages(String s1) {
        String sTimeStamp = String.valueOf(TimestampUtil.getCurrentTimestampStringFormat());
        int type = StaticReferenceClass.INCOME;
        int status = -1;

        //String sFinalTime = sTimeStamp.split("\\s")[1].substring(0, 5);

        DataBaseHelper e = new DataBaseHelper(type, s1, sTimeStamp, status);
        if (ChatFragment.mListener != null) {
            ChatFragment.mListener.onChat("UPDATE_MSG", "", 0, e);
        }
        dbh.addDataToMessageTable(e);
        dbReference.child("chats").child("9036640528").child("text").setValue("");
        dbReference.child("chats").child("9036640528").child("delivered").setValue(true);
        if (HomeScreenActivity.onChatInterfaceListener != null)
            HomeScreenActivity.onChatInterfaceListener.onChat("NEW_MESSAGE_RECEIVED", s1, 0, null);
    }

}
