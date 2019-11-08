package ichen.chu.drawnsend.pages.dashboard;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.raycoarana.codeinputview.CodeInputView;
import com.raycoarana.codeinputview.OnCodeCompleteListener;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import ichen.chu.drawnsend.Bus;
import ichen.chu.drawnsend.BusEvent;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.api.DnsServerAgent;
import ichen.chu.drawnsend.model.PlayerItem;
import ichen.chu.drawnsend.pages.dashboard.ListAdapter.PlayerItemAdapter;
import ichen.chu.drawnsend.util.MLog;
import info.hoang8f.widget.FButton;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static ichen.chu.drawnsend.App.SERVER_SITE;
import static ichen.chu.drawnsend.Bus.EVENT_DASHBOARD_GET_PLAYER_ORDER;
import static ichen.chu.drawnsend.Bus.EVENT_DRAWABLE_CHANGE_STROKE_SIZE_1;
import static ichen.chu.drawnsend.Bus.EVENT_LOGIN_SUCCESS;
import static ichen.chu.drawnsend.Bus.EVENT_MAP;
import static ichen.chu.drawnsend.api.APICode.*;

/**
 * Created by IChen.Chu on 2018/9/25
 * A fragment to show home page.
 */
public class DashboardMainFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // View
    private Button googleSignOutBtn;
    private CircleImageView profile_image;
    private TextView accountEmailTV;
    private FloatingActionButton signOutFAB;
    private FloatingActionButton joinRoomFAB;
    private FloatingActionButton createRoomFAB;

    // RecycleView
    private RecyclerView recycleViewPlayerListContainer;
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    private GridLayoutManager gridLayoutManager;

    /*data Block*/
    private PlayerItemAdapter playerItemAdapter;

    // Constants

    // Handler

    /**
     * storage the result of event search.
     */
    private final List<PlayerItem> playerItemsList = new ArrayList<>();

    // Listener
    private OnDashboardMainFragmentInteractionListener mHomeFragmentListener;


    // Fields
    private GoogleSignInClient mGoogleSignInClient;

    public DashboardMainFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of HomeFragment.
     */
    public static DashboardMainFragment newInstance() {
        DashboardMainFragment fragment = new DashboardMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        Bus.getInstance().registerSticky(this);

        initGoogleAPI();
    }

    @Override
    public void onDestroy() {
        Bus.getInstance().unregister(this);
        super.onDestroy();
    }

    private void initGoogleAPI() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard_main, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        googleSignOutBtn = rootView.findViewById(R.id.googleSignOutBtn);
        profile_image = rootView.findViewById(R.id.profile_image);
        accountEmailTV = rootView.findViewById(R.id.accountEmailTV);
        signOutFAB = rootView.findViewById(R.id.signOutFAB);
        joinRoomFAB = rootView.findViewById(R.id.joinRoomFAB);
        createRoomFAB = rootView.findViewById(R.id.createRoomFAB);

    }


    private void initViewsFeature() {

        googleSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }

            private void signOut() {
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mLog.d(TAG, "signOut Complete");
                                mHomeFragmentListener.onLogOutSuccess();
                            }
                        });
            }
        });

        signOutFAB.setIcon(R.drawable.sign_out_icon);
        signOutFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }

            private void signOut() {
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mLog.d(TAG, "signOut Complete");
                                mHomeFragmentListener.onLogOutSuccess();
                            }
                        });
            }
        });

        // ******************** JOIN *************************
        joinRoomFAB.setIcon(R.drawable.join_room);
        joinRoomFAB.setOnClickListener(new JoinRoomClickListener());

        // ******************** CREATE *************************
        createRoomFAB.setIcon(R.drawable.create_room);
        createRoomFAB.setOnClickListener(new CreateRoomListener());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void userLoginSucceed() {
        mLog.d(TAG, "userLoginSucceed");
//        mViewPager.setCurrentItem(PAGE_INSPECTION, false);
    }

    public void userLogOutSucceed() {
        mLog.d(TAG, "userLogOutSucceed");
//        mViewPager.setCurrentItem(PAGE_INSPECTION, false);
    }

    // -------------------------------------------
    public interface OnDashboardMainFragmentInteractionListener {
        void onLogOutSuccess();

        void onStartToPlayGame();
    }

    public void setDashboardMainFragmentListener(OnDashboardMainFragmentInteractionListener listener) {
        mHomeFragmentListener = listener;
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

    // Event Bus
    public void onEventMainThread(BusEvent event){
//        event.getMessage();
        mLog.d(TAG, "event= " + event.getMessage());

        switch (event.getEventType()) {
            case EVENT_LOGIN_SUCCESS:
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
                new DownloadImageTask(profile_image).execute(acct.getPhotoUrl().toString());
                accountEmailTV.setText(acct.getEmail());
                break;
        }
    }


    private class ThreadObject extends Object {

        boolean isRunning = true;

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }
    }

    private class JoinRoomClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mLog.d(TAG, "click joinRoomFAB");

            final ThreadObject threadObject = new ThreadObject();
            threadObject.setRunning(false);

            LayoutInflater inflater = LayoutInflater.from(getContext());
            FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.join_room_frame_layout,null);
            final CodeInputView codeInputView = frameLayout.findViewById(R.id.roomCodeInput);

            codeInputView.addOnCompleteListener(new OnCodeCompleteListener() {
                @Override
                public void onCompleted(String code) {
                    mLog.d(TAG, "code= " + code);
                }
            });

            final TextView joinTV = frameLayout.findViewById(R.id.joinTV);

            // Player Recycler View
            recycleViewPlayerListContainer = frameLayout.findViewById(R.id.recycleViewPlayerListContainer);

            final PlayerItemAdapter playerItemAdapter = new PlayerItemAdapter(getContext(), playerItemsList);
            gridLayoutManager = new GridLayoutManager(getContext(), 5);
            recycleViewPlayerListContainer.setAdapter(playerItemAdapter);
            recycleViewPlayerListContainer.setLayoutManager(gridLayoutManager);
            recycleViewPlayerListContainer.setNestedScrollingEnabled(false);

            final Handler mySADHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
