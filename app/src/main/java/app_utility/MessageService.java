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
import android.widget.Toast;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static android.content.ContentValues.TAG;
import static androidx.core.app.NotificationCompat.PRIORITY_MAX;

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
        }
    }

    private void notifyTyping(boolean b) {
        dbReference.child("chats").child("9036640528").child("typing").setValue(b);
    }

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
        }
        if (alPendingList.size() > 0 && ChatFragment.mListener != null)
            ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.size(), null);
        dbReference.child("chats").child("9036640528").child("delivered").setValue(false);
        dbReference.child("chats").child("9036640528").child("read").setValue(false);
    }

    private String getReadyToSendPendingMessages(){
        StringBuilder sAllMessages = new StringBuilder();
        ArrayList<DataBaseHelper> alPendingList = new ArrayList<>(dbh.getMessageByStatusFilter(StaticReferenceClass.PENDING));
        for (int i=0; i<alPendingList.size();i++){
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

    private void eventListener() {
        dbReference.child("chats").child("9036640528").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                switch (dataSnapshot.getKey()) {
                        /*case "sent":
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
                            break;*/
                    case "delivered":
                        if ((boolean) dataSnapshot.getValue()) {
                            ArrayList<DataBaseHelper> alPendingList = new ArrayList<>(dbh.getMessageByStatusFilter(StaticReferenceClass.SENT));
                            for (int i = 0; i < alPendingList.size(); i++) {
                                int id = alPendingList.get(i).get_id();
                                DataBaseHelper e = new DataBaseHelper(StaticReferenceClass.DELIVERED);
                                dbh.updateStatusOfMessages(e, id);
                            }
                            if (ChatFragment.mListener != null)
                                ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.size(), null);
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
                            }
                            if (ChatFragment.mListener != null)
                                ChatFragment.mListener.onChat("UPDATE_STATUS", "", alPendingList.size(), null);
                            dbReference.child("chats").child("9036640528").child("read").setValue(false);
                        }
                        break;
                    case "typing":
                        if ((boolean) dataSnapshot.getValue()) {

                        }
                        break;
                    case "text":
                        String s1 = dataSnapshot.getValue().toString();
                        if(!s1.equals("")) {
                            String sTimeStamp = String.valueOf(TimestampUtil.getCurrentTimestamp());
                            int type = StaticReferenceClass.INCOME;
                            int status = -1;

                            String sFinalTime = sTimeStamp.split("\\s")[1].substring(0, 5);

                            DataBaseHelper e = new DataBaseHelper(type, s1, sFinalTime, status);
                            if (ChatFragment.mListener != null) {
                                ChatFragment.mListener.onChat("UPDATE_MSG", "", 0, e);
                            }
                            dbh.addDataToMessageTable(e);
                            dbReference.child("chats").child("9036640528").child("text").setValue("");
                            dbReference.child("chats").child("9036640528").child("delivered").setValue(true);
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
    }
}
