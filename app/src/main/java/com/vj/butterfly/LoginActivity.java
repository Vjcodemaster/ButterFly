package com.vj.butterfly;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import app_utility.CircularProgressBar;
import app_utility.PermissionHandler;
import app_utility.SharedPreferenceClass;

import static app_utility.PermissionHandler.APP_PERMISSION;

public class LoginActivity extends AppCompatActivity {

    TextView tvPutNumberHere;
    TextInputLayout etPhoneNumber;
    public DatabaseReference dbReference;
    private int nPermissionFlag = 0;
    int countOfPhoneNumber = 0;
    private CircularProgressBar circularProgressBar;
    private SharedPreferenceClass sharedPreferenceClass;
    String[] saFunnyCaptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initClasses();

        initViews();

        initListeners();
    }

    private void initClasses() {
        dbReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferenceClass = new SharedPreferenceClass(LoginActivity.this);
        saFunnyCaptions = (getResources().getStringArray(R.array.like_him));
    }

    private void initViews() {
        tvPutNumberHere = findViewById(R.id.tv_put_num);
        etPhoneNumber = findViewById(R.id.et_phone_number);

        circularProgressBar = new CircularProgressBar(LoginActivity.this);
        /*Typeface face = Typeface.createFromAsset(getAssets(), "roboto_condensed_regular.ttf");
        tvPutNumberHere.setTypeface(face);*/
    }

    private void initListeners() {
        etPhoneNumber.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String sPhoneNumber = etPhoneNumber.getEditText().getText().toString();
                if (sPhoneNumber.length() == 10) {
                    if (sPhoneNumber.equals("9036640528") || sPhoneNumber.equals("9110886193")) {
                        Snackbar.make(findViewById(android.R.id.content), saFunnyCaptions[countOfPhoneNumber], Snackbar.LENGTH_SHORT).show();
                        countOfPhoneNumber = countOfPhoneNumber + 1;
                    } else {
                        if (sPhoneNumber.equals("8495964996")) {
                            showProgressBar();
                            sharedPreferenceClass.setUserLogStatus(true, getResources().getString(R.string.b), getContactName("9036640528"), sPhoneNumber);
                            Intent in = new Intent(LoginActivity.this, HomeScreenActivity.class);
                            startActivity(in);
                            //validateInNewThread(sPhoneNumber);
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "It's not your number :)", Snackbar.LENGTH_SHORT).show();
                        }
                        hideKeyboard();
                    }
                }
            }
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void validateInNewThread(final String phone) {


        new Thread() {
            public void run() {
                LoginActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(phone)) {
                                    //sharedPreferenceClass.setUserLogStatus(true, getResources().getString(R.string.b), getContactName(phone), );
                                    Intent in = new Intent(LoginActivity.this, HomeScreenActivity.class);
                                    startActivity(in);
                                } else {
                                    Snackbar.make(findViewById(android.R.id.content), "It's not your number :)", Snackbar.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }

                        });
                        stopProgressBar();
                    }
                });
                //Thread.sleep(300);
            }
        }.start();
    }

    public String getContactName(final String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = "";
        Cursor cursor =  getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!PermissionHandler.hasPermissions(LoginActivity.this, APP_PERMISSION)) {
            ActivityCompat.requestPermissions(LoginActivity.this, APP_PERMISSION, 1);
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
                            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.READ_CONTACTS)) {
                                //Toast.makeText(SignInActivity.this, "not given", Toast.LENGTH_SHORT).show();
                                sMSG.append("Contacts, ");
                                nPermissionFlag = 0;
                            } else {
                                //Never ask again selected, or device policy prohibits the app from having that permission.
                                //So, disable that feature, or fall back to another situation...
                                //Toast.makeText(SignInActivity.this, "permission never ask", Toast.LENGTH_SHORT).show();
                                sMSG.append("Contacts, ");
                                nPermissionFlag = 0;
                            }
                        }
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                //Toast.makeText(SignInActivity.this, "not given", Toast.LENGTH_SHORT).show();
                                sMSG.append("Storage, ");
                                nPermissionFlag = 0;
                            } else {
                                //Never ask again selected, or device policy prohibits the app from having that permission.
                                //So, disable that feature, or fall back to another situation...
                                //Toast.makeText(SignInActivity.this, "permission never ask", Toast.LENGTH_SHORT).show();
                                sMSG.append("Storage, ");
                                nPermissionFlag = 0;
                            }
                        }
                        break;
                }
            }
            if (!sMSG.toString().equals("") && !sMSG.toString().equals(" ")) {
                PermissionHandler permissionHandler = new PermissionHandler(LoginActivity.this, 0, sMSG.toString(), nPermissionFlag);
            }
        }
    }

    private void showProgressBar() {
        circularProgressBar.setCanceledOnTouchOutside(false);
        circularProgressBar.setCancelable(false);
        circularProgressBar.show();
    }

    private void stopProgressBar() {
        if (circularProgressBar != null && circularProgressBar.isShowing())
            circularProgressBar.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 3:
                if (resultCode != Activity.RESULT_OK) {
                    LoginActivity.this.finish();
                }
                break;
        }
    }
}
