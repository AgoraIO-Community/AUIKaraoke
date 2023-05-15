package io.agora.auikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class AUiCreateRoomInfo implements Serializable {
    public @NonNull String roomName = "";       //房间名称
    public @NonNull String thumbnail = "";      //房间列表上的缩略图
    public int seatCount = 8;                   //麦位个数
    public @Nullable String password;           //房间密码
}
