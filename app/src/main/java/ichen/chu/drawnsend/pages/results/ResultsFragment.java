package ichen.chu.drawnsend.pages.results;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ichen.chu.drawnsend.Bus;
import ichen.chu.drawnsend.BusEvent;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.api.DnsServerAgent;
import ichen.chu.drawnsend.customview.scrollable.SampleHeaderView;
import ichen.chu.drawnsend.model.DnsGameChain;
import ichen.chu.drawnsend.model.DnsPlayRoom;
import ichen.chu.drawnsend.model.PlayerItem;
import ichen.chu.drawnsend.model.ResultItem;
import ichen.chu.drawnsend.pages.dashboard.ListAdapter.PlayerItemAdapter;
import ichen.chu.drawnsend.pages.results.ListAdapter.ResultItemAdapter;
import ichen.chu.drawnsend.util.MLog;
import ru.noties.ccf.CCFAnimator;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;

import static ichen.chu.drawnsend.Bus.EVENT_MAP;
import static ichen.chu.drawnsend.api.APICode.API_FETCH_GAME_CHAIN_INFO;

/**
 * Created by IChen.Chu on 2019/11/08
 */
public class ResultsFragment extends Fragment {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Field
    private String roomJoinNumber;

    // View
    private FloatingActionButton backDashboardFAB;

    // Scrollable
    private ScrollableLayout scrollableLayout;
    private SampleHeaderView subjectHeader;


    // RecycleView DnsPlayer Tab
    private RecyclerView recycleViewPlayerContainer;
    private PlayerItemAdapter playerItemAdapter;
    private LinearLayoutManager linearLayoutManager ;

    // RecycleView Results
    private RecyclerView recycleViewResultsContainer;
    private ResultItemAdapter resultItemAdapter;
    private LinearLayoutManager linearLayoutManager_result;

    // Google
    private GoogleSignInAccount acct;

