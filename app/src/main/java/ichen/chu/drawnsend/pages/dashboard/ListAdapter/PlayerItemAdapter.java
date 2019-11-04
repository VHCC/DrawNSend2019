package ichen.chu.drawnsend.pages.dashboard.ListAdapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.model.PlayerItem;
import ichen.chu.drawnsend.util.MLog;

/**
 * Created by IChen.Chu on 2019/11/04
 */
public class PlayerItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // parent object
    private final LayoutInflater mLayoutInflater;

    /**
     * it's used to major operate and display.
     */
    private ArrayList<PlayerItem> mUploadItemList = new ArrayList<PlayerItem>();

    /**
     * it's stored from DashboardEventListDisplayFragment.
     */
    private List<PlayerItem> playerDisplayResults;


    public PlayerItemAdapter(Context context, List<PlayerItem> playerDataList) {
        mLog.d(TAG, "constructor");
        playerDisplayResults = playerDataList;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mLog.i(TAG, "onCreateViewHolder()" + ", viewType= " + viewType);
        RecyclerView.ViewHolder holder;
        UploadItemViewHolder uploadItemViewHolder = new UploadItemViewHolder(
                mLayoutInflater.inflate(R.layout.upload_item_card, parent, false));
        uploadItemViewHolder.initView(viewType);
        holder = uploadItemViewHolder;
        return holder;
    }

    @Override
    public int getItemViewType(int position) {

        PlayerItem playerItem = mUploadItemList.get(position);
        mLog.i(TAG, "getItemViewType()" + ", position= " + position + ", type= " + playerItem.getPlayerType());

        switch (playerItem.getPlayerType()) {
            case OWNER: {
                return 0;
            }
            case PARTICIPANTS: {
                return 1;
            }
            default:
                return 2;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mLog.i(TAG, "onBindViewHolder()" + ", position= " + position);
        if (holder instanceof UploadItemViewHolder) {
            UploadItemViewHolder uploadItemViewHolder = (UploadItemViewHolder) holder;
            PlayerItem playerItem = mUploadItemList.get(position);
            uploadItemViewHolder.bindData(playerItem);
        }
    }

    @Override
    public int getItemCount() {
        return mUploadItemList == null ? 0 : mUploadItemList.size();
    }


    // View Holder
    public class UploadItemViewHolder extends RecyclerView.ViewHolder {
        final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

        /*View Block*/
        LinearLayout uploadItemCardTypeLinerLayout;
        ImageView uploadItemCardType;

        /*Listener Block*/
        EventItemClickListener eventItemClickListener;

        public UploadItemViewHolder(final View itemView) {
            super(itemView);

            uploadItemCardTypeLinerLayout = (LinearLayout) itemView.findViewById(R.id.uploadItemCardTypeLinerLayout);
            uploadItemCardType = (ImageView) itemView.findViewById(R.id.uploadItemCardType);
            itemView.setOnClickListener(eventItemClickListener);
            itemView.setOnLongClickListener(eventItemClickListener);

        }

        public void initView(int viewType) {
            switch (viewType) {
                case 0: {
//                    uploadItemCardType.setImageResource(R.drawable.ic3_btn_inspection_feedback);
//                    uploadItemCardTypeLinerLayout.setPadding(33, 30 , 33, 30);
                }
                break;
                case 1: {
//                    uploadItemCardType.setImageResource(R.drawable.ic3_btn_receive_feedback);
                }
                break;
            }
        }

        public void bindData(PlayerItem item) {
            mLog.w(TAG, "item: " + item.toString());
            switch (item.getPlayerType()) {
                case OWNER:
                    break;
                case PARTICIPANTS:
                    break;
            }
        }

        private class EventItemClickListener implements View.OnClickListener, View.OnLongClickListener {
            @Override
            public void onClick(View view) {
                mLog.d(TAG, "onClick");
                switch (view.getId()) {
                }
            }

            @Override
            public boolean onLongClick(View view) {
                mLog.d(TAG, "onLongClick");
                return true;
            }
        }
    }

    // Feature
    public synchronized void refreshList() {
        mLog.i(TAG, "refreshList()");
        mUploadItemList.clear();

//        Collections.sort(playerDisplayResults, new Comparator<PlayerItem>() {
//            @Override
//            public int compare(PlayerItem playerItem1, PlayerItem playerItem2) {
//                return ((int) (Long.valueOf(playerItem1.getUserInfo()) - Long.valueOf(playerItem2.getUserInfo())) * -1); // -1 降; 1 升
//            }
//        });

        for (int index = 0; index < playerDisplayResults.size(); index++) {
            mLog.w(TAG, "sorted: " + playerDisplayResults.get(index).toString());
        }

        for (int index = 0; index < playerDisplayResults.size(); index++) {
            mUploadItemList.add(playerDisplayResults.get(index));
            notifyItemInserted(index);
        }
    }

    public synchronized void clearAll() {
        mLog.i(TAG, "clearAll()");
        while (mUploadItemList.size() > 0) {
            int itemIndex = mUploadItemList.size() - 1;
            mUploadItemList.remove(itemIndex);
            notifyItemRemoved(itemIndex);
        }
    }

}
