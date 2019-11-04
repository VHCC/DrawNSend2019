package ichen.chu.drawnsend;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import de.greenrobot.event.EventBus;
import ichen.chu.drawableviewlibs.DrawableView;
import ichen.chu.drawableviewlibs.DrawableViewConfig;
import ichen.chu.drawnsend.pages.dashboard.DashboardMainFragment;
import ichen.chu.drawnsend.pages.home.HomeFragment;
import ichen.chu.drawnsend.pages.login.LoginFragment;
import ichen.chu.drawnsend.pages.pagerAdapter.tranform.ScaleInOutTransformer;
import ichen.chu.drawnsend.pages.subPage.SubPageEmptyFragment;
import ichen.chu.drawnsend.util.MLog;
import io.mattcarroll.hover.HoverView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private DrawableView drawableView;
    private DrawableViewConfig config = new DrawableViewConfig();

    private int RC_SIGN_IN = 1001;
    private final int RC_PERMISSIONS = 9001;

    private HoverView mHoverView;

    private GoogleSignInClient mGoogleSignInClient;




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
//        initUi();

//        initHover();

        checkPermission();



        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.mainContainer);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, new ScaleInOutTransformer());
    }

    private void checkPermission() {
        String[] perms = {
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
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


//    private void initUi() {
//        drawableView = (DrawableView) findViewById(R.id.paintView);
//        Button strokeWidthMinusButton = (Button) findViewById(R.id.strokeWidthMinusButton);
//        strokeWidthMinusButton.setVisibility(View.GONE);
//        Button strokeWidthPlusButton = (Button) findViewById(R.id.strokeWidthPlusButton);
//        strokeWidthPlusButton.setVisibility(View.GONE);
//        final Button changeColorButton = (Button) findViewById(R.id.changeColorButton);
//        Button undoButton = (Button) findViewById(R.id.undoButton);
//        undoButton.setVisibility(View.GONE);
//        Button clearButton = (Button) findViewById(R.id.clearButton);
//        Button getButton = (Button) findViewById(R.id.getButton);
//
//        // Set the dimensions of the sign-in button.
//        SignInButton signInButton = findViewById(R.id.sign_in_button);
//        signInButton.setSize(SignInButton.SIZE_STANDARD);
//
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
//        int height = size.y;
//
//        config.setStrokeColor(getResources().getColor(android.R.color.black));
//        config.setShowCanvasBounds(true);
//        config.setStrokeWidth(20.0f);
//        config.setMinZoom(1.0f);
//        config.setMaxZoom(2.0f);
//        config.setCanvasHeight(height);
//        config.setCanvasWidth(width);
//        drawableView.setConfig(config);
//
//        strokeWidthPlusButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                config.setStrokeWidth(config.getStrokeWidth() + 10);
//            }
//        });
//        strokeWidthMinusButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                config.setStrokeWidth(config.getStrokeWidth() - 10);
//            }
//        });
//        changeColorButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Random random = new Random();
//                int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
//                config.setStrokeColor(color);
//                changeColorButton.setBackgroundColor(color);
//            }
//        });
//        undoButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                drawableView.undo();
//            }
//        });
//
//        clearButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                drawableView.clear();
//            }
//        });
//
//        getButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Bitmap outB = drawableView.obtainBitmap().copy(Bitmap.Config.ARGB_8888, true);
//                        Canvas canvas = new Canvas(outB);
//                        canvas.drawColor(Color.WHITE);
//                        canvas.drawBitmap(drawableView.obtainBitmap(), 0, 0, null);
//
////                        image.setImageBitmap(outB);
//
//                        String tmp = "/sdcard/test/" + System.currentTimeMillis() + ".jpeg";
//                        File file = new File(tmp);
//
//                        try {
//                            FileOutputStream out = new FileOutputStream(file);
//                            if (outB.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
//                                out.flush();
//                                out.close();
//                            }
//                            uploadFile(file);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//
//            }
//        });
//
//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signIn();
//            }
//
//            private void signIn() {
//                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, RC_SIGN_IN);
//            }
//        });
//
//    }

//    private void initHover() {
//        mLog.d(TAG, "* initHover()");
//        try {
//            final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.AppTheme);
//            HoverMenu hoverMenu = new HoverMenuFactory().createDemoMenuFromCode(contextThemeWrapper, Bus.getInstance());
//
//            mHoverView = findViewById(R.id.hovermenu);
//            mHoverView.setMenu(hoverMenu);
//            mHoverView.enableDebugMode(true);
//            mHoverView.collapse();
//        } catch (Exception e) {
//            mLog.e(TAG, "Failed to create demo menu from file. e= " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

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
        mHoverView.collapse();
    }

    private void uploadFile(File file) {
        try {
            try {
                final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
                RequestBody req = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("name", file.getName())
                        .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE_JPEG, file)).build();

                Request request = new Request.Builder()
                        .url("http://172.20.10.3:3000/api/post_official_doc_upload_file")
                        .post(req)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                mLog.d(TAG, "uploadImage: " + response.body().string());

            } catch (UnknownHostException | UnsupportedEncodingException e) {
                mLog.e(TAG, "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bus.getInstance().unregister(this);
    }

    // ------------------------------------------------------------

    public class SectionsPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        // Constants
        static final int PAGE_HOME = 0;
        static final int PAGE_LOGIN = 1;
        static final int PAGE_DASHBOARD = 2;
        static final int PAGE_SUB_PAGE = 3;

        // Fields
        private final int[] PAGE_GROUP = new int[]{
                PAGE_HOME, PAGE_LOGIN, PAGE_DASHBOARD, PAGE_SUB_PAGE
        };
        private final String[] PAGE_NAMES = new String[]{
                "PAGE_HOME", "PAGE_LOGIN", "PAGE_DASHBOARD", "PAGE_SUB_PAGE"
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
                                Bus.getInstance().post(new BusEvent("login success", 1001));
                            } else {
                                mViewPager.setCurrentItem(PAGE_LOGIN);
                            }
                        }
                    });
                    fragment = homeFragment;
                }
                break;

                case PAGE_LOGIN: {
//                    ItemFragment itemFragment = ItemFragment.getInstance(1);
//                    fragment = itemFragment;
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

                case PAGE_SUB_PAGE: {
                    SubPageEmptyFragment subPagesMainFragment = SubPageEmptyFragment.newInstance();
                    fragment = subPagesMainFragment;
                }
                break;
            }
            mLog.v(TAG, "getItem(): " + fragment.toString());
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
            mLog.d(TAG, "onPageSelected(): " + PAGE_NAMES[lastPosition] + " >> " + PAGE_NAMES[position]);
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
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DASHBOARD);
                Bus.getInstance().post(new BusEvent("login success", 1001));
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

        };

    }

}
