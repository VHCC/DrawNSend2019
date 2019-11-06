package ichen.chu.drawnsend.pages.playboard;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.raycoarana.codeinputview.CodeInputView;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import cn.iwgang.countdownview.CountdownView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import ichen.chu.drawableviewlibs.DrawableView;
import ichen.chu.drawableviewlibs.DrawableViewConfig;
import ichen.chu.drawnsend.Bus;
import ichen.chu.drawnsend.BusEvent;
import ichen.chu.drawnsend.HoverMenu.HoverMenuFactory;
import ichen.chu.drawnsend.HoverMenu.theme.HoverTheme;
import ichen.chu.drawnsend.HoverMenu.theme.HoverThemeManager;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.api.DnsServerAgent;
import ichen.chu.drawnsend.util.MLog;
import ichen.chu.hoverlibs.HoverMenu;
import ichen.chu.hoverlibs.HoverView;
import ichen.chu.squareprogessbarlibs.SquareProgressBar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static ichen.chu.drawnsend.App.SERVER_SITE;

/**
 * Created by IChen.Chu on 2018/9/26
 */
public class PlayBoardMainFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // DrawableView
    private final DrawableViewConfig config = new DrawableViewConfig();
    private DrawableView drawableView;

    // Constants
    private long playTime = 10L;
    private long playTimeMs = playTime * 1000L;

    // Hover
    private HoverView mHoverView;

    //View
    private ShimmerTextView shimmerTV;

    // Listener
    private MainDrawableViewListener mainDrawableViewListener = new MainDrawableViewListener();
    private AvatarClickListener mAvatarClickListener = new AvatarClickListener();

    // Constructor
    public PlayBoardMainFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SubPagesMainFragment.
     */
    public static PlayBoardMainFragment newInstance() {
        PlayBoardMainFragment fragment = new PlayBoardMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mLog.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        Bus.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        Bus.getInstance().unregister(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_play_board_main, container, false);
        initUi(rootView);
        initHover(rootView);
        return rootView;
    }

    private void initUi(View rootView) {
        drawableView = (DrawableView) rootView.findViewById(R.id.paintView);

        CountdownView countdownView = (CountdownView) rootView.findViewById(R.id.countdownView);
        final SquareProgressBar sProgressBar = (SquareProgressBar) rootView.findViewById(R.id.sProgressBar);
        shimmerTV = (ShimmerTextView) rootView.findViewById(R.id.shimmerTV);
        CircleImageView playerAvatar_pre = rootView.findViewById(R.id.playerAvatar_pre);
        CircleImageView playerAvatar_next = rootView.findViewById(R.id.playerAvatar_next);
        playerAvatar_pre.setOnClickListener(mAvatarClickListener);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        new DownloadImageTask(playerAvatar_pre).execute(acct.getPhotoUrl().toString());
        new DownloadImageTask(playerAvatar_next).execute(acct.getPhotoUrl().toString());

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        config.setStrokeColor(HoverThemeManager.getInstance().getTheme().getAccentColor());
        config.setShowCanvasBounds(true);
        config.setStrokeWidth(10.0f);
        config.setMinZoom(1.0f);
        config.setMaxZoom(2.0f);
        config.setCanvasHeight(height);
        config.setCanvasWidth(width);
        drawableView.setConfig(config);

        drawableView.setSimpleFingerGesturesListener(mainDrawableViewListener);

        countdownView.start(playTimeMs); // Millisecond
        countdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                drawableView.setDisabled(true);
                Bus.getInstance().post(new BusEvent("send Pic", 12001));
                mLog.d(TAG, "onEnd");
            }
        });

        countdownView.setOnCountdownIntervalListener(playTime, new CountdownView.OnCountdownIntervalListener() {
            @Override
            public void onInterval(CountdownView cv, long remainTime) {
                long totalTime = playTimeMs;
                float remainPercents = ((float) remainTime / (float) totalTime);
//                mLog.d(TAG, "totalTime= " + totalTime + ", remainTime= " + remainTime + ", remainPercents= " + remainPercents);
                if (remainPercents < 0.50) {
                    sProgressBar.setColor("#FF0000");
                }
            }
        });

        sProgressBar.setWidth(8);
        sProgressBar.setOpacity(false, false);
        sProgressBar.showProgress(false);
        sProgressBar.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        sProgressBar.setImage(R.drawable.bg_white);
        sProgressBar.setColor("#3BF5CF");

        ValueAnimator va = ValueAnimator.ofFloat(0f, 100f);
        va.setDuration(playTimeMs);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                sProgressBar.setProgress((float) animation.getAnimatedValue());
            }
        });
        va.start();
    }

    private void initHover(View rootView) {
        mLog.d(TAG, "* initHover()");
        try {
            final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.AppTheme);
            final HoverMenu hoverMenu = new HoverMenuFactory().createDemoMenuFromCode(contextThemeWrapper, Bus.getInstance());

            mHoverView = rootView.findViewById(R.id.hovermenu);
            mHoverView.setMenu(hoverMenu);
            mHoverView.enableDebugMode(false);
            mHoverView.collapse();
        } catch (Exception e) {
            mLog.e(TAG, "Failed to create demo menu from file. e= " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void uploadFile(File file) {
        try {
            try {
                final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
                mLog.d(TAG, "file.getName()= " + file.getName());
                RequestBody req = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("name", file.getName())
                        .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE_JPEG, file)).build();

                Request request = new Request.Builder()
                        .url(SERVER_SITE + "/api/post_dns_upload_file")
                        .post(req)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                mLog.d(TAG, "uploadImage: " + response.body().string());

            } catch (UnknownHostException | UnsupportedEncodingException e) {
                mLog.e(TAG, "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onEventMainThread(@NonNull HoverTheme newTheme) {
        config.setStrokeColor(newTheme.getAccentColor());
    }

    public void onEventBackgroundThread(BusEvent event) {
        mLog.d(TAG, "event= " + event.getEventType());
        switch (event.getEventType()) {
            case 12001:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap outB = drawableView.obtainBitmap().copy(Bitmap.Config.ARGB_8888, true);
                        Canvas canvas = new Canvas(outB);
                        canvas.drawColor(Color.WHITE);
                        canvas.drawBitmap(drawableView.obtainBitmap(), 0, 0, null);

//                        image.setImageBitmap(outB);

                        String tmp = "/sdcard/test/" + System.currentTimeMillis() + ".jpeg";
                        File file = new File(tmp);

                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            if (outB.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                                out.flush();
                                out.close();
                            }
                            uploadFile(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
        }
    }

    public void onEventMainThread(@NonNull BusEvent event) {
//        mLog.d(TAG, "event= " + event.getEventType());
        switch (event.getEventType()) {
            case 5001:
                break;
            case 4001:
                config.setStrokeWidth(10);
                break;
            case 4002:
                config.setStrokeWidth(20);
                break;
            case 4003:
                config.setStrokeWidth(30);
                break;
            case 4004:
                config.setStrokeWidth(40);
                break;
            case 4005:
                config.setStrokeWidth(50);
                break;
            case 9002:
//                mLog.d(TAG, "event msg= " + event.getMessage());
                shimmerTV.setText(event.getMessage());
                shimmerTV.setVisibility(View.VISIBLE);
                final Shimmer shimmer = new Shimmer();
                shimmer.setRepeatCount(1)
                        .setDuration(1500)
                        .setStartDelay(0)
                        .setDirection(Shimmer.ANIMATION_DIRECTION_LTR)
                        .setAnimatorListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
//                                mLog.d(TAG, "onAnimationStart()");
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
//                                mLog.d(TAG, "onAnimationEnd()");
                                shimmerTV.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
//                                mLog.d(TAG, "onAnimationCancel()");
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
//                                mLog.d(TAG, "onAnimationRepeat()");
                            }
                        });
                shimmer.start(shimmerTV);
                break;
        }
    }



    private class MainDrawableViewListener implements DrawableView.DrawableViewListener {

        @Override
        public boolean onSwipeUp(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "swiped " + fingers + " up");
            switch (fingers) {
                case 3:
                    drawableView.undo();
                    Bus.getInstance().post(new BusEvent("Undo", 9002));
                    break;
            }
            return false;
        }

        @Override
        public boolean onSwipeDown(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "swiped " + fingers + " down");
            switch (fingers) {
                case 3:
                    drawableView.clear();
                    Bus.getInstance().post(new BusEvent("Clear All", 9002));
                    break;
            }
            return false;
        }

        @Override
        public boolean onSwipeLeft(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "swiped " + fingers + " left");
            switch (fingers) {
                case 3:
                    break;
            }
            return false;
        }

        @Override
        public boolean onSwipeRight(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "swiped " + fingers + " right");
            switch (fingers) {
                case 3:
                    break;
            }
            return false;
        }

        @Override
        public boolean onPinch(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "pinch");
            return false;
        }

        @Override
        public boolean onUnpinch(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "unpinch");
            return false;
        }

        @Override
        public boolean onDoubleTap(int fingers) {
            mLog.d(TAG, "onDoubleTap");
            return false;
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            mLog.d(TAG, "onTouch");
            return true;
        }
    }

    private class AvatarClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mLog.d(TAG, "click preview Avatar");
            LayoutInflater inflater = LayoutInflater.from(getContext());
            FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.show_preview_result,null);

            // init UI
            final ImageView preview_results = frameLayout.findViewById(R.id.preview_results);
            SweetAlertDialog saDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE);


            saDialog.setTitleText("Pre Stage")
                    .setCustomView(frameLayout)
                    .setConfirmText("OK")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                        }
                    })
                    .show();
        }
    }

    // -------------------------------------------
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
