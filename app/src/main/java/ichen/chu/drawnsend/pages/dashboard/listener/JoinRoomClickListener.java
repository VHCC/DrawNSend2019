package ichen.chu.drawnsend.pages.dashboard.listener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.florent37.viewtooltip.ViewTooltip;
import com.github.glomadrian.codeinputlib.CodeInput;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.raycoarana.codeinputview.CodeInputView;
import com.raycoarana.codeinputview.OnCodeCompleteListener;

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

import static ichen.chu.drawnsend.Bus.EVENT_DASHBOARD_START_TO_PLAY_GAME;
import static ichen.chu.drawnsend.Bus.EVENT_MAP;
import static ichen.chu.drawnsend.api.APICode.API_CREATE_GAME_CHAIN;
import static ichen.chu.drawnsend.api.APICode.API_FETCH_ROOM_INFO;
import static ichen.chu.drawnsend.api.APICode.API_GET_FOLDER_ID;
import static ichen.chu.drawnsend.api.APICode.API_GET_GAME_SUBJECT;
import static ichen.chu.drawnsend.model.DnsPlayRoom.CLOSED;
import static ichen.chu.drawnsend.model.DnsPlayRoom.PLAYING;
import static ichen.chu.drawnsend.model.DnsPlayRoom.READY_TO_PLAY;
import static ichen.chu.drawnsend.model.DnsPlayRoom.SETTING;

public class JoinRoomClickListener implements View.OnClickListener {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private Context mContext;

    public JoinRoomClickListener(Context mContext) {
        this.mContext = mContext;
    }

    private Handler mySADHandler;

    // RecycleView
    private RecyclerView recycleViewPlayerListContainer;
    private GridLayoutManager gridLayoutManager;

    /*data Block*/

    // Font Family
    Typeface mCustomFont = null;

    /**
     * storage the result of event search.
     */
    private final List<PlayerItem> playerItemsList = new ArrayList<>();

    // View
    private View codeInputView;

    @Override
    public void onClick(View v) {
        mLog.d(TAG, "click joinRoomFAB");

        mCustomFont = Typeface.createFromAsset(mContext.getAssets(), "Pacifico-Regular.ttf");

        final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

        final ThreadObject threadObject = new ThreadObject();
        threadObject.setRunning(false);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.join_room_frame_layout,null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            codeInputView = (CodeInputView) frameLayout.findViewById(R.id.roomCodeInput);
            codeInputView.setVisibility(View.VISIBLE);
        } else {
            codeInputView = (CodeInput) frameLayout.findViewById(R.id.roomCodeInput_old);
            codeInputView.setVisibility(View.VISIBLE);
        }

        final TextView joinTV = frameLayout.findViewById(R.id.joinTV);

        // DnsPlayer Recycler View
        recycleViewPlayerListContainer = frameLayout.findViewById(R.id.recycleViewPlayerListContainer);

        final PlayerItemAdapter playerItemAdapter = new PlayerItemAdapter(mContext, playerItemsList);
        gridLayoutManager = new GridLayoutManager(mContext, 5);
        recycleViewPlayerListContainer.setAdapter(playerItemAdapter);
        recycleViewPlayerListContainer.setLayoutManager(gridLayoutManager);
        recycleViewPlayerListContainer.setNestedScrollingEnabled(false);

