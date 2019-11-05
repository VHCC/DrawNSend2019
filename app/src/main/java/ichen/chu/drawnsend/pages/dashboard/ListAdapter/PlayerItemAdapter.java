package ichen.chu.drawnsend.pages.dashboard.ListAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.model.PlayerItem;
import ichen.chu.drawnsend.pages.dashboard.DashboardMainFragment;
import ichen.chu.drawnsend.util.MLog;

/**
 * Created by IChen.Chu on 2019/11/04
 */
public class PlayerItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    final int OWNER_TYPE = 0;
    final int PARTICIPANT_TYPE = 1;
    final int UNKNOWN_TYPE = 2;

    // parent object
    private final LayoutInflater mLayoutInflater;

    /**
     * it's used to major operate and display.
     */
    private ArrayList<PlayerItem> mPlayerDataList = new ArrayList<PlayerItem>();

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
        PlayerItemViewHolder uploadItemViewHolder = new PlayerItemViewHolder(
                mLayoutInflater.inflate(R.layout.upload_item_card, parent, false));
        uploadItemViewHolder.initView(viewType);
        holder = uploadItemViewHolder;
        return holder;
    }

    @Override
    public int getItemViewType(int position) {

        PlayerItem playerItem = mPlayerDataList.get(position);
        mLog.i(TAG, "getItemViewType()" + ", position= " + position + ", type= " + playerItem.getPlayerType());

        switch (playerItem.getPlayerType()) {
            case OWNER: {
                return OWNER_TYPE;
            }
            case PARTICIPANTS: {
                return PARTICIPANT_TYPE;
            }
            default:
                return UNKNOWN_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mLog.i(TAG, "onBindViewHolder()" + ", position= " + position);
        if (holder instanceof PlayerItemViewHolder) {
            PlayerItemViewHolder playerItemViewHolder = (PlayerItemViewHolder) holder;
            PlayerItem playerItem = mPlayerDataList.get(position);
            playerItemViewHolder.bindData(playerItem);
        }
    }

    @Override
    public int getItemCount() {
        return mPlayerDataList == null ? 0 : mPlayerDataList.size();
    }


    // View Holder
    public class PlayerItemViewHolder extends RecyclerView.ViewHolder {
        final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

        /*View Block*/
        LinearLayout playerItemLinerLayout;
        CircleImageView playerAvatar;

        /*Listener Block*/
        EventItemClickListener eventItemClickListener;

        public PlayerItemViewHolder(final View itemView) {
            super(itemView);

            playerItemLinerLayout = (LinearLayout) itemView.findViewById(R.id.playerItemLinerLayout);
            playerAvatar = (CircleImageView) itemView.findViewById(R.id.playerAvatar);
            itemView.setOnClickListener(eventItemClickListener);
            itemView.setOnLongClickListener(eventItemClickListener);

        }

        // at onCreateViewHolder
        public void initView(int viewType) {
            switch (viewType) {
                case OWNER_TYPE: {
//                    playerAvatar.setImageResource(R.drawable.ic3_btn_inspection_feedback);
//                    playerItemLinerLayout.setPadding(33, 30 , 33, 30);
                }
                break;
                case PARTICIPANT_TYPE: {
//                    playerAvatar.setImageResource(R.drawable.ic3_btn_receive_feedback);
                }
                break;
            }
        }

        public void bindData(PlayerItem item) {
            mLog.w(TAG, "item: " + item.toString());
            try {
                new DownloadImageTask(playerAvatar).execute((String) item.getUserInfo().get("photoUrl"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        mPlayerDataList.clear();

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
            mPlayerDataList.add(playerDisplayResults.get(index));
            notifyItemInserted(index);
        }
    }

    public synchronized void clearAll() {
        mLog.i(TAG, "clearAll()");
        while (mPlayerDataList.size() > 0) {
            int itemIndex = mPlayerDataList.size() - 1;
            mPlayerDataList.remove(itemIndex);
            notifyItemRemoved(itemIndex);
        }
    }

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

}
