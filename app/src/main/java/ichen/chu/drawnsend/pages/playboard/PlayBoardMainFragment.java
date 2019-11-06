package ichen.chu.drawnsend.pages.playboard;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import cn.iwgang.countdownview.CountdownView;
import ichen.chu.drawableviewlibs.DrawableView;
import ichen.chu.drawableviewlibs.DrawableViewConfig;
import ichen.chu.drawnsend.Bus;
import ichen.chu.drawnsend.BusEvent;
import ichen.chu.drawnsend.HoverMenu.HoverMenuFactory;
import ichen.chu.drawnsend.HoverMenu.theme.HoverTheme;
import ichen.chu.drawnsend.HoverMenu.theme.HoverThemeManager;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.util.MLog;
import ichen.chu.hoverlibs.HoverMenu;
import ichen.chu.hoverlibs.HoverView;
import ichen.chu.hoverlibs.OnExitListener;
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
    private final DrawableViewConfig config = new DrawableViewConfig();
    private DrawableView drawableView;
    private HoverView mHoverView;

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
        Button strokeWidthMinusButton = (Button) rootView.findViewById(R.id.strokeWidthMinusButton);
        strokeWidthMinusButton.setVisibility(View.GONE);
        Button strokeWidthPlusButton = (Button) rootView.findViewById(R.id.strokeWidthPlusButton);
        strokeWidthPlusButton.setVisibility(View.GONE);
        final Button changeColorButton = (Button) rootView.findViewById(R.id.changeColorButton);
        Button undoButton = (Button) rootView.findViewById(R.id.undoButton);
        undoButton.setVisibility(View.GONE);
        Button clearButton = (Button) rootView.findViewById(R.id.clearButton);
        Button getButton = (Button) rootView.findViewById(R.id.getButton);
        CountdownView countdownView = (CountdownView) rootView.findViewById(R.id.countdownView);
        final SquareProgressBar sProgressBar = (SquareProgressBar) rootView.findViewById(R.id.sProgressBar);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        config.setStrokeColor(HoverThemeManager.getInstance().getTheme().getAccentColor());
        config.setShowCanvasBounds(true);
        config.setStrokeWidth(20.0f);
        config.setMinZoom(1.0f);
        config.setMaxZoom(2.0f);
        config.setCanvasHeight(height);
        config.setCanvasWidth(width);
        drawableView.setConfig(config);

        drawableView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLog.d(TAG, " * onLongClick");
                return false;
            }
        });

        strokeWidthPlusButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                config.setStrokeWidth(config.getStrokeWidth() + 10);
            }
        });
        strokeWidthMinusButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                config.setStrokeWidth(config.getStrokeWidth() - 10);
            }
        });
        changeColorButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Random random = new Random();
                int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                config.setStrokeColor(color);
                changeColorButton.setBackgroundColor(color);
            }
        });
        undoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawableView.undo();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawableView.clear();
            }
        });

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

            }
        });

        countdownView.start(10 * 1000); // Millisecond
        countdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
            @Override
            public void onEnd(CountdownView cv) {
                mLog.d(TAG, "onEnd");
            }
        });
        countdownView.setOnCountdownIntervalListener(10, new CountdownView.OnCountdownIntervalListener() {
            @Override
            public void onInterval(CountdownView cv, long remainTime) {
                mLog.d(TAG, "remainTime= " + remainTime);
            }
        });


        sProgressBar.setWidth(8);
        sProgressBar.setOpacity(false, false);
        sProgressBar.showProgress(false);
        sProgressBar.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        sProgressBar.setImage(R.drawable.bg_white);

        ValueAnimator va = ValueAnimator.ofFloat(0f, 100f);
        va.setDuration(10 * 1000);
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
            mHoverView.setOnExitListener(new OnExitListener() {
                @Override
                public void onExit() {
                    Bus.getInstance().post(new BusEvent("Hover View reattach", 5001));
                }
            });
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
                RequestBody req = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("name", file.getName())
                        .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE_JPEG, file)).build();

                Request request = new Request.Builder()
                        .url(SERVER_SITE + "/api/post_official_doc_upload_file")
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

    public void onEventMainThread(@NonNull BusEvent event) {
        mLog.d(TAG, "event= " + event.getEventType());
        switch (event.getEventType()) {
            case 5001:
                break;
        }
    }
}
