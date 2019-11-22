package ichen.chu.drawnsend.pages.dashboard.listener;

import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import androidx.viewpager2.widget.ViewPager2;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.pages.dashboard.ThreadObject;
import ichen.chu.drawnsend.util.MLog;
import me.relex.circleindicator.CircleIndicator3;
import me.relex.circleindicator.Config;

public class GuideClickListener implements View.OnClickListener {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private Context mContext;

    public GuideClickListener(Context mContext) {
        this.mContext = mContext;
    }

    private Handler mySADHandler;

    // RecycleView

    /*data Block*/


    @Override
    public void onClick(View v) {
        mLog.d(TAG, "click GuideFAB");

        final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

        final ThreadObject threadObject = new ThreadObject();
        threadObject.setRunning(false);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.guide_frame_layout,null);

        GuideRecyclerAdapter mAdapter = new GuideRecyclerAdapter(mContext,4);
        ViewPager2 viewpager = frameLayout.findViewById(R.id.viewpager);
        viewpager.setAdapter(mAdapter);

        // CircleIndicator3 for RecyclerView
        CircleIndicator3 indicator = frameLayout.findViewById(R.id.indicator);
        int indicatorWidth = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                mContext.getResources().getDisplayMetrics()) + 0.5f);
        int indicatorHeight = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                mContext.getResources().getDisplayMetrics()) + 0.5f);
        int indicatorMargin = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6,
                mContext.getResources().getDisplayMetrics()) + 0.5f);

        Config config = new Config.Builder().width(indicatorWidth)
                .height(indicatorHeight)
                .margin(indicatorMargin)
                .animator(R.animator.indicator_animator)
                .animatorReverse(R.animator.indicator_animator_reverse)
                .drawable(R.drawable.black_radius_square)
                .build();
        indicator.initialize(config);

        indicator.setViewPager(viewpager);

        // CurrentItem
        viewpager.setCurrentItem(0,false);
        // Observe Data Change
        mAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());



        final SweetAlertDialog saDialog = new SweetAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE);

        saDialog.setCancelable(true);

        saDialog.setTitleText("Guide how to Play")
//                .setConfirmText("OK")
                .setCustomView(frameLayout)
                .hideConfirmButton()
                .show();
    }
}