//                        Log.d(TAG, "msg.obj= " + msg.obj);
                    try {
                        JSONObject responseJ = (JSONObject) ((JSONArray) msg.obj).get(0);

                        int roomStatus = (int) responseJ.get("roomStatus");

                        switch (roomStatus) {
                            case 2:
                                joinTV.setText("遊戲中");
                                break;
                            case 3:
                                joinTV.setText("房間已經關閉");
                                break;
                        }

                        JSONArray jsonArray = (JSONArray) responseJ.get("participants");

                        mLog.d(TAG, "- participants= " + jsonArray.length());

                        List<PlayerItem> playerItemsListTemp = new ArrayList<>();


                        for (int index = 0; index < jsonArray.length(); index ++) {
                            PlayerItem item = new PlayerItem(
                                    PlayerItem.TYPE.PARTICIPANTS,
                                    (JSONObject) jsonArray.get(index)
                            );
                            playerItemsListTemp.add(item);
                        }

//                        mLog.d(TAG, "playerItemsList.containsAll(playerItemsListTemp)= " + playerItemsList.contains(playerItemsListTemp));

                        if (!playerItemsList.contains(playerItemsListTemp)) {
                            playerItemsList.clear();

                            for (int index = 0; index < jsonArray.length(); index ++) {
                                PlayerItem item = new PlayerItem(
                                        PlayerItem.TYPE.PARTICIPANTS,
                                        (JSONObject) jsonArray.get(index)
                                );
                                playerItemsList.add(item);
                            }

                            playerItemAdapter.clearAll();
                            playerItemAdapter.refreshList();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });

            final Thread checkRoomInfoThread = new Thread(new Runnable() {

                private static final long task_minimum_tick_time_msec = 1000; // 1 second

                @Override
                public void run() {
                    long tick_count = 0;
                    mLog.d(TAG, "task_minimum_tick_time_msec= " + (task_minimum_tick_time_msec));
                    mLog.d(TAG, "threadObject.isRunning()= " + threadObject.isRunning());

                    while (threadObject.isRunning()) {
                        try {

                            long start_time_tick = System.currentTimeMillis();
                            // real-time task

                            if (tick_count % 5 == 1) {
                                mLog.d(TAG, "* fetchPlayRoomInfo");
                                DnsServerAgent.getInstance(getContext())
                                        .fetchPlayRoomInfo(mySADHandler, codeInputView.getCode());
                            }

                            long end_time_tick = System.currentTimeMillis();

                            if (end_time_tick - start_time_tick > task_minimum_tick_time_msec) {
                                mLog.w(TAG, "Over time process " + (end_time_tick - start_time_tick));
                            } else {
                                Thread.sleep(task_minimum_tick_time_msec);
                            }
                            tick_count++;
                        } catch (InterruptedException e) {
                            mLog.d(TAG, "appRunnable interrupted");
                        }
                    }
                }
            });

            SweetAlertDialog saDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE);

            saDialog.setCancelable(false);

            saDialog.setTitleText("Join a Room")
                    .setConfirmText("Join")
                    .setCancelText("Quit")
                    .setCustomView(frameLayout)
