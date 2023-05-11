package io.agora.auikit.model;

import android.util.SparseArray;
import android.view.View;

import androidx.annotation.NonNull;

public class AUiRoomConfig {
    public static final int TOKEN_RTM_LOGIN = 100;
    public static final int TOKEN_RTC_007 = 101;
    public static final int TOKEN_RTM_KTV = 102;
    public static final int TOKEN_RTC_SERVICE = 200;
    public static final int TOKEN_RTC_KTV_CHORUS = 201;

    public @NonNull
    final String channelName;
    public @NonNull
    final String ktvChannelName;
    public @NonNull
    final String ktvChorusChannelName;

    public @NonNull SparseArray<String> tokenMap = new SparseArray<>();

    public int themeId = View.NO_ID;

    public AUiRoomConfig(String roomId) {
        this(roomId,roomId + "_rtc", roomId + "_rtc_ex");
    }

    public AUiRoomConfig(@NonNull String channelName, @NonNull String ktvChannelName, @NonNull String ktvChorusChannelName) {
        this.channelName = channelName;
        this.ktvChannelName = ktvChannelName;
        this.ktvChorusChannelName = ktvChorusChannelName;
    }

}
