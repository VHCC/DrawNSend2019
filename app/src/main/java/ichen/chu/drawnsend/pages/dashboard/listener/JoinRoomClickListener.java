package ichen.chu.drawnsend.pages.dashboard.listener;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.raycoarana.codeinputview.CodeInputView;
import com.raycoarana.codeinputview.OnCodeCompleteListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.api.DnsServerAgent;
import ichen.chu.drawnsend.model.PlayerItem;
import ichen.chu.drawnsend.pages.dashboard.ListAdapter.PlayerItemAdapter;
import ichen.chu.drawnsend.pages.dashboard.ThreadObject;
import ichen.chu.drawnsend.util.MLog;

public class JoinRoomClickListener implements View.OnClickListener {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private Context mContext;

    public JoinRoomClickListener(Context mContext) {
        this.mContext = mContext;
    }

    // RecycleView
    private RecyclerView recycleViewPlayerListContainer;
    private GridLayoutManager gridLayoutManager;

    /*data Block*/

    /**
     * storage the result of event search.
     */
    private final List<PlayerItem> playerItemsList = new ArrayList<>();

    @Override
    public void onClick(View v) {
        mLog.d(TAG, "click joinRoomFAB");

        final ThreadObject threadObject = new ThreadObject();
        threadObject.setRunning(false);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.join_room_frame_layout,null);
        final CodeInputView codeInputView = frameLayout.findViewById(R.id.roomCodeInput);

        codeInputView.addOnCompleteListener(new OnCodeCompleteListener() {
            @Override
            public void onCompleted(String code) {
                mLog.d(TAG, "code= " + code);
            }
        });

        final TextView joinTV = frameLayout.findViewById(R.id.joinTV);

        // DnsPlayer Recycler View
        recycleViewPlayerListContainer = frameLayout.findViewById(R.id.recycleViewPlayerListContainer);

        final PlayerItemAdapter playerItemAdapter = new PlayerItemAdapter(mContext, playerItemsList);
        gridLayoutManager = new GridLayoutManager(mContext, 5);
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
                            DnsServerAgent.getInstance(mContext)
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

        SweetAlertDialog saDialog = new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE);

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
                                DnsServerAgent.getInstance(mContext)
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
                                    DnsServerAgent.getInstance(mContext)
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
