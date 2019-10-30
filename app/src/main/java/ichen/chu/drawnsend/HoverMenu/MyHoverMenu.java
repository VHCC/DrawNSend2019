package ichen.chu.drawnsend.HoverMenu;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ichen.chu.drawnsend.HoverMenu.base.DemoTabView;
import ichen.chu.drawnsend.HoverMenu.theme.HoverTheme;
import ichen.chu.drawnsend.R;
import ichen.chu.drawnsend.util.MLog;
import io.mattcarroll.hover.Content;
import io.mattcarroll.hover.HoverMenu;

/**
 * Created by IChen.Chu on 2019/10/29
 */
public class MyHoverMenu extends HoverMenu {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    public static final String M_1 = "m_1";
    public static final String SELECT_COLOR_ID = "select_color";

    private Context mContext;
    private final String mMenuId;
    private HoverTheme mTheme;
    private final List<Section> mSections = new ArrayList<>();

    public MyHoverMenu(@NonNull Context context,
                       @NonNull String menuId,
                       @NonNull Map<String, Content> data,
                       @NonNull HoverTheme theme) {
        mContext = context;
        mMenuId = menuId;
        mTheme = theme;

        for (String tabId : data.keySet()) {
            mSections.add(new Section(
                    new SectionId(tabId),
                    createTabView(tabId),
                    data.get(tabId)
            ));
        }

    }

    private View createTabView() {

        Resources resources = mContext.getResources();
        int elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, resources.getDisplayMetrics());

        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(R.drawable.tab_background);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setElevation(elevation);
        }

        return imageView;
    }

    private View createTabView(String tabID) {
        switch (tabID) {
            case M_1:
                return createTabView(R.drawable.ic_orange_circle, mTheme.getAccentColor(), mTheme.getBaseColor());
            case SELECT_COLOR_ID:
                return createTabView(R.drawable.ic_paintbrush, mTheme.getAccentColor(), mTheme.getBaseColor());
            default:
                throw new RuntimeException("Unknown tab selected: " + tabID);
        }
    }

    private View createTabView(@DrawableRes int tabBitmapRes, @ColorInt int backgroundColor, @ColorInt Integer iconColor) {
        Resources resources = mContext.getResources();
        int elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, resources.getDisplayMetrics());

        DemoTabView view = new DemoTabView(mContext, resources.getDrawable(R.drawable.tab_background), resources.getDrawable(tabBitmapRes));
        view.setTabBackgroundColor(backgroundColor);
        view.setTabForegroundColor(iconColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(elevation);
        }
        return view;
    }

    @Deprecated
    private Content createContent() {
        return new HoverMenuScreen(mContext, "Screen 1");
    }

    @Override
    public String getId() {
        return mMenuId;
    }

    @Override
    public int getSectionCount() {
        return mSections.size();
    }

    @Nullable
    @Override
    public Section getSection(int index) {
        return mSections.get(index);
    }

    @Nullable
    @Override
    public Section getSection(@NonNull SectionId sectionId) {
        for (Section section : mSections) {
            if (section.getId().equals(sectionId)) {
                return section;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public List<Section> getSections() {
        return new ArrayList<>(mSections);
    }


}
