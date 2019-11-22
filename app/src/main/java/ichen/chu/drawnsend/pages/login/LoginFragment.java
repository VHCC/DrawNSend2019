package ichen.chu.drawnsend.pages.login;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.nio.file.Paths;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.util.MLog;

import static ichen.chu.drawnsend.MainActivity.CUSTOM_FONT;

/**
 * Created by IChen.Chu on 2018/9/25
 * A fragment to show home page.
 */
public class LoginFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants
    private int RC_SIGN_IN = 1001;

    // View
    private TextView appVersion;
    private TextView authTv;
    private SignInButton signInButton;
    private Button googleSignOutBtn;

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;

    // Fields
    private GoogleSignInClient mGoogleSignInClient;

    public LoginFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of HomeFragment.
     */
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        appVersion = rootView.findViewById(R.id.appVersion);
        authTv = rootView.findViewById(R.id.authTv);
        googleSignOutBtn = rootView.findViewById(R.id.googleSignOutBtn);

        // Set the dimensions of the sign-in button.
        signInButton = rootView.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
    }


    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());
        appVersion.setTypeface(CUSTOM_FONT);
        authTv.setTypeface(CUSTOM_FONT);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }

            private void signIn() {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

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
                            }
                        });
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLog.d(TAG, "onActivityResult, requestCode= " + requestCode);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        mLog.d(TAG, "handleSignInResult");
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);



            // Signed in successfully, show authenticated UI.
            mLog.d(TAG, "* Signed in successfully");
//            Log.d(TAG, "- account= " + account);
            mLog.d(TAG, "- getDisplayName= " + account.getDisplayName());
            mLog.d(TAG, "- getEmail= " + account.getEmail());
            mLog.d(TAG, "- getPhotoUrl= " + account.getPhotoUrl());

//            if (null == account.getPhotoUrl()) {
//                mGoogleSignInClient.signOut()
//                        .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                mLog.d(TAG, "signOut Complete");
//                            }
//                        });
//            } else {
                onFragmentInteractionListener.onLoginSuccess();
//            }


//            signInButton.setEnabled(false);
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            mLog.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    // -------------------------------------------
    public interface OnFragmentInteractionListener {
        void onLoginSuccess();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

}
