package ichen.chu.drawnsend.pages.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.util.MLog;

/**
 * Created by IChen.Chu on 2018/9/25
 * A fragment to show home page.
 */
public class DashboardMainFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // View
    private Button googleSignOutBtn;

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
}
