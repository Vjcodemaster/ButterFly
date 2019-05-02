package app_utility;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import com.vj.butterfly.R;

import androidx.annotation.NonNull;


public class CircularProgressBar extends Dialog {

    public CircularProgressBar(@NonNull Context context) {
        super(context);
    }
    /*private Activity activity;

    public CircularProgressBar(Activity activity){
        super(activity);
        this.activity = activity;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.circular_progress_dialog);
        //makes background of dialog transperent so that we can add shadow effect to cardview
        if (getWindow() != null)
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        /*ProgressBar mProgressBar = findViewById(R.id.login_progress);
        ObjectAnimator anim = ObjectAnimator.ofInt(mProgressBar, "progress", 0, 100);
        anim.setDuration(15000);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();*/
    }

}
