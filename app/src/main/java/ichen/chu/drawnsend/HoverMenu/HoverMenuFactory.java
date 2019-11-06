/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ichen.chu.drawnsend.HoverMenu;

import android.content.Context;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import de.greenrobot.event.EventBus;
import ichen.chu.drawnsend.Bus;
import ichen.chu.drawnsend.HoverMenu.colorselection.ColorSelectionContent;
import ichen.chu.drawnsend.HoverMenu.intro.HoverIntroductionContent;
import ichen.chu.drawnsend.HoverMenu.theme.HoverThemeManager;
import ichen.chu.drawnsend.util.MLog;
import ichen.chu.hoverlibs.Content;

/**
 * Can create a Hover menu from code or from file.
 */
public class HoverMenuFactory {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    /**
     * Example of how to create a menu in code.
     * @return HoverMenu
     */
    public MyHoverMenu createDemoMenuFromCode(@NonNull Context context, @NonNull EventBus bus) throws IOException {
//        Menu drillDownMenuLevelTwo = new Menu("Demo Menu - Level 2", Arrays.asList(
//                new MenuItem(UUID.randomUUID().toString(), "Google", new DoNothingMenuAction()),
//                new MenuItem(UUID.randomUUID().toString(), "Amazon", new DoNothingMenuAction())
//        ));
//        ShowSubmenuMenuAction showLevelTwoMenuAction = new ShowSubmenuMenuAction(drillDownMenuLevelTwo);
//
//        Menu drillDownMenu = new Menu("Demo Menu", Arrays.asList(
//                new MenuItem(UUID.randomUUID().toString(), "GPS", new DoNothingMenuAction()),
//                new MenuItem(UUID.randomUUID().toString(), "Cell Tower Triangulation", new DoNothingMenuAction()),
//                new MenuItem(UUID.randomUUID().toString(), "Location Services", showLevelTwoMenuAction)
//        ));
//
//        MenuListContent drillDownMenuNavigatorContent = new MenuListContent(context, drillDownMenu);
//
//        ToolbarNavigator toolbarNavigator = new ToolbarNavigator(context);
//        toolbarNavigator.pushContent(drillDownMenuNavigatorContent);

        Map<String, Content> menuStack = new LinkedHashMap<>();
        menuStack.put(MyHoverMenu.M_1, new HoverIntroductionContent(context, Bus.getInstance()));
        menuStack.put(MyHoverMenu.SELECT_COLOR_ID, new ColorSelectionContent(context, Bus.getInstance(), HoverThemeManager.getInstance(), HoverThemeManager.getInstance().getTheme()));


        return new MyHoverMenu(context, "ichenTab", menuStack, HoverThemeManager.getInstance().getTheme());
    }

}
