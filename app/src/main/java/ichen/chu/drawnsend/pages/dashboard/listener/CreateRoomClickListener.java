package ichen.chu.drawnsend.pages.dashboard.listener;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.raycoarana.codeinputview.CodeInputView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ichen.chu.drawnsend.Bus;
import ichen.chu.drawnsend.BusEvent;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.api.DnsServerAgent;
import ichen.chu.drawnsend.model.DnsPlayRoom;
import ichen.chu.drawnsend.model.DnsPlayer;
import ichen.chu.drawnsend.model.DnsResult;
import ichen.chu.drawnsend.model.PlayerItem;
import ichen.chu.drawnsend.pages.dashboard.ListAdapter.PlayerItemAdapter;
import ichen.chu.drawnsend.pages.dashboard.ThreadObject;
import ichen.chu.drawnsend.util.MLog;
import info.hoang8f.widget.FButton;

import static ichen.chu.drawnsend.Bus.EVENT_DASHBOARD_START_TO_PLAY_GAME;
import static ichen.chu.drawnsend.Bus.EVENT_LOGIN_SUCCESS;
import static ichen.chu.drawnsend.Bus.EVENT_MAP;
import static ichen.chu.drawnsend.api.APICode.API_CREATE_GAME_CHAIN;
import static ichen.chu.drawnsend.api.APICode.API_FETCH_ROOM_INFO;
import static ichen.chu.drawnsend.api.APICode.API_GET_FOLDER_ID;
import static ichen.chu.drawnsend.api.APICode.API_GET_GAME_SUBJECT;
import static ichen.chu.drawnsend.api.APICode.API_GET_PLAYER_ORDERS;
import static ichen.chu.drawnsend.api.APICode.API_UPDATE_ROOM_STATUS;
import static ichen.chu.drawnsend.model.DnsPlayRoom.CLOSED;
import static ichen.chu.drawnsend.model.DnsPlayRoom.PLAYING;
import static ichen.chu.drawnsend.model.DnsPlayRoom.READY_TO_PLAY;

public class CreateRoomClickListener implements View.OnClickListener {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private Context mContext;

    public CreateRoomClickListener(Context context) {
        this.mContext = context;
    }

    private Handler mySADHandler;

    // RecycleView
    private RecyclerView recycleViewPlayerListContainer;
    private GridLayoutManager gridLayoutManager;

    /*data Block*/


    /**
     * storage the result of event search.
     */
    private final List<PlayerItem> playerItemsList = new ArrayList<>();


    @Override
    public void onClick(final View v) {
        mLog.d(TAG, "click createRoomFAB");

        final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

        final ThreadObject threadObject = new ThreadObject();
        threadObject.setRunning(false);

        // VIEW
        LayoutInflater inflater = LayoutInflater.from(mContext);
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
        playFBt.setButtonColor(mContext.getResources().getColor(R.color.fbutton_color_orange));
        playFBt.setShadowColor(mContext.getResources().getColor(R.color.fbutton_color_carrot));
        playFBt.setShadowEnabled(true);
        playFBt.setShadowHeight(5);
        playFBt.setCornerRadius(5);
        playFBt.setVisibility(View.GONE);

        final FButton readyFBt = frameLayout.findViewById(R.id.readyFBt);
        readyFBt.setButtonColor(mContext.getResources().getColor(R.color.fbutton_default_color));
        readyFBt.setShadowColor(mContext.getResources().getColor(R.color.fbutton_default_shadow_color));
        readyFBt.setShadowEnabled(true);
        readyFBt.setShadowHeight(5);
        readyFBt.setCornerRadius(5);
        readyFBt.setVisibility(View.GONE);

        // Join Number
        final CodeInputView roomNumber = frameLayout.findViewById(R.id.roomNumber);

        // DnsPlayer Recycler View
        recycleViewPlayerListContainer = frameLayout.findViewById(R.id.recycleViewPlayerListContainer);

        final PlayerItemAdapter playerItemAdapter = new PlayerItemAdapter(mContext, playerItemsList);
        gridLayoutManager = new GridLayoutManager(mContext, 5);
        recycleViewPlayerListContainer.setAdapter(playerItemAdapter);
        recycleViewPlayerListContainer.setLayoutManager(gridLayoutManager);
        recycleViewPlayerListContainer.setNestedScrollingEnabled(false);

        final SweetAlertDialog saDialog = new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE);

