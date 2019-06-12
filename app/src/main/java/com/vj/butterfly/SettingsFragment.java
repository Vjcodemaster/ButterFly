package com.vj.butterfly;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app_utility.SharedPreferenceClass;
import library.CircleImageView;

import static app_utility.StaticReferenceClass.PICTURE_REQUEST_CODE;
//import static app_utility.StaticReferenceClass.REGISTER_IMAGE_REQUEST_CODE;


/*
 *
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String sDialogCase;

    private FrameLayout frameLayout;
    private String mParam1;
    private String mParam2;

    private File sdImageMainDirectory;
    private Uri outputFileUri;

    private ImageButton ibProfileEdit;

    private CircleImageView civDPPreview;

    private int shortAnimationDuration;
    private Animator currentAnimator;

    private LinearLayout llNickName, llThink;

    private TextView tvNickName, tvThinkAbout;

    private SharedPreferenceClass sharedPreferenceClass;

    TextInputLayout etDialogInput;
    //private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initClasses();
        initViews(view);
        initListeners(view);

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        return view;
    }

    private void initClasses() {
        sharedPreferenceClass = new SharedPreferenceClass(getActivity());
    }

    private void initViews(final View view) {
        /*Toolbar toolbar = view.findViewById(R.id.settings_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }*/
        llNickName = view.findViewById(R.id.ll_nick_name);
        llThink = view.findViewById(R.id.ll_think);

        frameLayout = view.findViewById(R.id.container);
        ibProfileEdit = view.findViewById(R.id.ib_edit_image);
        civDPPreview = view.findViewById(R.id.civ_profile_preview);

        tvNickName = view.findViewById(R.id.tv_nick_name);
        tvThinkAbout = view.findViewById(R.id.tv_thinking_about);
    }

    private void initListeners(final View view) {
        llNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sDialogCase = "NICK_NAME";
                openDialog(sDialogCase);
            }
        });

        llThink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sDialogCase = "THINK";
                openDialog(sDialogCase);
            }
        });
        civDPPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomImageFromThumb(view, civDPPreview, R.drawable.vj);
                frameLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        });

        ibProfileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeScreenActivity.isUserSelectingImage = true;
                openImageIntent();
            }
        });

        tvNickName.setText(sharedPreferenceClass.getUserName());

        if (!sharedPreferenceClass.getThinking().equals("")) {
            tvThinkAbout.setText(sharedPreferenceClass.getThinking().split("##,")[0]);
        }
    }

    private void openDialog(final String sDialogCase) {
        String sHeading = "";
        final TextView[] tvThinkArray;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());
        View view2 = layoutInflaterAndroid.inflate(R.layout.dialog_settings, null);
        etDialogInput = view2.findViewById(R.id.et_dialog_input);

        switch (sDialogCase) {
            case "NICK_NAME":
                sHeading = "Enter your Nick Name";
                break;
            case "THINK":
                sHeading = "What are you up to?";
                ArrayList<String> alThink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.think)));
                if (!sharedPreferenceClass.getThinking().equals(""))
                    alThink.addAll(Arrays.asList(sharedPreferenceClass.getThinking().split("##,")));

                tvThinkArray = new TextView[alThink.size()];
                LinearLayout llDynamicTv = view2.findViewById(R.id.ll_dynamic_tv);


                for (int i = 0; i < alThink.size(); i++) {
                    TextView tv = new TextView(getActivity());
                    /*View view = new View(getActivity());
                    view.setBackgroundColor(getResources().getColor(R.color.colorLightBlueTint));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
                    params.setMarginStart(10);
                    params.setMarginEnd(10);
                    view.setLayoutParams(params);*/
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 10, 0, 0);
                    //params.setMarginEnd(10);
                    tv.setLayoutParams(params);
                    tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                    if (Build.VERSION.SDK_INT < 23) {
                        tv.setTextAppearance(getActivity(), R.style.TextAppearance_AppCompat_Medium);
                    } else {
                        tv.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
                    }
                    tv.setText(alThink.get(i));
                    TypedValue outValue = new TypedValue();
                    getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                    tv.setBackgroundResource(outValue.resourceId);

                    tvThinkArray[i] = tv;
                    final int finalI = i;
                    tvThinkArray[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String sThink = tvThinkArray[finalI].getText().toString();
                            etDialogInput.getEditText().setText(sThink);
                        }
                    });
                    //llDynamicTv.addView(view);
                    llDynamicTv.addView(tv);
                }
                break;
        }

        builder.setView(view2);
        builder.setCancelable(false);
        builder.setTitle(sHeading);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String s = etDialogInput.getEditText().getText().toString();
                if (sDialogCase.equals("THINK")) {
                    if (!s.equals("")) {
                        tvThinkAbout.setText(s);
                        String sThinking = sharedPreferenceClass.getThinking();
                        if (!sThinking.equals(""))
                            sThinking = sThinking + "##," + s;
                        else
                            sThinking = s;
                        sharedPreferenceClass.setThinking(sThinking);
                    }
                } else {
                    tvNickName.setText(s);
                    sharedPreferenceClass.setUserName(s);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    private void openImageIntent() {
        //onImageUtilsListener.onBitmapCompressed("SHOW_PROGRESS_BAR",1,null, null, null);
        //MainActivity.homeInterfaceListener.onHomeCalled("SHOW_PROGRESS_BAR", 0, null, null);

        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory().getPath() +
                File.separator + "Android/data/" + File.separator + getActivity().getPackageName() + File.separator);
        root.mkdirs();
        final String fname = System.currentTimeMillis() + "profile";
        sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getActivity().getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Choose");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        //MainActivity.homeInterfaceListener.onHomeCalled("FILE_URI", REGISTER_IMAGE_REQUEST_CODE, null, outputFileUri);
        getActivity().startActivityForResult(chooserIntent, PICTURE_REQUEST_CODE);
        //onImageUtilsListener.onBitmapCompressed("START_ACTIVITY_FOR_RESULT",1,null, chooserIntent, outputFileUri);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    private void zoomImageFromThumb(final View view, final View thumbView, int imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) view.findViewById(
                R.id.iv_expanded_dp);
        expandedImageView.setImageResource(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        view.findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;


        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;

                frameLayout.setBackgroundColor(getResources().getColor(R.color.colorNextToWhite));
            }
        });
    }

}
