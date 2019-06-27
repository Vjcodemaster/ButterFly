package app_utility;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceClass {
    private SharedPreferences sharedPreferences;
    private Context _context;
    private SharedPreferences.Editor editor;

    private static final String APP_PREFERENCES = "BUTTERFLY_PREFERENCES";

    private static final int PRIVATE_MODE = 0;
    private static final String IS_LOGGED_IN = "IS_LOGGED_IN";
    private static final String USER_NAME = "USER_NAME";

    private static final String USER_PHONE = "USER_PHONE";

    private static final String BUTTERFLY_NAME = "BUTTERFLY_NAME";

    private static final String LAST_SEEN = "LAST_SEEN";
    private static final String THINKING = "THINKING";

    private static final String BUTTERFLY_NICK_NAME = "BUTTERFLY_NICK_NAME";
    private static final String BUTTERFLY_THINK = "BUTTERFLY_THINK";
    private static final String LAST_TYPE_MSG = "LAST_TYPE_MSG";

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

    public void setLastSeen(String sLastSeen){
        editor.putString(LAST_SEEN, sLastSeen);
        editor.apply();
    }
    public void setThinking(String sThinking){
        editor.putString(THINKING, sThinking);
        editor.apply();
    }

    public void setUserName(String sUserName){
        editor.putString(USER_NAME, sUserName);
        editor.apply();
    }

    public void setButterflyNickName(String sNickName){
        editor.putString(BUTTERFLY_NICK_NAME, sNickName);
        editor.apply();
    }

    public void setButterflyThink(String sThink){
        editor.putString(BUTTERFLY_THINK, sThink);
        editor.apply();
    }

    public void setLastTypingMessage(String sTypingMessage){
        editor.putString(LAST_TYPE_MSG, sTypingMessage);
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

    public String getLastSeen(){
        return sharedPreferences.getString(LAST_SEEN, "");
    }

    public String getThinking(){
        return sharedPreferences.getString(THINKING, "");
    }
    public String getUserPhone(){
        return sharedPreferences.getString(USER_PHONE, "");
    }

    public String getButterflyThink(){
        return sharedPreferences.getString(BUTTERFLY_THINK, "");
    }

    public String getButterflyNickName(){
        return sharedPreferences.getString(BUTTERFLY_NICK_NAME, "");
    }

    public String getLastTypingMessage(){
        return sharedPreferences.getString(LAST_TYPE_MSG, "");
    }
}
