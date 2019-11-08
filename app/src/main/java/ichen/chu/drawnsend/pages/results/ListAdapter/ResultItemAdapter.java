package ichen.chu.drawnsend.pages.results.ListAdapter;

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
import android.widget.TextView;

import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.model.PlayerItem;
import ichen.chu.drawnsend.model.ResultItem;
import ichen.chu.drawnsend.util.MLog;

/**
 * Created by IChen.Chu on 2019/11/04
 */
public class ResultItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    final int OWNER_TYPE = 0;
    final int PARTICIPANT_TYPE = 1;
    final int UNKNOWN_TYPE = 2;

    // Constants
    final int RESULT_MARGIN = 20;

    // parent object
    private final LayoutInflater mLayoutInflater;

    /**
     * it's used to major operate and display.
     */
    private ArrayList<ResultItem> mResultDataList = new ArrayList<ResultItem>();

    /**
     * it's stored from DashboardEventListDisplayFragment.
     */
    private List<ResultItem> resultsDisplayDatas;


    public ResultItemAdapter(Context context, List<ResultItem> playerDataList) {
        mLog.d(TAG, "constructor");
        resultsDisplayDatas = playerDataList;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mLog.i(TAG, "onCreateViewHolder()" + ", viewType= " + viewType);
        RecyclerView.ViewHolder holder;
        ResultItemViewHolder uploadItemViewHolder = new ResultItemViewHolder(
                mLayoutInflater.inflate(R.layout.result_item_card, parent, false));
        uploadItemViewHolder.initView(viewType);
        holder = uploadItemViewHolder;
        return holder;
    }

    @Override
    public int getItemViewType(int position) {

        ResultItem resultItem = mResultDataList.get(position);
        mLog.i(TAG, "getItemViewType()" + ", position= " + position);
        return 0;
//        switch (resultItem.getPlayerType()) {
//            case OWNER: {
//                return OWNER_TYPE;
//            }
//            case PARTICIPANTS: {
//                return PARTICIPANT_TYPE;
//            }
//            case OWNER_RESULTS: {
//                return RESULTS_OWNER_TYPE;
//            }
//            case PARTICIPANTS_RESULTS: {
//                return RESULTS_PARTICIPANT_TYPE;
//            }
//            default:
//                return UNKNOWN_TYPE;
//        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mLog.i(TAG, "onBindViewHolder()" + ", position= " + position);
        if (holder instanceof ResultItemViewHolder) {
            ResultItemViewHolder resultItemViewHolder = (ResultItemViewHolder) holder;
            ResultItem resultItem = mResultDataList.get(position);
            resultItemViewHolder.bindData(resultItem, position);
        }
    }

    @Override
    public int getItemCount() {
        return mResultDataList == null ? 0 : mResultDataList.size();
    }


    // View Holder
    public class ResultItemViewHolder extends RecyclerView.ViewHolder {
        final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

        // Data

        /*View Block*/
        LinearLayout cardLayout;
        LinearLayout playerItemLinerLayout;
        CircleImageView playerAvatar;
        ImageView resultView;
        TextView resultIndex;

        /*Listener Block*/
        ResultItemClickListener playerItemClickListener = new ResultItemClickListener();

        public ResultItemViewHolder(final View itemView) {
            super(itemView);

            cardLayout = itemView.findViewById(R.id.cardLayout);
            playerAvatar = itemView.findViewById(R.id.playerAvatar);
            resultView = itemView.findViewById(R.id.resultView);
            resultIndex = itemView.findViewById(R.id.resultIndex);
            itemView.setOnClickListener(playerItemClickListener);
            itemView.setOnLongClickListener(playerItemClickListener);

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

        public void bindData(ResultItem item, int position) {
            mLog.w(TAG, "item: " + item.toString());
            try {
                resultIndex.setText(String.valueOf(position + 1));
                new DownloadImageTask(playerAvatar).execute((String) item.getUserInfo().get("photoUrl"));
                new DownloadImageTask(resultView).execute((String) item.getResultsUrl());
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            switch (item.getPlayerType()) {
//                case OWNER:
//                    break;
//                case PARTICIPANTS:
//                    break;
//            }
        }

        private class ResultItemClickListener implements View.OnClickListener, View.OnLongClickListener {
            @Override
            public void onClick(View view) {
                mLog.d(TAG, "onClick");
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
        mResultDataList.clear();

        for (int index = 0; index < resultsDisplayDatas.size(); index++) {
//            mLog.w(TAG, "sorted: " + resultsDisplayDatas.get(index).toString());
        }

        for (int index = 0; index < resultsDisplayDatas.size(); index++) {
            mResultDataList.add(resultsDisplayDatas.get(index));
            notifyItemInserted(index);
        }
    }

    public synchronized void clearAll() {
        mLog.i(TAG, "clearAll()");
        while (mResultDataList.size() > 0) {
            int itemIndex = mResultDataList.size() - 1;
            mResultDataList.remove(itemIndex);
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
