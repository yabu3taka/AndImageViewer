package ex1.siv.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import ex1.siv.area.AreaData;

public class CommonSetting {
    final static String FIELD_TIMER_TIME = "timerTime";
    final static String FIELD_NAVI_NEXT = "naviNext";
    final static String FIELD_NAVI_FLICK = "naviFlickNext";
    final static String FIELD_NAVI_PERCENT = "naviPercent";
    final static String FIELD_SIDE_POS = "sidePos";

    private final SharedPreferences mPref;

    public CommonSetting(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /*********************************************************************
     * For Timer
     *********************************************************************/
    public long getTimerTime() {
        String val = mPref.getString(FIELD_TIMER_TIME, "1500");
        return Long.parseLong(val);
    }

    static boolean isValidTimerTime(long time) {
        return time >= 1000;
    }

    /*********************************************************************
     * For Navi
     *********************************************************************/
    public NaviSetting getNaviSetting(AreaData area) {
        String flickStr =  mPref.getString(FIELD_NAVI_FLICK, PosType.RIGHT.getType());
        String nextStr = mPref.getString(FIELD_NAVI_NEXT, PosType.RIGHT.getType());
        String percentStr = mPref.getString(FIELD_NAVI_PERCENT, "20");
        return new NaviSetting(area, PosType.find(nextStr), Integer.parseInt(percentStr), PosType.find(flickStr));
    }

    static boolean isValidNaviPercent(int percent) {
        return 5 <= percent && percent <= 40;
    }

    /*********************************************************************
     * For Side List
     *********************************************************************/
    public SideListSetting getSideListSetting() {
        String sidePos = mPref.getString(FIELD_SIDE_POS, PosType.RIGHT.getType());
        return new SideListSetting(PosType.find(sidePos));
    }
}
