package ichen.chu.drawnsend.pages.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ichen.chu.drawnsend.R;
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
    private FloatingActionButton signOutFAB;
    private FloatingActionButton joinRoomFAB;
    private FloatingActionButton createRoomFAB;

    // Constants

    // Handler

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

        initGoogleAPI();
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

        createRoomFAB.setIcon(R.drawable.create_room);
        createRoomFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(getContext());
                FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.create_room_frame_layout,null);
                final DiscreteSeekBar discreteSeekBar = frameLayout.findViewById(R.id.playTimeSettingBar);
                final TextView gameTimeTV = frameLayout.findViewById(R.id.gameTimeTV);
                gameTimeTV.setText("game period: " + discreteSeekBar.getProgress() + " s");
                discreteSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
                    @Override
                    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                        gameTimeTV.setText("game period: " + discreteSeekBar.getProgress() + " s");
                    }

                    @Override
                    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

                    }
                });



                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Setting and ready to Game")
                        .setCustomView(frameLayout)
                        .setConfirmText("Create")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                            @Override
                            public void onClick(final SweetAlertDialog sDialog) {
                                mLog.d(TAG, "game time= " + discreteSeekBar.getProgress());

                                discreteSeekBar.setVisibility(View.INVISIBLE);

                                final Handler SADHandler = new Handler(new Handler.Callback() {
                                    @Override
                                    public boolean handleMessage(Message msg) {
                                        Log.d(TAG, "msg= " + msg);
                                        sDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                        return false;
                                    }
                                });

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        createPlayRoom(SADHandler,
                                                discreteSeekBar.getProgress());
                                    }
                                }).start();

                                sDialog.hideConfirmButton();
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
                        .url("http://172.22.212.168:4009/api/post_dns_join_game_room")
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
                        .url("http://172.22.212.168:4009/api/post_dns_quit_game_room")
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
                                 final int playTime) {
        try {
            try {

                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());

                JSONObject jsonObj = new JSONObject();
                jsonObj.put("roomOwner", acct.getEmail());
                jsonObj.put("playTime", playTime);

                RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

                Request request = new Request.Builder()
                        .url("http://172.22.212.168:4009/api/post_dns_create_game_room")
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
}