//                        .hideConfirmButton()
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog sDialog) {
                            mLog.d(TAG, "code= " + codeInputView.getCode());
                            threadObject.setRunning(true);

                            final Handler SADHandler = new Handler(new Handler.Callback() {
                                @Override
                                public boolean handleMessage(Message msg) {
                                    Log.d(TAG, "msg.what= " + msg.what);

                                    switch (msg.what) {
                                        case 0:
                                            sDialog.hideConfirmButton();
                                            sDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                            break;
                                        case 1:
                                            checkRoomInfoThread.start();
                                            sDialog.hideConfirmButton();
                                            sDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                            break;
                                    }
                                    return false;
                                }
                            });

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    DnsServerAgent.getInstance(getContext())
                                            .joinPlayRoom(SADHandler, codeInputView.getCode());
                                }
                            }).start();
                        }
                    })
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog sDialog) {
                            threadObject.setRunning(false);
                            if (codeInputView.getCode().length() == 6) {

                                final Handler SADHandler = new Handler(new Handler.Callback() {
                                    @Override
                                    public boolean handleMessage(Message msg) {
                                        Log.d(TAG, "msg= " + msg);
                                        sDialog.dismissWithAnimation();
                                        return false;
                                    }
                                });

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DnsServerAgent.getInstance(getContext())
                                                .quitPlayRoom(SADHandler, codeInputView.getCode());
                                    }
                                }).start();

                            } else {
                                sDialog.dismissWithAnimation();
                            }
                        }
                    })
                    .show();
        }
    }

    private class CreateRoomListener implements View.OnClickListener {

        String roomJoinNumber;
        Handler mySADHandler;

        @Override
        public void onClick(final View v) {
            mLog.d(TAG, "click createRoomFAB");


            final ThreadObject threadObject = new ThreadObject();
            threadObject.setRunning(false);

            // VIEW
            LayoutInflater inflater = LayoutInflater.from(getContext());
            FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.create_room_frame_layout,null);
            final DiscreteSeekBar playTimeSeekBar = frameLayout.findViewById(R.id.playTimeSettingBar);
            final TextView gameTimeTV = frameLayout.findViewById(R.id.gameTimeTV);
            gameTimeTV.setText("game period: " + playTimeSeekBar.getProgress() + " s");

            playTimeSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
                @Override
                public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                    gameTimeTV.setText("game period: " + playTimeSeekBar.getProgress() + " s");
                }

                @Override
                public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

                }
            });

            final DiscreteSeekBar difficultySeekBar = frameLayout.findViewById(R.id.difficultySeekBar);
            final TextView difficultyTV = frameLayout.findViewById(R.id.difficultyTV);
            difficultyTV.setText("game level: " + difficultySeekBar.getProgress());

            difficultySeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
                @Override
                public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                    difficultyTV.setText("game level: " + difficultySeekBar.getProgress());
                }

                @Override
                public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

                }
            });

            final Switch isAdultSwitch = frameLayout.findViewById(R.id.isAdultSwitch);
            final TextView isAdultTV = frameLayout.findViewById(R.id.isAdultTV);
            isAdultTV.setText("isAdult: " + isAdultSwitch.isChecked());

            isAdultSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isAdultTV.setText("isAdult: " + isChecked);
                }
            });

            //FButton
            final FButton playFBt = frameLayout.findViewById(R.id.playFBt);
            playFBt.setButtonColor(getResources().getColor(R.color.fbutton_color_orange));
            playFBt.setShadowColor(getResources().getColor(R.color.fbutton_color_carrot));
            playFBt.setShadowEnabled(true);
            playFBt.setShadowHeight(5);
            playFBt.setCornerRadius(5);
            playFBt.setVisibility(View.GONE);

            // Join Number
            final CodeInputView roomNumber = frameLayout.findViewById(R.id.roomNumber);

            // Player Recycler View
            recycleViewPlayerListContainer = frameLayout.findViewById(R.id.recycleViewPlayerListContainer);

            final PlayerItemAdapter playerItemAdapter = new PlayerItemAdapter(getContext(), playerItemsList);
            gridLayoutManager = new GridLayoutManager(getContext(), 5);
            recycleViewPlayerListContainer.setAdapter(playerItemAdapter);
            recycleViewPlayerListContainer.setLayoutManager(gridLayoutManager);
            recycleViewPlayerListContainer.setNestedScrollingEnabled(false);

            mySADHandler = new Handler(new Handler.Callback() {

                JSONArray jsonArray;
                List<PlayerItem> playerItemsListTemp;

                @Override
                public boolean handleMessage(Message msg) {
                        mLog.d(TAG, "msg.obj= " + msg.obj);
                    try {

                        switch (msg.arg1) {
                            case API_GET_FOLDER_ID:

                                String gameRoomFolderID = (String) msg.obj;
                                mLog.d(TAG, "gameRoomFolderID= " + gameRoomFolderID);
                                DnsServerAgent.getInstance(getContext())
                                        .getRandomPlayers(mySADHandler, roomJoinNumber);


                                break;

                            case API_GET_PLAYER_ORDERS:
                                jsonArray = (JSONArray) msg.obj;
                                mLog.d(TAG, "- participants= " + jsonArray.length());
                                playerItemsListTemp = new ArrayList<>();

                                for (int index = 0; index < jsonArray.length(); index ++) {
                                    PlayerItem item = new PlayerItem(
                                            PlayerItem.TYPE.PARTICIPANTS,
                                            (JSONObject) jsonArray.get(index)
                                    );
                                    playerItemsListTemp.add(item);
                                }

//                        mLog.d(TAG, "playerItemsList.containsAll(playerItemsListTemp)= " + playerItemsList.contains(playerItemsListTemp));;

                                if (!playerItemsList.contains(playerItemsListTemp)) {
                                    playerItemsList.clear();

                                    for (int index = 0; index < jsonArray.length(); index ++) {
                                        PlayerItem item = new PlayerItem(
                                                PlayerItem.TYPE.PARTICIPANTS,
                                                (JSONObject) jsonArray.get(index)
                                        );
                                        playerItemsList.add(item);
                                    }
                                    playerItemAdapter.clearAll();
                                    playerItemAdapter.refreshList();
                                }

                                break;
                            case API_FETCH_ROOM_INFO:
                                JSONObject responseJ = (JSONObject) ((JSONArray) msg.obj).get(0);

                                roomJoinNumber = responseJ.getString("joinNumber");
                                jsonArray = (JSONArray) responseJ.get("participants");
                                mLog.d(TAG, "Join Number= " + roomJoinNumber);

//                        mLog.d(TAG, "- participants= " + jsonArray.length());

                                if (jsonArray.length() > 1) {
                                    playFBt.setEnabled(true);
                                } else {
                                    playFBt.setEnabled(false);
                                }

                                playerItemsListTemp = new ArrayList<>();

                                for (int index = 0; index < jsonArray.length(); index ++) {
                                    PlayerItem item = new PlayerItem(
                                            PlayerItem.TYPE.PARTICIPANTS,
                                            (JSONObject) jsonArray.get(index)
                                    );
                                    playerItemsListTemp.add(item);
                                }

//                        mLog.d(TAG, "playerItemsList.containsAll(playerItemsListTemp)= " + playerItemsList.contains(playerItemsListTemp));;

                                if (!playerItemsList.contains(playerItemsListTemp)) {
                                    playerItemsList.clear();

                                    for (int index = 0; index < jsonArray.length(); index ++) {
                                        PlayerItem item = new PlayerItem(
                                                PlayerItem.TYPE.PARTICIPANTS,
                                                (JSONObject) jsonArray.get(index)
                                        );
                                        playerItemsList.add(item);
                                    }
                                    playerItemAdapter.clearAll();
                                    playerItemAdapter.refreshList();
                                }
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });

            final Thread checkRoomInfoThread = new Thread(new Runnable() {

                private static final long task_minimum_tick_time_msec = 1000; // 1 second

                @Override
                public void run() {
                    long tick_count = 0;
                    mLog.d(TAG, "task_minimum_tick_time_msec= " + (task_minimum_tick_time_msec));
                    mLog.d(TAG, "threadObject.isRunning()= " + threadObject.isRunning());

                    while (threadObject.isRunning()) {
                        try {

                            long start_time_tick = System.currentTimeMillis();
                            // real-time task

                            if (tick_count % 5 == 1) {
                                mLog.d(TAG, "* fetchPlayRoomInfo");
                                DnsServerAgent.getInstance(getContext())
                                        .fetchPlayRoomInfo(mySADHandler, roomNumber.getCode());
                            }

                            long end_time_tick = System.currentTimeMillis();

                            if (end_time_tick - start_time_tick > task_minimum_tick_time_msec) {
                                mLog.w(TAG, "Over time process " + (end_time_tick - start_time_tick));
                            } else {
                                Thread.sleep(task_minimum_tick_time_msec);
                            }
                            tick_count++;
                        } catch (InterruptedException e) {
                            mLog.d(TAG, "appRunnable interrupted");
                        }
                    }
                }
            });

            final SweetAlertDialog saDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE);

            saDialog.setTitleText("Setting and ready to Game")
                    .setCustomView(frameLayout)
                    .setConfirmText("Create")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                        @Override
                        public void onClick(final SweetAlertDialog sDialog) {
                            threadObject.setRunning(true);

                            playTimeSeekBar.setVisibility(View.GONE);
                            difficultySeekBar.setVisibility(View.GONE);
                            isAdultSwitch.setVisibility(View.GONE);
                            playFBt.setVisibility(View.VISIBLE);
                            playFBt.setEnabled(false);

                            final Handler dialogHandler = new Handler(new Handler.Callback() {
                                @Override
                                public boolean handleMessage(Message msg) {
                                    Log.d(TAG, "msg= " + msg);
                                    try {
                                        JSONObject responseJ = (JSONObject) msg.obj;

//                                            mLog.d(TAG, "responseJ= " + responseJ);
                                        mLog.d(TAG, "Join Number= " + responseJ.get("joinNumber"));
                                        roomNumber.setVisibility(View.VISIBLE);
                                        roomNumber.setCode((String) responseJ.get("joinNumber"));
                                        roomNumber.setEditable(false);

                                        JSONArray jsonArray = (JSONArray) responseJ.get("roomOwner");

                                        mLog.d(TAG, "roomOwner= " + jsonArray.get(0));

                                        playerItemsList.clear();
                                        PlayerItem item = new PlayerItem(
                                                PlayerItem.TYPE.OWNER,
                                                (JSONObject) jsonArray.get(0)
                                        );
                                        playerItemsList.add(item);

                                        playerItemAdapter.clearAll();
                                        playerItemAdapter.refreshList();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    checkRoomInfoThread.start();
                                    sDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    return false;
                                }
                            });

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    DnsServerAgent.getInstance(getContext()).createPlayRoom(dialogHandler,
                                            playTimeSeekBar.getProgress(),
                                            difficultySeekBar.getProgress(),
                                            isAdultSwitch.isChecked());
                                }
                            }).start();

                            sDialog.hideConfirmButton();
                        }

                    })
                    .show();

            saDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mLog.d(TAG, "* onDismiss");
                    threadObject.setRunning(false);
                    DnsServerAgent.getInstance(getContext()).
                            updatePlayRoomStatus(roomNumber.getCode(), 4);
                }
            });

            playFBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLog.d(TAG, "- onClick playFBt");
                    threadObject.setRunning(false);

                    DnsServerAgent.getInstance(getContext())
                            .getGameChainFolderID(mySADHandler, roomJoinNumber);

//                    mHomeFragmentListener.onStartToPlayGame();
//                    saDialog.dismissWithAnimation();
                }
            });

        }

    }

}
