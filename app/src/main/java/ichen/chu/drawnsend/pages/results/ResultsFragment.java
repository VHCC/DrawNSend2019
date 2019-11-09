package ichen.chu.drawnsend.pages.results;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.customview.scrollable.SampleHeaderView;
import ichen.chu.drawnsend.model.PlayerItem;
import ichen.chu.drawnsend.model.ResultItem;
import ichen.chu.drawnsend.pages.dashboard.ListAdapter.PlayerItemAdapter;
import ichen.chu.drawnsend.pages.results.ListAdapter.ResultItemAdapter;
import ichen.chu.drawnsend.util.MLog;
import ru.noties.ccf.CCFAnimator;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;

/**
 * Created by IChen.Chu on 2019/11/08
 */
public class ResultsFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Scrollable
    private ScrollableLayout scrollableLayout;
    private SampleHeaderView header;


    // RecycleView DnsPlayer Tab
    private RecyclerView recycleViewPlayerContainer;
    private PlayerItemAdapter playerItemAdapter;
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    private GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 5);

    // RecycleView Results
    private RecyclerView recycleViewResultsContainer;
    private ResultItemAdapter resultItemAdapter;
    private LinearLayoutManager linearLayoutManager_result = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);


    // Listener
    private ResultViewOnScrollChangedListener mResultViewOnScrollChangedListener = new ResultViewOnScrollChangedListener();

    // Animation
    private CCFAnimator mAnimator;

    /**
     * storage the result of player search.
     */
    private final List<PlayerItem> playerItemsList = new ArrayList<>();
    private final List<ResultItem> resultItemsList = new ArrayList<>();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SubPagesMainFragment.
     */
    public static ResultsFragment newInstance() {
        ResultsFragment fragment = new ResultsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    // Constructor
    public ResultsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mLog.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_results, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        scrollableLayout = rootView.findViewById(R.id.scrollable_layout);
        header = rootView.findViewById(R.id.header);

        // DnsPlayer Recycler View
        recycleViewPlayerContainer = rootView.findViewById(R.id.recycleViewPlayerContainer);

        // Results Recycler View
        recycleViewResultsContainer = rootView.findViewById(R.id.recycleViewResultsContainer);
    }


    private void initViewsFeature() {
        mAnimator = CCFAnimator.rgb(header.getExpandedColor(), header.getCollapsedColor());
        scrollableLayout.addOnScrollChangedListener(mResultViewOnScrollChangedListener);

        playerItemAdapter = new PlayerItemAdapter(getContext(), playerItemsList);
        recycleViewPlayerContainer.setAdapter(playerItemAdapter);
        recycleViewPlayerContainer.setLayoutManager(linearLayoutManager);
        recycleViewPlayerContainer.setNestedScrollingEnabled(false);

        // result
        resultItemAdapter = new ResultItemAdapter(getContext(), resultItemsList);
        recycleViewResultsContainer.setAdapter(resultItemAdapter);
        recycleViewResultsContainer.setLayoutManager(linearLayoutManager_result);
        recycleViewResultsContainer.setNestedScrollingEnabled(false);

        setTestData();
    }

    private void setTestData() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());

        for (int index = 0; index < 10; index ++) {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("email", "A" + index);
                jObj.put("displayName", acct.getDisplayName());
                jObj.put("photoUrl", acct.getPhotoUrl().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PlayerItem item = new PlayerItem(
                    PlayerItem.TYPE.OWNER_RESULTS,
                    jObj
            );
//            mLog.d(TAG, "item= " + item.toString());
            playerItemsList.add(item);
        }
        playerItemAdapter.clearAll();
        playerItemAdapter.refreshList();

        for (int index = 0; index < 10; index ++) {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("email", "A" + index);
                jObj.put("displayName", acct.getDisplayName());
                jObj.put("photoUrl", acct.getPhotoUrl().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String resultUrl = "https://lh3.googleusercontent.com/V61FwF080MLaf7U-DW_gWZYieQuVBd4QKN3tA0DdvRdBizLSwsA2fE-qt-a_d0OPj9O5Ohs9Ig0=s220";
            ResultItem item = new ResultItem(resultUrl, jObj
            );
//            mLog.d(TAG, "item= " + item.toString());
            resultItemsList.add(item);
        }

        resultItemAdapter.clearAll();
        resultItemAdapter.refreshList();
    }

    // ------------------------------------------------------------

    private class ResultViewOnScrollChangedListener implements OnScrollChangedListener {

        @Override
        public void onScrollChanged(int y, int oldY, int maxY) {

//                Debug.i("y: %s, oldY: %s, maxY: %s", y, oldY, maxY);

            final float tabsTranslationY;
            if (y < maxY) {
                tabsTranslationY = .0F;
            } else {
                tabsTranslationY = y - maxY;
            }

//                tabsLayout.setTranslationY(tabsTranslationY);

            // parallax effect for collapse/expand
            final float ratio = (float) y / maxY;
            header.setBackgroundColor(mAnimator.getColor(ratio));
            header.getTextView().setAlpha(1.F - ratio);
            header.getTextView().setTranslationY(y / 2);
        }
    }

}
