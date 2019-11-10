package ichen.chu.drawnsend.pages.dashboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.carbs.android.avatarimageview.library.AvatarImageView;
import de.hdodenhof.circleimageview.CircleImageView;
import ichen.chu.drawnsend.Bus;
import ichen.chu.drawnsend.BusEvent;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.pages.dashboard.listener.CreateRoomClickListener;
import ichen.chu.drawnsend.pages.dashboard.listener.JoinRoomClickListener;
import ichen.chu.drawnsend.util.MLog;

import static ichen.chu.drawnsend.Bus.EVENT_DASHBOARD_START_TO_PLAY_GAME;
import static ichen.chu.drawnsend.Bus.EVENT_LOGIN_SUCCESS;

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
    private AvatarImageView item_avatar;
    private TextView accountEmailTV;
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
        item_avatar = rootView.findViewById(R.id.item_avatar);
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
        joinRoomFAB.setOnClickListener(new JoinRoomClickListener(getContext()));

        // ******************** CREATE *************************
        createRoomFAB.setIcon(R.drawable.create_room);
        createRoomFAB.setOnClickListener(new CreateRoomClickListener(getContext()));
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

                if (null == acct.getPhotoUrl()) {
                    item_avatar.setTextAndColorSeed(String.valueOf(acct.getDisplayName().charAt(0)), acct.getDisplayName().toString());
                } else {
                    new DownloadImageTask(item_avatar).execute(acct.getPhotoUrl().toString());
                }

                accountEmailTV.setText(acct.getEmail());
                break;
            case EVENT_DASHBOARD_START_TO_PLAY_GAME:
                mHomeFragmentListener.onStartToPlayGame();
                break;
        }
    }


}
