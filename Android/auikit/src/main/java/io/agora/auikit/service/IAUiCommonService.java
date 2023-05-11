package io.agora.auikit.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.auikit.model.AUiRoomContext;

public interface IAUiCommonService<Delegate> {

    /**
     * 绑定响应事件回调，可绑定多个
     *
     * @param delegate 响应事件回调
     */
    void bindRespDelegate(@Nullable Delegate delegate);

    /**
     * 解绑响应事件回调
     *
     * @param delegate 响应事件回调
     */
    void unbindRespDelegate(@Nullable Delegate delegate);

    /**
     *
     * @return
     */
    @NonNull AUiRoomContext getContext();

    @NonNull String getChannelName();

}
