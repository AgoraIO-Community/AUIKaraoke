package io.agora.auikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AUiRoomContext {

    public @NonNull AUiUserThumbnailInfo currentUserInfo = new AUiUserThumbnailInfo();
    public @NonNull AUiCommonConfig roomConfig = new AUiCommonConfig();
    private final Map<String, AUiRoomInfo> roomInfoMap = new HashMap<>();

    public boolean isRoomOwner(String channelName){
        AUiRoomInfo roomInfo = roomInfoMap.get(channelName);
        if(roomInfo == null || roomInfo.roomOwner == null){
            return false;
        }
        return roomInfo.roomOwner.userId.equals(currentUserInfo.userId);
    }

    public void resetRoomMap(@Nullable List<AUiRoomInfo> roomInfoList) {
        roomInfoMap.clear();
        if (roomInfoList == null || roomInfoList.size() == 0) {
            return;
        }
        for (AUiRoomInfo info : roomInfoList) {
            roomInfoMap.put(info.roomId, info);
        }
    }

    public void insertRoomInfo(AUiRoomInfo info) {
        roomInfoMap.put(info.roomId, info);
    }

    public void cleanRoom(String channelName){
        roomInfoMap.remove(channelName);
    }

}
