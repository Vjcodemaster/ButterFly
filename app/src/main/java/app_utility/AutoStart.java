package app_utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStart extends BroadcastReceiver
{
    Alarm alarm = new Alarm();
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            alarm.setAlarm(context);
        }
        if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
            Log.e("asdasd", "ACTION_DATE_CHANGED received");
            Log.e("asdasds", "ACTION_DATE_CHANGED received");
            Log.e("asdasd", "ACTION_DATE_CHANGED received");
            Log.e("asdasd", "ACTION_DATE_CHANGED received");
            Log.e("asdasd", "ACTION_DATE_CHANGED received");
            Log.e("asdasd", "ACTION_DATE_CHANGED received");
        }
    }
}