        mySADHandler = new Handler(new Handler.Callback() {

            List<PlayerItem> playerItemsListTemp;

            @Override
            public boolean handleMessage(Message msg) {
//                mLog.d(TAG, "msg.obj= " + msg.obj);
                try {
//                    mLog.d(TAG, "msg.arg1= " + msg.arg1);
                    switch (msg.arg1) {
                        case API_UPDATE_ROOM_STATUS:
                            int roomStatus = (int) msg.obj;
                            switch (roomStatus) {
                                case READY_TO_PLAY:
                                    break;
                                case PLAYING:
                                    DnsServerAgent.getInstance(mContext)
                                            .getSubject(mySADHandler,
                                                    DnsPlayRoom.getInstance().getDifficulty(),
                                                    DnsPlayRoom.getInstance().isAdult());
                                    break;
                            }
                            break;
                        case API_GET_FOLDER_ID:
                            readyFBt.setText("Re-Orders");
                            playFBt.setEnabled(true);
                            playFBt.setVisibility(View.VISIBLE);
                            String gameRoomFolderID = (String) msg.obj;

                            DnsResult.getInstance().setFolderID(gameRoomFolderID);

                            mLog.d(TAG, "- gameRoomFolderID= " + DnsResult.getInstance().getFolderID());
                            DnsServerAgent.getInstance(mContext)
                                    .getRandomPlayers(mySADHandler, DnsPlayRoom.getInstance().getJoinNumber());

                            DnsServerAgent.getInstance(mContext).
                                    updatePlayRoomStatus(mySADHandler,
                                            roomNumber.getCode(), READY_TO_PLAY);

                            break;

                        case API_GET_PLAYER_ORDERS:

                            DnsPlayRoom.getInstance().setRoomInfo((JSONObject) msg.obj);

                            mLog.d(TAG, "- game players= " + DnsPlayRoom.getInstance().getPlayOrders().length());

                            List<String> emailLst = new ArrayList<String>();

                            for (int index = 0; index < DnsPlayRoom.getInstance().getPlayOrders().length(); index ++) {
                                emailLst.add(((JSONObject) DnsPlayRoom.getInstance().getPlayOrders().get(index)).getString("email"));
                            }

                            mLog.d(TAG, "- play Index= " +
                                    emailLst.indexOf(acct.getEmail()));

                            JSONArray playOrdersArrayA = new JSONArray();
                            JSONArray playOrdersArrayB = new JSONArray();

                            for (int index = 0; index < DnsPlayRoom.getInstance().getPlayOrders().length(); index ++) {
                                if (index >= emailLst.indexOf(acct.getEmail())) {
                                    playOrdersArrayA.put(DnsPlayRoom.getInstance().getPlayOrders().get(index));
                                } else {
                                    playOrdersArrayB.put(DnsPlayRoom.getInstance().getPlayOrders().get(index));
                                }
                            }

                            for (int index = 0; index < playOrdersArrayB.length(); index ++) {
                                playOrdersArrayA.put(playOrdersArrayB.get(index));
                            }

//                            mLog.d(TAG, playOrdersArrayA.toString());

                            DnsPlayer.getInstance().setOrders(playOrdersArrayA);

                            playerItemsList.clear();
                            for (int index = 0; index < DnsPlayer.getInstance().getPlayerOrders().length(); index ++) {
                                PlayerItem item = new PlayerItem(
                                        PlayerItem.TYPE.PARTICIPANTS,
                                        (JSONObject) DnsPlayer.getInstance().getPlayerOrders().get(index)
                                );
                                playerItemsList.add(item);
                            }

                            playerItemAdapter.clearAll();
                            playerItemAdapter.refreshList();

                            break;
                            // polling
                        case API_FETCH_ROOM_INFO:
                            JSONObject responseJ = (JSONObject) ((JSONArray) msg.obj).get(0);
                            DnsPlayRoom.getInstance().setRoomInfo(responseJ);

                            mLog.d(TAG, "- Join Number= " + DnsPlayRoom.getInstance().getJoinNumber());

//                        mLog.d(TAG, "- participants= " + jsonArray.length());

                            readyFBt.setText("Ready");
                            readyFBt.setVisibility(View.VISIBLE);
                            if (DnsPlayRoom.getInstance().getParticipants().length() > 1) {
                                readyFBt.setEnabled(true);
                            } else {
                                readyFBt.setEnabled(false);
                            }

                            playerItemsListTemp = new ArrayList<>();

                            for (int index = 0; index < DnsPlayRoom.getInstance().getParticipants().length(); index ++) {
                                PlayerItem item = new PlayerItem(
                                        PlayerItem.TYPE.PARTICIPANTS,
                                        (JSONObject) DnsPlayRoom.getInstance().getParticipants().get(index)
                                );
                                playerItemsListTemp.add(item);
                            }

//                        mLog.d(TAG, "playerItemsList.containsAll(playerItemsListTemp)= " + playerItemsList.contains(playerItemsListTemp));;

                            if (!playerItemsList.contains(playerItemsListTemp)) {
                                playerItemsList.clear();

                                for (int index = 0; index < DnsPlayRoom.getInstance().getParticipants().length(); index ++) {
                                    PlayerItem item = new PlayerItem(
                                            PlayerItem.TYPE.PARTICIPANTS,
                                            (JSONObject) DnsPlayRoom.getInstance().getParticipants().get(index)
                                    );
                                    playerItemsList.add(item);
                                }
                                playerItemAdapter.clearAll();
                                playerItemAdapter.refreshList();
                            }
                            break;
                        case API_GET_GAME_SUBJECT:
                            mLog.d(TAG, "msg.obj= " + msg.obj);
                            String gameSubject = ((JSONObject)((JSONArray) msg.obj).get(0)).getString("content");
                            DnsResult.getInstance().setSubject(gameSubject);

                            DnsServerAgent.getInstance(mContext)
                                    .createGameChain(mySADHandler,
                                            DnsPlayRoom.getInstance().getJoinNumber(),
                                            DnsResult.getInstance().getFolderID(),
                                            DnsResult.getInstance().getSubject(),
                                            DnsPlayer.getInstance().getPlayerOrders());

                            break;
                        case API_CREATE_GAME_CHAIN:
                            mLog.d(TAG, "msg.obj= " + msg.obj);
                            Bus.getInstance().post(new BusEvent(EVENT_MAP.get(EVENT_DASHBOARD_START_TO_PLAY_GAME), EVENT_DASHBOARD_START_TO_PLAY_GAME));
                            saDialog.dismissWithAnimation();
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
//                mLog.d(TAG, "task_minimum_tick_time_msec= " + (task_minimum_tick_time_msec));

                while (threadObject.isRunning()) {
                    try {

                        long start_time_tick = System.currentTimeMillis();
                        // real-time task

                        if (tick_count % 5 == 1) {
                            mLog.d(TAG, "api * fetchPlayRoomInfo (5s)");
                            DnsServerAgent.getInstance(mContext)
                                    .fetchPlayRoomInfo(mySADHandler, roomNumber.getCode());
                        }

                        long end_time_tick = System.currentTimeMillis();

                        if (end_time_tick - start_time_tick > task_minimum_tick_time_msec) {
                            mLog.w(TAG, " == Over time process " + (end_time_tick - start_time_tick) + " == ");
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

                        final Handler dialogHandler = new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message msg) {
//                                mLog.d(TAG, "msg= " + msg);
                                JSONObject responseJ = (JSONObject) msg.obj;

                                DnsPlayRoom.getInstance().setRoomInfo(responseJ);

                                mLog.d(TAG, DnsPlayRoom.getInstance().toString());
                                mLog.d(TAG, "- Join Number= " + DnsPlayRoom.getInstance().getJoinNumber());
                                roomNumber.setVisibility(View.VISIBLE);
                                roomNumber.setCode(DnsPlayRoom.getInstance().getJoinNumber());
                                roomNumber.setEditable(false);

                                mLog.d(TAG, "- roomOwner= " + DnsPlayRoom.getInstance().getRoomOwner());

                                playerItemsList.clear();
                                PlayerItem item = new PlayerItem(
                                        PlayerItem.TYPE.OWNER,
                                        DnsPlayRoom.getInstance().getRoomOwner()
                                );
                                playerItemsList.add(item);

                                playerItemAdapter.clearAll();
                                playerItemAdapter.refreshList();
                                checkRoomInfoThread.start();
                                sDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                return false;
                            }
                        });

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DnsServerAgent.getInstance(mContext).createPlayRoom(dialogHandler,
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
                DnsServerAgent.getInstance(mContext).
                        updatePlayRoomStatus(mySADHandler,
                                roomNumber.getCode(), CLOSED);
            }
        });

        // stop join room
        readyFBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLog.d(TAG, "- onClick readyFBt");
                threadObject.setRunning(false);

                DnsServerAgent.getInstance(mContext)
                        .getGameChainFolderID(mySADHandler,
                                DnsPlayRoom.getInstance().getJoinNumber());

            }
        });

        playFBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DnsServerAgent.getInstance(mContext).
                        updatePlayRoomStatus(mySADHandler,
                                roomNumber.getCode(), PLAYING);
            }
        });

    }
}
