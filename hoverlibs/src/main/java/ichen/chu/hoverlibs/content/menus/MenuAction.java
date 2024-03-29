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
package ichen.chu.hoverlibs.content.menus;

import android.content.Context;

import androidx.annotation.NonNull;
import ichen.chu.hoverlibs.content.Navigator;

/**
 * Represents an action that executes when the user selects an item in a Hover menu.
 */
public interface MenuAction {

    /**
     * Executes a desired action, possibly navigating to new content by using the given {@code navigator}.
     * @param context context
     * @param navigator the {@link Navigator} that holds the menu that this action belongs to
     */
    void execute(@NonNull Context context, @NonNull Navigator navigator);

}
