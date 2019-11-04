package ichen.chu.drawnsend.pages.dashboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import ichen.chu.drawnsend.Bus;
import ichen.chu.drawnsend.BusEvent;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.model.PlayerItem;
import ichen.chu.drawnsend.pages.dashboard.ListAdapter.PlayerItemAdapter;
import ichen.chu.drawnsend.util.MLog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

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
        joinRoomFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLog.d(TAG, "click joinRoomFAB");

                LayoutInflater inflater = LayoutInflater.from(getContext());
                FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.join_room_frame_layout,null);
                final CodeInputView codeInputView = frameLayout.findViewById(R.id.roomCodeInput);

                codeInputView.addOnCompleteListener(new OnCodeCompleteListener() {
                    @Override
                    public void onCompleted(String code) {
                        mLog.d(TAG, "code= " + code);
                    }
                });


                SweetAlertDialog sad = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE);

                sad.setCancelable(false);

                sad.setTitleText("Join a Room")
                    .setConfirmText("Join")
                    .setCancelText("Quit")
                    .setCustomView(frameLayout)
//                        .hideConfirmButton()
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog sDialog) {
                            mLog.d(TAG, "code= " + codeInputView.getCode());

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
                                    joinPlayRoom(SADHandler,
                                            codeInputView.getCode());
                                }
                            }).start();
                        }
                    })
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog sDialog) {
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
                                        quitPlayRoom(SADHandler, codeInputView.getCode());
                                    }
                                }).start();

                            } else {
                                sDialog.dismissWithAnimation();
                            }
                        }
                    })
                    .show();
            }
        });


        // ******************** CREATE *************************
        createRoomFAB.setIcon(R.drawable.create_room);
        createRoomFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

                // Join Number
                final CodeInputView roomNumber = frameLayout.findViewById(R.id.roomNumber);


                // Player Recycler View
                recycleViewPlayerListContainer = frameLayout.findViewById(R.id.recycleViewPlayerListContainer);

                final PlayerItemAdapter playerItemAdapter = new PlayerItemAdapter(getContext(), playerItemsList);
                recycleViewPlayerListContainer.setAdapter(playerItemAdapter);
                recycleViewPlayerListContainer.setLayoutManager(linearLayoutManager);
                recycleViewPlayerListContainer.setNestedScrollingEnabled(false);

                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Setting and ready to Game")
                        .setCustomView(frameLayout)
                        .setConfirmText("Create")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                            @Override
                            public void onClick(final SweetAlertDialog sDialog) {
                                mLog.d(TAG, "game time= " + playTimeSeekBar.getProgress());

                                playTimeSeekBar.setVisibility(View.GONE);
                                difficultySeekBar.setVisibility(View.GONE);
                                isAdultSwitch.setVisibility(View.GONE);


                                final Handler SADHandler = new Handler(new Handler.Callback() {
                                    @Override
                                    public boolean handleMessage(Message msg) {
                                        Log.d(TAG, "msg= " + msg);
                                        try {
                                            JSONObject responseJ = (JSONObject) msg.obj;

                                            mLog.d(TAG, "responseJ= " + responseJ);
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
                                            playerItemsList.add(item);
                                            playerItemsList.add(item);

                                            playerItemAdapter.clearAll();
                                            playerItemAdapter.refreshList();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        sDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                        return false;
                                    }
                                });

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        createPlayRoom(SADHandler,
                                                playTimeSeekBar.getProgress(),
                                                difficultySeekBar.getProgress(),
                                                isAdultSwitch.isChecked());
                                    }
                                }).start();

                                sDialog.hideConfirmButton();
                            }

                            public void onEventMainThread(BusEvent event){
                                //        event.getMessage();
                                mLog.d(TAG, "* createRoomFAB, event= " + event.getMessage());
                            }
                        })
                        .show();


            }
        });
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
    }

    public void setDashboardMainFragmentListener(OnDashboardMainFragmentInteractionListener listener) {
        mHomeFragmentListener = listener;
    }

    // -------------------------------------------
    private void joinPlayRoom(final Handler SADHandler,
                                final String roomNumberCode) {
        try {
            try {

                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());

                JSONObject jsonObj = new JSONObject();
                jsonObj.put("player", "qQQQQ");
                jsonObj.put("joinNumber", roomNumberCode);

                RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

                Request request = new Request.Builder()
                        .url("http://172.22.212.158:4009/api/post_dns_join_game_room")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseJ = new JSONObject(response.body().string());
                                mLog.d(TAG, "response= " + responseJ);
                                mLog.d(TAG, "payload= " + responseJ.get("payload"));
                                JSONObject responsePayload = new JSONObject(String.valueOf(responseJ.get("payload")));
                                mLog.d(TAG, "nModified= " + responsePayload.get("nModified"));

                                int modifiedStatus = (int) responsePayload.get("nModified");
                                Message msg;
                                switch(modifiedStatus) {
                                    case 0:
                                        msg = new Message();
                                        msg.what = 0;
                                        SADHandler.sendMessage(msg);
                                        break;
                                    case 1:
                                        msg = new Message();
                                        msg.what = 1;
                                        SADHandler.sendMessage(msg);
                                        break;
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });


//            } catch (UnknownHostException | UnsupportedEncodingException e) {
//                mLog.e(TAG, "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void quitPlayRoom(final Handler SADHandler,
                              final String roomNumberCode) {
        try {
            try {

                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                JSONObject jsonObj = new JSONObject();
                jsonObj.put("player", "qQQQQ");
                jsonObj.put("joinNumber", roomNumberCode);

                RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

                Request request = new Request.Builder()
                        .url("http://172.22.212.158:4009/api/post_dns_quit_game_room")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                        if (response.isSuccessful()) {
                            mLog.d(TAG, "response= " + response.body().string());
                            SADHandler.sendMessage(new Message());
                        }
                    }
                });


//            } catch (UnknownHostException | UnsupportedEncodingException e) {
//                mLog.e(TAG, "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void createPlayRoom(final Handler SADHandler,
                                 final int playTime,
                                 final int difficulty,
                                 final boolean isAdult
                                ) {
        try {
            try {

                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());

                JSONObject userObj = new JSONObject();
                userObj.put("email", acct.getEmail());
                userObj.put("displayName", acct.getDisplayName());
                userObj.put("photoUrl", acct.getPhotoUrl());


                JSONObject jsonObj = new JSONObject();
                jsonObj.put("roomOwner", userObj);
                jsonObj.put("playTime", playTime);
                jsonObj.put("difficulty", difficulty);
                jsonObj.put("isAdult", isAdult);

                RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

                Request request = new Request.Builder()
                        .url("http://172.22.212.158:4009/api/post_dns_create_game_room")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject responseJ = new JSONObject(response.body().string());
                                mLog.d(TAG, "response= " + responseJ);
                                mLog.d(TAG, "payload= " + responseJ.get("payload"));
                                Message msg = new Message();
                                msg.obj = responseJ.get("payload");
                                SADHandler.sendMessage(msg);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });


//            } catch (UnknownHostException | UnsupportedEncodingException e) {
//                mLog.e(TAG, "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public void onEventMainThread(BusEvent event){
//        event.getMessage();
        mLog.d(TAG, "event= " + event.getMessage());

        switch (event.getEventType()) {
            case 1001:
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
                new DownloadImageTask(profile_image).execute(acct.getPhotoUrl().toString());
                accountEmailTV.setText(acct.getEmail());
                break;
        }
    }
}
