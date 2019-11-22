package ichen.chu.drawnsend.pages.playboard;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.viewtooltip.ViewTooltip;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import cn.carbs.android.avatarimageview.library.AvatarImageView;
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
import ichen.chu.drawnsend.model.DnsGameChain;
import ichen.chu.drawnsend.model.DnsPlayRoom;
import ichen.chu.drawnsend.model.DnsResult;
import ichen.chu.drawnsend.util.MLog;
import ichen.chu.hoverlibs.HoverMenu;
import ichen.chu.hoverlibs.HoverView;
import ichen.chu.squareprogessbarlibs.SquareProgressBar;

import static ichen.chu.drawnsend.Bus.EVENT_DRAWABLE_CHANGE_STROKE_SIZE_1;
import static ichen.chu.drawnsend.Bus.EVENT_DRAWABLE_CHANGE_STROKE_SIZE_2;
import static ichen.chu.drawnsend.Bus.EVENT_DRAWABLE_CHANGE_STROKE_SIZE_3;
import static ichen.chu.drawnsend.Bus.EVENT_DRAWABLE_CHANGE_STROKE_SIZE_4;
import static ichen.chu.drawnsend.Bus.EVENT_DRAWABLE_CHANGE_STROKE_SIZE_5;
import static ichen.chu.drawnsend.Bus.EVENT_MAP;
import static ichen.chu.drawnsend.Bus.EVENT_PLAY_BOARD_UPLOAD_FILE_DONE;
import static ichen.chu.drawnsend.Bus.EVENT_PLAY_BOARD_UPLOAD_FILE_START;
import static ichen.chu.drawnsend.Bus.EVENT_PLAY_BOARD_UPLOAD_GAME_CHAIN_RESULT_DONE;
import static ichen.chu.drawnsend.api.APICode.API_FETCH_GAME_CHAIN_INFO;
import static ichen.chu.drawnsend.api.APICode.API_GET_FILE_THUMBNAIL_LINK;

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
    private long playTime;
    private long playTimeMs;

    // Hover
    private HoverView mHoverView;

    //View
    private ShimmerTextView shimmerTV;
    private CountdownView countdownView;
    private ValueAnimator valueAnimator;
    private CircleImageView playerAvatar_pre;
    private AvatarImageView item_avatar_pre;
    private CircleImageView playerAvatar_next;
    private AvatarImageView item_avatar_next;
    private TextView stageCountTV;
    private SweetAlertDialog loadingDialog;
    private SquareProgressBar sProgressBar;

    // Font Family
    Typeface mCustomFont = null;

    // Google
    private GoogleSignInAccount acct;

    // Listener
    private MainDrawableViewListener mainDrawableViewListener = new MainDrawableViewListener();
    private AvatarClickListener mAvatarClickListener = new AvatarClickListener();
    private OnPlayBoardMainFragmentInteractionListener onPlayBoardMainFragmentInteractionListener;


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

    boolean isSupportAnimation = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mLog.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        Bus.getInstance().register(this);
        isViewInitiated = true;

        switch (android.os.Build.MANUFACTURER) {
            case "HTC": {
                isSupportAnimation = false;
            }
            break;
            default:
                break;
        }

    }

    protected boolean isViewInitiated;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getUserVisibleHint() && isViewInitiated) {
            fetchGameChainData(acct.getEmail() + DnsPlayRoom.getInstance().getJoinNumber());
        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        mLog.d(TAG, "setUserVisibleHint()=  " + isVisibleToUser);
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isViewInitiated) {
            fetchGameChainData(acct.getEmail() + DnsPlayRoom.getInstance().getJoinNumber());
        }
    }


    private void lazyLoad() {
        GameConfigureView gameConfigureView = new GameConfigureView(getContext());
        gameConfigureView.readyDialogShow();
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
        acct = GoogleSignIn.getLastSignedInAccount(getContext());
        initUi(rootView);
        initHover(rootView);
        return rootView;
    }

    private void initUi(View rootView) {
        drawableView = (DrawableView) rootView.findViewById(R.id.paintView);

        countdownView = (CountdownView) rootView.findViewById(R.id.countdownView);
        sProgressBar = (SquareProgressBar) rootView.findViewById(R.id.sProgressBar);
        shimmerTV = (ShimmerTextView) rootView.findViewById(R.id.shimmerTV);
        playerAvatar_pre = rootView.findViewById(R.id.playerAvatar_pre);
        item_avatar_pre = rootView.findViewById(R.id.item_avatar_pre);
        playerAvatar_next = rootView.findViewById(R.id.playerAvatar_next);
        item_avatar_next = rootView.findViewById(R.id.item_avatar_next);
        stageCountTV = rootView.findViewById(R.id.stageCountTV);
        playerAvatar_pre.setOnClickListener(mAvatarClickListener);
        item_avatar_pre.setOnClickListener(mAvatarClickListener);

        mCustomFont = Typeface.createFromAsset(getContext().getAssets(), "Pacifico-Regular.ttf");

        mLog.d(TAG, "acct= " + acct);
        mLog.d(TAG, "acct.getEmail= " + acct.getEmail());
        mLog.d(TAG, "acct.getDisplayName= " + acct.getDisplayName());
        mLog.d(TAG, "acct.getPhotoUrl= " + acct.getPhotoUrl());

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        config.setStrokeColor(HoverThemeManager.getInstance().getTheme().getAccentColor());
        config.setShowCanvasBounds(true);
        config.setStrokeWidth(10.0f);
        config.setMinZoom(1.0f);
        config.setMaxZoom(1.0f);
        config.setCanvasHeight(height);
        config.setCanvasWidth(width);
        drawableView.setConfig(config);

        drawableView.post(new Runnable() {
            @Override
            public void run() {
                config.setCanvasHeight(drawableView.getHeight());
                config.setCanvasWidth(drawableView.getWidth());
                drawableView.setConfig(config);
            }
        });

        drawableView.setSimpleFingerGesturesListener(mainDrawableViewListener);

        countdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                drawableView.setDisabled(true);
                Bus.getInstance().post(new BusEvent(EVENT_MAP.get(EVENT_PLAY_BOARD_UPLOAD_FILE_START), EVENT_PLAY_BOARD_UPLOAD_FILE_START));
                mLog.d(TAG, "countdownView, onEnd");
                loadingDialog.show();
            }
        });

