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
package ichen.chu.drawnsend;

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import ichen.chu.drawnsend.util.MLog;

/**
 * Globally accessible EventBus.
 */
public class Bus {

    static public final int EVENT_LOGIN_SUCCESS = 1001;

    static public final int EVENT_DRAWABLE_CHANGE_STROKE_SIZE_1 = 4001;
    static public final int EVENT_DRAWABLE_CHANGE_STROKE_SIZE_2 = 4002;
    static public final int EVENT_DRAWABLE_CHANGE_STROKE_SIZE_3 = 4003;
    static public final int EVENT_DRAWABLE_CHANGE_STROKE_SIZE_4 = 4004;
    static public final int EVENT_DRAWABLE_CHANGE_STROKE_SIZE_5 = 4005;



    static public final int EVENT_DASHBOARD_GET_PLAYER_ORDER = 5001;

    static public final int EVENT_DASHBOARD_START_TO_PLAY_GAME = 6001;




    static public final HashMap<Integer, String> EVENT_MAP = new HashMap<>();

    static {
        EVENT_MAP.put(EVENT_LOGIN_SUCCESS, "login success");
        EVENT_MAP.put(EVENT_DRAWABLE_CHANGE_STROKE_SIZE_1, "change stroke size");
        EVENT_MAP.put(EVENT_DRAWABLE_CHANGE_STROKE_SIZE_2, "change stroke size");
        EVENT_MAP.put(EVENT_DRAWABLE_CHANGE_STROKE_SIZE_3, "change stroke size");
        EVENT_MAP.put(EVENT_DRAWABLE_CHANGE_STROKE_SIZE_4, "change stroke size");
        EVENT_MAP.put(EVENT_DRAWABLE_CHANGE_STROKE_SIZE_5, "change stroke size");
        EVENT_MAP.put(EVENT_DASHBOARD_GET_PLAYER_ORDER, "api - get player order");
        EVENT_MAP.put(EVENT_DASHBOARD_START_TO_PLAY_GAME, "start to play game");
    }

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private static EventBus sBus = new EventBus();

    public static EventBus getInstance() {
        return sBus;
    }

}