    // Listener
    private ResultViewOnScrollChangedListener mResultViewOnScrollChangedListener = new ResultViewOnScrollChangedListener();
    private OnResultFragmentInteractionListener mResultFragmentListener;

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
        mLog.d(TAG, "- onCreate()");
        super.onCreate(savedInstanceState);
        isViewInitiated = true;
        Bus.getInstance().register(this);
    }

    protected boolean isViewInitiated;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLog.d(TAG, "- onViewCreated()");
        if (getUserVisibleHint() && isViewInitiated) {
            fetchGameChainData(acct.getEmail() + DnsPlayRoom.getInstance().getJoinNumber());
//            fetchGameChainData(acct.getEmail() + "299860");

        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        mLog.d(TAG, "- setUserVisibleHint()=  " + isVisibleToUser);
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isViewInitiated) {
            fetchGameChainData(acct.getEmail() + DnsPlayRoom.getInstance().getJoinNumber());
//            fetchGameChainData(acct.getEmail() + "299860");
        }
    }

    private void lazyLoad() {
        mLog.d(TAG, "- lazyLoad()");
//        setTestData();
        setResultData();
    }

    public void onEventMainThread(PlayerItem item){
        mLog.d(TAG, DnsGameChain.getInstance().toString());
        mLog.d(TAG, item.toString());
        try {
           String targetEmail = item.getUserInfo().getString("email");
           if (targetEmail.equals(DnsGameChain.getInstance().getCreator())) {

           } else {
//               fetchGameChainData(targetEmail + "299860");
               fetchGameChainData(targetEmail + DnsPlayRoom.getInstance().getJoinNumber());
           }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, "- onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_results, container, false);
        acct = GoogleSignIn.getLastSignedInAccount(getContext());
        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        scrollableLayout = rootView.findViewById(R.id.scrollable_layout);
        subjectHeader = rootView.findViewById(R.id.header);
        backDashboardFAB = rootView.findViewById(R.id.backDashboardFAB);

        // DnsPlayer Recycler View
        recycleViewPlayerContainer = rootView.findViewById(R.id.recycleViewPlayerContainer);

        // Results Recycler View
        recycleViewResultsContainer = rootView.findViewById(R.id.recycleViewResultsContainer);
    }


    private void initViewsFeature() {
        mAnimator = CCFAnimator.rgb(subjectHeader.getExpandedColor(), subjectHeader.getCollapsedColor());
        scrollableLayout.addOnScrollChangedListener(mResultViewOnScrollChangedListener);


        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        playerItemAdapter = new PlayerItemAdapter(getContext(), playerItemsList);
        recycleViewPlayerContainer.setAdapter(playerItemAdapter);
        recycleViewPlayerContainer.setLayoutManager(linearLayoutManager);
        recycleViewPlayerContainer.setNestedScrollingEnabled(false);

        // result
        linearLayoutManager_result = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        resultItemAdapter = new ResultItemAdapter(getContext(), resultItemsList);
        recycleViewResultsContainer.setAdapter(resultItemAdapter);
        recycleViewResultsContainer.setLayoutManager(linearLayoutManager_result);
        recycleViewResultsContainer.setNestedScrollingEnabled(false);

        backDashboardFAB.setIcon(R.drawable.dashboard_home);
        backDashboardFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToDashboard();
            }

            private void backToDashboard() {
                    mResultFragmentListener.onClickBackToDashboard();
            }
        });
    }

    private void setTestData() {

        for (int index = 0; index < 10; index ++) {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("email", "A" + index);
                jObj.put("displayName", acct.getDisplayName());
                if (null != acct.getPhotoUrl()) {
                    jObj.put("photoUrl", acct.getPhotoUrl().toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PlayerItem item = new PlayerItem(
                    PlayerItem.TYPE.OWNER_RESULTS,
                    jObj);
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
                if (null != acct.getPhotoUrl()) {
                    jObj.put("photoUrl", acct.getPhotoUrl().toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String resultUrl = "https://lh3.googleusercontent.com/V61FwF080MLaf7U-DW_gWZYieQuVBd4QKN3tA0DdvRdBizLSwsA2fE-qt-a_d0OPj9O5Ohs9Ig0=s220";
            ResultItem item = new ResultItem(resultUrl, jObj
            );
            resultItemsList.add(item);
        }

        resultItemAdapter.clearAll();
        resultItemAdapter.refreshList();
    }

    private void setResultData() {
        mLog.d(TAG, "* setResultData");

        try {
            playerItemsList.clear();
            for (int index = 0; index < DnsGameChain.getInstance().getPlayerChained().length(); index ++) {
                JSONObject playerItem = DnsGameChain.getInstance().getPlayerChained().getJSONObject(index);

                PlayerItem item = new PlayerItem(
                        PlayerItem.TYPE.OWNER_RESULTS,
                        playerItem
                );
                playerItemsList.add(item);
            }
            playerItemAdapter.clearAll();
            playerItemAdapter.refreshList();

            resultItemsList.clear();
            for (int index = 0; index < DnsGameChain.getInstance().getResultsChained().length(); index ++) {
                JSONObject playerItem = DnsGameChain.getInstance().getPlayerChained().getJSONObject(index);
                String resultItem = DnsGameChain.getInstance().getResultsChained().getString(index);
                ResultItem item = new ResultItem(resultItem, playerItem);
                resultItemsList.add(item);
            }

            resultItemAdapter.clearAll();
            resultItemAdapter.refreshList();
            } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Bus.getInstance().unregister(this);
        super.onDestroy();
    }
    // -------------------------------------------
    public interface OnResultFragmentInteractionListener {
        void onClickBackToDashboard();
    }

    public void setResultFragmentListener(OnResultFragmentInteractionListener listener) {
        mResultFragmentListener = listener;
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
            subjectHeader.setBackgroundColor(mAnimator.getColor(ratio));
            subjectHeader.getTextView().setAlpha(1.F - ratio);
            subjectHeader.getTextView().setTranslationY(y / 2);
        }
    }

    private Handler resultBoardHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mLog.d(TAG, "msg.obj= " + msg.obj);
            switch (msg.arg1) {
                case API_FETCH_GAME_CHAIN_INFO:
                    DnsGameChain.getInstance().setGameChainInfo((JSONObject) msg.obj);
                    mLog.d(TAG, DnsGameChain.getInstance().toString());
                    subjectHeader.setSubject(DnsGameChain.getInstance().getSubject());
                    lazyLoad();
                    break;
            }
        }
    };

    private void fetchGameChainData(String chainID) {

        DnsServerAgent.getInstance(getContext())
                .fetchGameChainInfo(resultBoardHandler,
                        chainID);
    }

}