//        countdownView.setOnCountdownIntervalListener(playTime, new CountdownView.OnCountdownIntervalListener() {
//            @Override
//            public void onInterval(CountdownView cv, long remainTime) {
//                long totalTime = playTimeMs;
//                float remainPercents = ((float) remainTime / (float) totalTime);
////                mLog.d(TAG, "totalTime= " + totalTime + ", remainTime= " + remainTime + ", remainPercents= " + remainPercents);
//                if (remainPercents < 0.50) {
//                    sProgressBar.setColor("#FF0000");
//                }
//            }
//        });


        if (isSupportAnimation) {
            sProgressBar.setOpacity(false, false);
            sProgressBar.showProgress(false);
            sProgressBar.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
            sProgressBar.setImage(R.drawable.bg_white);
            sProgressBar.setWidth(8);
            sProgressBar.setColor("#3BF5CF");
            valueAnimator = ValueAnimator.ofFloat(0f, 100f);
//        valueAnimator.setDuration(playTimeMs);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    sProgressBar.setProgress((float) animation.getAnimatedValue());
                }
            });
        } else {
            sProgressBar.setVisibility(View.GONE);
        }
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

    public void onEventMainThread(@NonNull HoverTheme newTheme) {
        config.setStrokeColor(newTheme.getAccentColor());
    }

    public void onEventBackgroundThread(BusEvent event) {
        mLog.d(TAG, "* event= " + event.getEventType());
        switch (event.getEventType()) {
            case EVENT_PLAY_BOARD_UPLOAD_FILE_START:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap outB = drawableView.obtainBitmap().copy(Bitmap.Config.ARGB_8888, true);
                        Canvas canvas = new Canvas(outB);
                        canvas.drawColor(Color.WHITE);
                        canvas.drawBitmap(drawableView.obtainBitmap(), 0, 0, null);

//                        image.setImageBitmap(outB);

                        String tmp = "/sdcard/" + acct.getEmail() + "_" + System.currentTimeMillis() + ".jpeg";
                        File file = new File(tmp);

                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            if (outB.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                                out.flush();
                                out.close();
                            }
                            DnsServerAgent.getInstance(getContext())
                                    .uploadFile(file, DnsGameChain.getInstance().getParentID());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            file.delete();
                        }
                    }
                }).start();
                break;
            case EVENT_PLAY_BOARD_UPLOAD_FILE_DONE:
                try {
                    String targetChainID = DnsGameChain.getInstance().getPlayerChained().getJSONObject(0).getString("email") +
                            DnsPlayRoom.getInstance().getJoinNumber();
                    DnsServerAgent.getInstance(getContext())
                            .updateGameChainResult(targetChainID, DnsResult.getInstance().getResultID(), currentStage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case EVENT_PLAY_BOARD_UPLOAD_GAME_CHAIN_RESULT_DONE:
                try {
                    int players = DnsPlayRoom.getInstance().getParticipants().length(); // 最後一個
                    String chainID = DnsGameChain.getInstance().getPlayerChained().getJSONObject(players-1).getString("email") +
                            DnsPlayRoom.getInstance().getJoinNumber();
                    fetchGameChainData(chainID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void onEventMainThread(@NonNull BusEvent event) {
//        mLog.d(TAG, "event= " + event.getEventType());
        switch (event.getEventType()) {
            case 5001:
                break;
            case EVENT_DRAWABLE_CHANGE_STROKE_SIZE_1:
                config.setStrokeWidth(10);
                break;
            case EVENT_DRAWABLE_CHANGE_STROKE_SIZE_2:
                config.setStrokeWidth(20);
                break;
            case EVENT_DRAWABLE_CHANGE_STROKE_SIZE_3:
                config.setStrokeWidth(30);
                break;
            case EVENT_DRAWABLE_CHANGE_STROKE_SIZE_4:
                config.setStrokeWidth(40);
                break;
            case EVENT_DRAWABLE_CHANGE_STROKE_SIZE_5:
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
//            mLog.d(TAG, "swiped " + fingers + " up");
            switch (fingers) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    drawableView.undo();
                    Bus.getInstance().post(new BusEvent("Undo", 9002));
                    break;
            }
            return false;
        }

        @Override
        public boolean onSwipeDown(int fingers, long gestureDuration, double gestureDistance) {
//            mLog.d(TAG, "swiped " + fingers + " down");
            switch (fingers) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    drawableView.clear();
                    drawableView.setConfig(config);
                    Bus.getInstance().post(new BusEvent("Clear All", 9002));
                    break;
            }
            return false;
        }

        @Override
        public boolean onSwipeLeft(int fingers, long gestureDuration, double gestureDistance) {
//            mLog.d(TAG, "swiped " + fingers + " left");
            switch (fingers) {
                case 3:
                    break;
            }
            return false;
        }

        @Override
        public boolean onSwipeRight(int fingers, long gestureDuration, double gestureDistance) {
//            mLog.d(TAG, "swiped " + fingers + " right");
            switch (fingers) {
                case 3:
                    break;
            }
            return false;
        }

        @Override
        public boolean onPinch(int fingers, long gestureDuration, double gestureDistance) {
//            mLog.d(TAG, "pinch");
            return false;
        }

        @Override
        public boolean onUnpinch(int fingers, long gestureDuration, double gestureDistance) {
//            mLog.d(TAG, "unpinch");
            return false;
        }

        @Override
        public boolean onDoubleTap(int fingers) {
//            mLog.d(TAG, "onDoubleTap");
            return false;
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            mLog.d(TAG, "onTouch");
            return true;
        }
    }

    private class AvatarClickListener implements View.OnClickListener {

        private Shimmer shimmerInner = new Shimmer();

        @Override
        public void onClick(View v) {
            mLog.d(TAG, "click preview Avatar");
            LayoutInflater inflater = LayoutInflater.from(getContext());
            FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.show_preview_result,null);

            // init UI
            final ShimmerTextView subjectTV = frameLayout.findViewById(R.id.subjectTV);
            final ImageView preview_results = frameLayout.findViewById(R.id.preview_results);

            SweetAlertDialog saDialog_avatar = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE);

            subjectTV.setText(DnsGameChain.getInstance().getSubject());

            boolean isFirstStage = DnsGameChain.getInstance().getResultsChained().length() == 0;

            String title = isFirstStage ? "Your Subject" : "Pre Stage";

            saDialog_avatar.setTitleText(title)
                    .setCustomView(frameLayout)
                    .hideConfirmButton()
                    .show();

            shimmerInner.setRepeatCount(5)
                    .setDuration(2000)
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

            if (isFirstStage) {
                shimmerInner.start(subjectTV);
                preview_results.setVisibility(View.GONE);
            } else {
                preview_results.setVisibility(View.VISIBLE);
                subjectTV.setVisibility(View.GONE);
                new DownloadImageTask(preview_results).execute(resultUrl);
            }
        }
    }

    // -------------------------------------------
    public interface OnPlayBoardMainFragmentInteractionListener {
        void onGameSet();

    }

    public void setPlayBoardMainFragmentListener(OnPlayBoardMainFragmentInteractionListener listener) {
        onPlayBoardMainFragmentInteractionListener = listener;
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

    private String resultUrl;

    private class GameConfigureView {

        private Context mContext;
        private LayoutInflater inflater;

        private long readyViewTime = 10L;
        private long readyViewTimeMs = readyViewTime * 1000L;

        private Handler mySADHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.arg1) {
                    case API_GET_FILE_THUMBNAIL_LINK:
                        resultUrl = msg.obj.toString();
                        new DownloadImageTask(preview_results).execute(resultUrl);
                        break;
                }
            }
        };

        public GameConfigureView(Context context) {
            mLog.d(TAG, " = GameConfigureView = ");
            mContext = context;
            drawableView.clear();
            drawableView.setConfig(config);
            initUI();
            initFeature();
        }

        // VIEW
        private FrameLayout frameLayoutInner;
        private ShimmerTextView subjectTV;
        private CountdownView countdownViewInner;
        private SweetAlertDialog saDialog;
        private ImageView preview_results;
        private TextView tv1;
        private TextView tv2;
        private Shimmer shimmerInner = new Shimmer();

        private void initUI() {
            inflater = LayoutInflater.from(mContext);
            frameLayoutInner = (FrameLayout) inflater.inflate(R.layout.room_configure_view_frame_layout,null);
            subjectTV = frameLayoutInner.findViewById(R.id.subjectTV);
            countdownViewInner = frameLayoutInner.findViewById(R.id.countdownViewInner);
            preview_results = frameLayoutInner.findViewById(R.id.preview_results);
            tv1 = frameLayoutInner.findViewById(R.id.tv1);
            tv2 = frameLayoutInner.findViewById(R.id.tv2);
            saDialog = new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE);

        }

        private void initFeature() {
            subjectTV.setText(DnsGameChain.getInstance().getSubject());

            countdownViewInner.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
                @Override
                public void onEnd(CountdownView cv) {
                    mLog.d(TAG, "** config view onEnd");
                    saDialog.setCancelable(true);
                    saDialog.dismissWithAnimation();
                    countdownView.setVisibility(View.VISIBLE);
                    countdownView.start(playTimeMs); // Millisecond
                    ViewTooltip
                            .on(getActivity(), item_avatar_pre)
                            .autoHide(true, 3000)
                            .corner(30)
                            .position(ViewTooltip.Position.TOP)
                            .text("Your Subject")
                            .textTypeFace(mCustomFont)
                            .show();
                    if (isSupportAnimation) {
                        valueAnimator.start();
                    }
                }
            });


            saDialog.setCancelable(false);
            saDialog.hideConfirmButton();

            saDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mLog.d(TAG, "** config view onDismiss");
                    drawableView.setDisabled(false);
                }
            });

            shimmerInner.setRepeatCount(5)
                    .setDuration(2000)
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


        }

        public void readyDialogShow() {
            saDialog.setTitleText("Getting ready")
                    .setCustomView(frameLayoutInner)
                    .show();
            countdownViewInner.start(readyViewTimeMs); // Millisecond


            mLog.d(TAG, "readyDialogShow, isFirstStage= " + isFirstStage);
            if (isFirstStage) {
                shimmerInner.start(subjectTV);
            } else {
                subjectTV.setVisibility(View.GONE);
                preview_results.setVisibility(View.VISIBLE);
                tv1.setVisibility(View.GONE);
                tv2.setVisibility(View.GONE);
                try {
                    String pre_results = DnsGameChain.getInstance().getResultsChained().get(currentStage-1).toString();
                    mLog.d(TAG, "pre_results= " + pre_results);
                    DnsServerAgent.getInstance(getContext())
                            .fetchFileThumbnailLinkByFileID(mySADHandler, pre_results);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isFirstStage = false;
    private boolean isNeedValidateStage = false;

    private Handler playBoardHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            mLog.d(TAG, "msg.obj= " + msg.obj);
            switch (msg.arg1) {
                case API_FETCH_GAME_CHAIN_INFO:

                    if (!DnsPlayRoom.getInstance().isTestMode()) {
                        DnsGameChain.getInstance().setGameChainInfo((JSONObject) msg.obj);
                        mLog.d(TAG, DnsGameChain.getInstance().toString());

                        try {
                            // Valid stage by
                            if ((currentStage + 1) != DnsGameChain.getInstance().getResultsChained().length() && isNeedValidateStage) {
                                String chainID = null;

                                chainID = DnsGameChain.getInstance().getPlayerChained().getJSONObject(0).getString("email") +
                                        DnsPlayRoom.getInstance().getJoinNumber();
                                fetchGameChainData(chainID);
                                Thread.sleep(500);
                                break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        isFirstStage = (DnsGameChain.getInstance().getResultsChained().length() == 0);
                        mLog.d(TAG, "* isFirstStage= " + isFirstStage);
                        if (isFirstStage) {
                            isNeedValidateStage = true;
                            try {

                                if (!(((JSONObject)DnsGameChain.getInstance().getPlayerChained().get(1))).has("photoUrl")) {
                                    item_avatar_next.setTextAndColorSeed(
                                            String.valueOf(((JSONObject)DnsGameChain.getInstance().getPlayerChained().get(1)).getString("displayName").charAt(0)),
                                            ((JSONObject)DnsGameChain.getInstance().getPlayerChained().get(1)).getString("displayName"));
                                } else {
                                    new DownloadImageTask(item_avatar_next).execute((String) ((JSONObject)DnsGameChain.getInstance().getPlayerChained().get(1)).get("photoUrl"));
                                }
//                            String nextUrl = ((JSONObject)DnsGameChain.getInstance().getPlayerChained().get(1)).getString("photoUrl");
//                            mLog.d(TAG, "nextUrl= " + nextUrl);
//                            new DownloadImageTask(playerAvatar_next).execute(nextUrl);

                                if (!(((JSONObject)DnsGameChain.getInstance().getPlayerChained().get(DnsGameChain.getInstance().getPlayerChained().length()-1))).has("photoUrl")) {
                                    item_avatar_pre.setTextAndColorSeed(
                                            String.valueOf(((JSONObject)DnsGameChain.getInstance().getPlayerChained().get(DnsGameChain.getInstance().getPlayerChained().length()-1)).getString("displayName").charAt(0)),
                                            ((JSONObject)DnsGameChain.getInstance().getPlayerChained().get(DnsGameChain.getInstance().getPlayerChained().length()-1)).getString("displayName"));
                                } else {
                                    new DownloadImageTask(item_avatar_pre).execute((String) ((JSONObject)DnsGameChain.getInstance().getPlayerChained().get(1)).get("photoUrl"));
                                }
//                            String preUrl = ((JSONObject)DnsGameChain.getInstance().getPlayerChained().get(DnsGameChain.getInstance().getPlayerChained().length()-1)).getString("photoUrl");
//                            mLog.d(TAG, "preUrl= " + preUrl);
//                            new DownloadImageTask(playerAvatar_pre).execute(preUrl);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        if (null != loadingDialog && loadingDialog.isShowing()) {
                            loadingDialog.setCancelable(true);
                            loadingDialog.dismissWithAnimation();
                        }

                        loadingDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
                        loadingDialog.setCancelable(false);
                        loadingDialog.hideConfirmButton();

                        currentStage = DnsGameChain.getInstance().getResultsChained().length();

                        mLog.d(TAG, "currentStage= " + currentStage);

                        if (currentStage >= DnsGameChain.getInstance().getPlayerChained().length()) {
                            isNeedValidateStage = false;
                            currentStage = 0;
                            loadingDialog.show();
                            onPlayBoardMainFragmentInteractionListener.onGameSet();
                            loadingDialog.dismissWithAnimation();
                        } else {
                            stageCountTV.setText("Stage: " +
                                    (currentStage + 1 )+ " / " +
                                    DnsGameChain.getInstance().getPlayerChained().length());
                            lazyLoad();
                        }
                        break;
                    }

            }

        }
    };

    private int currentStage = 0;

    private void fetchGameChainData(String chainID) {

        playTime = Long.valueOf(DnsPlayRoom.getInstance().getPlayTime()) * 1L;
//        playTime = 10L;
        playTimeMs = playTime * 1000L;
        if (isSupportAnimation) {
            valueAnimator.setDuration(playTimeMs);
        }

        countdownView.setOnCountdownIntervalListener(playTime, new CountdownView.OnCountdownIntervalListener() {
            @Override
            public void onInterval(CountdownView cv, long remainTime) {
                long totalTime = playTimeMs;
                float remainPercents = ((float) remainTime / (float) totalTime);
//                mLog.d(TAG, "totalTime= " + totalTime + ", remainTime= " + remainTime + ", remainPercents= " + remainPercents);
                if (remainPercents < 0.50) {
                    sProgressBar.setColor("#FF0000");
                } else {
                    sProgressBar.setColor("#3BF5CF");
                }
            }
        });

        DnsServerAgent.getInstance(getContext())
                .fetchGameChainInfo(playBoardHandler,
                        chainID);
    }



}