        final SweetAlertDialog saDialog = new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE);

        mySADHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                try {
                    switch (msg.arg1) {
                        case API_GET_FOLDER_ID:

                            String gameRoomFolderID = (String) msg.obj;

                            DnsResult.getInstance().setFolderID(gameRoomFolderID);

                            mLog.d(TAG, "- gameRoomFolderID= " + DnsResult.getInstance().getFolderID());

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
                        case API_FETCH_ROOM_INFO:

                            mLog.d(TAG, "msg.obj= " + msg.obj);
                            JSONObject responseJ = (JSONObject) ((JSONArray) msg.obj).get(0);
                            DnsPlayRoom.getInstance().setRoomInfo(responseJ);

                            mLog.d(TAG, "room status= " +
                                    DnsPlayRoom.getInstance().getRoomStatus());
                            switch (DnsPlayRoom.getInstance().getRoomStatus()) {
                                case SETTING:
                                    joinTV.setText("wait to room creator start game.");
                                    mLog.d(TAG, "- participants= " +
                                            DnsPlayRoom.getInstance().getParticipants().length());

                                    List<PlayerItem> playerItemsListTemp = new ArrayList<>();

                                    for (int index = 0; index < DnsPlayRoom.getInstance()
                                            .getParticipants().length(); index ++) {
                                        PlayerItem item = new PlayerItem(
                                                PlayerItem.TYPE.PARTICIPANTS,
                                                (JSONObject) DnsPlayRoom.getInstance()
                                                        .getParticipants().get(index)
                                        );
                                        playerItemsListTemp.add(item);
                                    }

//                        mLog.d(TAG, "playerItemsList.containsAll(playerItemsListTemp)= " + playerItemsList.contains(playerItemsListTemp));

                                    if (!playerItemsList.contains(playerItemsListTemp)) {
                                        playerItemsList.clear();

                                        for (int index = 0; index < DnsPlayRoom.getInstance()
                                                .getParticipants().length(); index ++) {
                                            PlayerItem item = new PlayerItem(
                                                    PlayerItem.TYPE.PARTICIPANTS,
                                                    (JSONObject) DnsPlayRoom.getInstance()
                                                            .getParticipants().get(index)
                                            );
                                            playerItemsList.add(item);
                                        }

                                        playerItemAdapter.clearAll();
                                        playerItemAdapter.refreshList();
                                    }

                                    break;
                                case READY_TO_PLAY:
                                    saDialog.showCancelButton(false);
                                    saDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                    joinTV.setText("Ready to Game...");
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

                                    DnsServerAgent.getInstance(mContext)
                                            .getGameChainFolderID(mySADHandler,
                                                    DnsPlayRoom.getInstance().getRoomOwner().getString("email") +
                                                    DnsPlayRoom.getInstance().getJoinNumber());

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        DnsServerAgent.getInstance(mContext)
                                                .readyPlayRoom(mySADHandler, ((CodeInputView)codeInputView).getCode());
                                    } else {

                                        try {
                                            StringBuilder builder = new StringBuilder();
                                            for(char s : ((CodeInput)codeInputView).getCode()) {
                                                builder.append(s);
                                            }
                                            DnsServerAgent.getInstance(mContext)
                                                    .readyPlayRoom(mySADHandler, builder.toString());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            DnsServerAgent.getInstance(mContext)
                                                    .readyPlayRoom(mySADHandler, "");
                                        }
                                    }

                                    break;
                                case PLAYING:
                                    threadObject.setRunning(false);
                                    joinTV.setText("遊戲中");
                                    DnsServerAgent.getInstance(mContext)
                                            .getSubject(mySADHandler,
                                                    DnsPlayRoom.getInstance().getDifficulty(),
                                                    DnsPlayRoom.getInstance().isAdult());

                                    break;
                                case CLOSED:
                                    joinTV.setText("房間已經關閉");
                                    threadObject.setRunning(false);
                                    saDialog.setCancelable(true);
                                    break;
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
//                mLog.d(TAG, "task_minimum_tick_time_msec= " + (task_minimum_tick_time_msec));

                while (threadObject.isRunning()) {
                    try {

                        long start_time_tick = System.currentTimeMillis();
                        // real-time task

                        if (tick_count % 5 == 1) {
                            mLog.d(TAG, "api * fetchPlayRoomInfo (5s)");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                DnsServerAgent.getInstance(mContext)
                                        .fetchPlayRoomInfo(mySADHandler, ((CodeInputView)codeInputView).getCode());
                            } else {
                                StringBuilder builder = new StringBuilder();
                                for(char s : ((CodeInput)codeInputView).getCode()) {
                                    builder.append(s);
                                }
                                DnsServerAgent.getInstance(mContext)
                                        .fetchPlayRoomInfo(mySADHandler, builder.toString());
                            }


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

        saDialog.setCancelable(false);

        saDialog.setTitleText("Ready to play")
                .setConfirmText("Join")
                .setCancelText("Quit")
                .setCustomView(frameLayout)
//                        .hideConfirmButton()
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(final SweetAlertDialog sDialog) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mLog.d(TAG, "code= " + ((CodeInputView)codeInputView).getCode());
                        } else {

                            try {
                                StringBuilder builder = new StringBuilder();
                                for(char s : ((CodeInput)codeInputView).getCode()) {
                                    builder.append(s);
                                }
                                String str = builder.toString();
                                mLog.d(TAG, "code= " + str);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
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

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    DnsServerAgent.getInstance(mContext)
                                            .joinPlayRoom(SADHandler, ((CodeInputView)codeInputView).getCode());
                                } else {

                                    try {
                                        StringBuilder builder = new StringBuilder();
                                        for(char s : ((CodeInput)codeInputView).getCode()) {
                                            builder.append(s);
                                        }
                                        DnsServerAgent.getInstance(mContext)
                                                .joinPlayRoom(SADHandler, builder.toString());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        DnsServerAgent.getInstance(mContext)
                                                .joinPlayRoom(SADHandler, "");
                                    }
                                }

//                                DnsServerAgent.getInstance(mContext)
//                                        .joinPlayRoom(SADHandler, String.valueOf(codeInputView.getCode()));
                            }
                        }).start();
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(final SweetAlertDialog sDialog) {
                        threadObject.setRunning(false);
                        String inputCode = "";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            inputCode = ((CodeInputView)codeInputView).getCode();
                        } else {
                            try {
                                StringBuilder builder = new StringBuilder();
                                for(char s : ((CodeInput)codeInputView).getCode()) {
                                    builder.append(s);
                                }
                                inputCode = builder.toString();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        if (inputCode.length() == 6) {

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

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        DnsServerAgent.getInstance(mContext)
                                                .quitPlayRoom(SADHandler, ((CodeInputView)codeInputView).getCode());
                                    } else {
                                        StringBuilder builder = new StringBuilder();
                                        for(char s : ((CodeInput)codeInputView).getCode()) {
                                            builder.append(s);
                                        }
                                        DnsServerAgent.getInstance(mContext)
                                                .quitPlayRoom(SADHandler, builder.toString());
                                    }

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
