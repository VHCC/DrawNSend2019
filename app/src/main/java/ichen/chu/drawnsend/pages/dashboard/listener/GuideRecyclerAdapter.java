package ichen.chu.drawnsend.pages.dashboard.listener;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.util.MLog;

public class GuideRecyclerAdapter extends RecyclerView.Adapter<GuideRecyclerAdapter.GuideViewHolder> {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    protected int mPageCounts;

    // parent object
    private final LayoutInflater mLayoutInflater;

    public GuideRecyclerAdapter(Context context, int count) {
        mLog.d(TAG, "constructor");
        mPageCounts = count;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        mLog.i(TAG, "onCreateViewHolder()" + ", position= " + position);
        GuideViewHolder guideViewHolder = new GuideViewHolder(
                mLayoutInflater.inflate(R.layout.guide_page, parent, false));
        guideViewHolder.initView(position);
        return guideViewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        mLog.i(TAG, "getItemViewType()" + ", position= " + position);
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        mLog.i(TAG, "onBindViewHolder()" + ", position= " + position);
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return mPageCounts;
    }

    public void add() {
        int position = mPageCounts;
        mPageCounts++;
        notifyItemInserted(position);
    }

    public void remove() {
        if (mPageCounts == 0) {
            return;
        }
        mPageCounts--;
        int position = mPageCounts;
        notifyItemRemoved(position);
    }

    public static class GuideViewHolder extends RecyclerView.ViewHolder {
        final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

        // View


        private GuideViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void initView(int pagePosition) {
            mLog.w(TAG, "* initView: " + pagePosition);
        }

        void bindData(int position) {
            mLog.w(TAG, "* bindData: " + position);
        }
    }
}