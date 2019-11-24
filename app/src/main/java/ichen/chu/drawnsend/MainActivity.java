package ichen.chu.drawnsend;

import android.Manifest;
import android.graphics.Typeface;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import ichen.chu.drawnsend.model.DnsPlayer;
import ichen.chu.drawnsend.pages.dashboard.DashboardMainFragment;
import ichen.chu.drawnsend.pages.home.HomeFragment;
import ichen.chu.drawnsend.pages.login.LoginFragment;
import ichen.chu.drawnsend.pages.pagerAdapter.tranform.ScaleInOutTransformer;
import ichen.chu.drawnsend.pages.playboard.PlayBoardMainFragment;
import ichen.chu.drawnsend.pages.results.ResultsFragment;
import ichen.chu.drawnsend.pages.subPage.SubPageEmptyFragment;
import ichen.chu.drawnsend.util.MLog;
import pub.devrel.easypermissions.EasyPermissions;

import static ichen.chu.drawnsend.Bus.EVENT_LOGIN_SUCCESS;
import static ichen.chu.drawnsend.Bus.EVENT_MAP;

public class MainActivity extends AppCompatActivity {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private final int RC_PERMISSIONS = 9001;

    // Font Family
    public static Typeface CUSTOM_FONT = null;
    
    /**
     * The {@link ViewPager} will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * The {@link androidx.viewpager.widget.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link androidx.fragment.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLog.d(TAG, "* onCreate()");
        Bus.getInstance().registerSticky(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.mainContainer);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, new ScaleInOutTransformer());
        mViewPager.setOffscreenPageLimit(1);

        CUSTOM_FONT = Typeface.createFromAsset(getAssets(), "Pacifico-Regular.ttf");
    }

    private void checkPermission() {
        String[] perms = {
//                Manifest.permission.CAMERA,
//                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.app_name),
                    RC_PERMISSIONS, perms);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bus.getInstance().unregister(this);
    }

    public void onEventBackgroundThread(BusEvent event) {
//        mLog.d(TAG, "* CCC");
//        event.getMessage();
//        mHoverView.collapse();
    }

    public void onEvent(BusEvent event){
//        mLog.d(TAG, "* BBB");
//        event.getMessage();
//        mHoverView.collapse();
    }

    public void onEventMainThread(BusEvent event){
//        mLog.d(TAG, "* AAA");
//        event.getMessage();
    }


    // ------------------------------------------------------------

    public class SectionsPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        // Constants
//        static final int PAGE_HOME = 100;
//        static final int PAGE_LOGIN = 200;
//        static final int PAGE_DASHBOARD = 300;
//        static final int PAGE_PLAY_BOARD = 400;
//        static final int PAGE_RESULTS = 0;
//        static final int PAGE_SUB_PAGE = 9999;

        static final int PAGE_HOME = 0;
        static final int PAGE_LOGIN = 1;
        static final int PAGE_DASHBOARD = 2;
        static final int PAGE_PLAY_BOARD = 3;
        static final int PAGE_RESULTS = 4;
        static final int PAGE_SUB_PAGE = 99;

        // Fields
        private final int[] PAGE_GROUP = new int[]{
                PAGE_HOME,
                PAGE_LOGIN,
                PAGE_DASHBOARD,
                PAGE_PLAY_BOARD,
                PAGE_RESULTS,
                PAGE_SUB_PAGE
        };
        private final String[] PAGE_NAMES = new String[]{
                "PAGE_HOME",
                "PAGE_LOGIN",
                "PAGE_DASHBOARD",
                "PAGE_PLAY_BOARD",
                "PAGE_RESULTS",
                "PAGE_SUB_PAGE"
        };
        private final Fragment[] fragments = new Fragment[PAGE_GROUP.length];

        // logic fields
        private int lastPosition = PAGE_HOME;

        // Constructor
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch(position) {
                case PAGE_HOME: {
                    HomeFragment homeFragment = HomeFragment.newInstance(2000);
                    homeFragment.setHomeFragmentListener(new HomeFragment.OnHomeFragmentInteractionListener() {
                        public void onShowEnd() {
                            mLog.d(TAG, "onShowEnd()");
                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                            if (acct != null) {
                                mViewPager.setCurrentItem(PAGE_DASHBOARD);
                                Bus.getInstance().post(new BusEvent(EVENT_MAP.get(EVENT_LOGIN_SUCCESS), EVENT_LOGIN_SUCCESS));
                            } else {
                                mViewPager.setCurrentItem(PAGE_LOGIN);
                            }
                        }
                    });
                    fragment = homeFragment;
                }
                break;

                case PAGE_LOGIN: {
                    LoginFragment loginFragment = LoginFragment.newInstance();
                    loginFragment.setOnFragmentInteractionListener(loginFragmentListener);
                    fragment = loginFragment;
                }
                break;

                case PAGE_DASHBOARD: {
                    DashboardMainFragment dashboardMainFragment = DashboardMainFragment.newInstance();
                    dashboardMainFragment.setDashboardMainFragmentListener(dashboardMainFragmentInteractionListener);
                    fragment = dashboardMainFragment;
                }
                break;

                case PAGE_PLAY_BOARD: {
                    PlayBoardMainFragment playBoardMainFragment = PlayBoardMainFragment.newInstance();
                    playBoardMainFragment.setPlayBoardMainFragmentListener(playBoardMainFragmentInteractionListener);
                    fragment = playBoardMainFragment;
                }
                break;

                case PAGE_RESULTS: {
                    ResultsFragment resultsFragment = ResultsFragment.newInstance();
                    resultsFragment.setResultFragmentListener(resultFragmentInteractionListener);
                    fragment = resultsFragment;
                }
                break;

                case PAGE_SUB_PAGE: {
                    SubPageEmptyFragment subPagesMainFragment = SubPageEmptyFragment.newInstance();
                    fragment = subPagesMainFragment;
                }
                break;
                default:
                    SubPageEmptyFragment subPagesMainFragment = SubPageEmptyFragment.newInstance();
                    fragment = subPagesMainFragment;
                    break;
            }
            mLog.v(TAG, "getItem(): [" +position + "], "+ fragment.toString());
            fragments[position] = fragment;
            return fragment;
        }

        @Override
        public int getCount() {
            return PAGE_GROUP.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "SECTION " + position;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mLog.d(TAG, "* onPageSelected(): " + PAGE_NAMES[lastPosition] + " >> " + PAGE_NAMES[position]);
            switch (position) {
                case PAGE_DASHBOARD:
                    ((DashboardMainFragment)fragments[PAGE_DASHBOARD]).userLoginSucceed();
                    break;
                case PAGE_LOGIN:
                    if (lastPosition == PAGE_DASHBOARD) {
                        ((DashboardMainFragment)fragments[PAGE_DASHBOARD]).userLogOutSucceed();
                    }
            }
            lastPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        // interactive fragment listener
        // ----------------------------------
        private LoginFragment.OnFragmentInteractionListener loginFragmentListener
                = new LoginFragment.OnFragmentInteractionListener() {
            @Override
            public void onLoginSuccess() {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

                DnsPlayer.getInstance().setPlayerInfo(acct);

                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DASHBOARD);
                Bus.getInstance().post(new BusEvent(EVENT_MAP.get(EVENT_LOGIN_SUCCESS), EVENT_LOGIN_SUCCESS));
//                MessageTools.showToast(mContext, "Login Succeed!");
//                MessageTools.showToast(mContext, "登入成功");
            }
        };

        private DashboardMainFragment.OnDashboardMainFragmentInteractionListener dashboardMainFragmentInteractionListener
                = new DashboardMainFragment.OnDashboardMainFragmentInteractionListener() {
            @Override
            public void onLogOutSuccess() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_LOGIN);
//                MessageTools.showToast(mContext, "Logout Succeed!");
            }

            @Override
            public void onStartToPlayGame() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_PLAY_BOARD);
            }

        };

        private PlayBoardMainFragment.OnPlayBoardMainFragmentInteractionListener playBoardMainFragmentInteractionListener
                = new PlayBoardMainFragment.OnPlayBoardMainFragmentInteractionListener() {
            @Override
            public void onGameSet() {
                mLog.d(TAG, "onGameSet");
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_RESULTS);
            }
        };

        private ResultsFragment.OnResultFragmentInteractionListener resultFragmentInteractionListener =
                new ResultsFragment.OnResultFragmentInteractionListener() {
            @Override
            public void onClickBackToDashboard() {
                mLog.d(TAG, "onClickBackToDashboard");
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DASHBOARD);
                Bus.getInstance().post(new BusEvent(EVENT_MAP.get(EVENT_LOGIN_SUCCESS), EVENT_LOGIN_SUCCESS));
            }
        };

    }

}
