package app_utility;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceClass {
    private SharedPreferences sharedPreferences;
    private Context _context;
    SharedPreferences.Editor editor;

    private static final String APP_PREFERENCES = "BUTTERFLY_PREFERENCES";

    private static final int PRIVATE_MODE = 0;
    private static final String IS_LOGGED_IN = "IS_LOGGED_IN";
    private static final String USER_NAME = "USER_NAME";

    private static final String USER_PHONE = "USER_PHONE";

    private static final String BUTTERFLY_NAME = "BUTTERFLY_NAME";


    // Constructor
    public SharedPreferenceClass(Context context) {
        this._context = context;

        sharedPreferences = _context.getSharedPreferences(APP_PREFERENCES, PRIVATE_MODE);
        editor = sharedPreferences.edit();
        editor.apply();
    }


    public void setUserLogStatus(boolean bValue, String sUserName, String sButterFly, String sPhone){
        /*SharedPreferences sharedPreferences = _context.getSharedPreferences(APP_PREFERENCES, PRIVATE_MODE);
        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();*/
        editor.putBoolean(IS_LOGGED_IN, bValue);
        editor.putString(USER_NAME, sUserName);
        editor.putString(BUTTERFLY_NAME, sButterFly);
        editor.putString(USER_PHONE, sPhone);
        editor.apply();
    }

    public boolean getUserLogStatus(){
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    public String getUserName(){
        return sharedPreferences.getString(USER_NAME, "");
    }

    public String getButterflyName(){
        return sharedPreferences.getString(BUTTERFLY_NAME, "");
    }
}
