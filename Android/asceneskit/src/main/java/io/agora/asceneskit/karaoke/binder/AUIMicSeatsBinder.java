package io.agora.asceneskit.karaoke.binder;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.agora.auikit.model.AUIChooseMusicModel;
import io.agora.auikit.model.AUIChoristerModel;
import io.agora.auikit.model.AUIMicSeatInfo;
import io.agora.auikit.model.AUIMicSeatStatus;
import io.agora.auikit.model.AUIRoomContext;
import io.agora.auikit.model.AUIRoomInfo;
import io.agora.auikit.model.AUIUserInfo;
import io.agora.auikit.model.AUIUserThumbnailInfo;
import io.agora.auikit.service.IAUIChorusService;
import io.agora.auikit.service.IAUIJukeboxService;
import io.agora.auikit.service.IAUIMicSeatService;
import io.agora.auikit.service.IAUIUserService;
import io.agora.auikit.service.callback.AUIChooseSongListCallback;
import io.agora.auikit.service.callback.AUIChoristerListCallback;
import io.agora.auikit.service.callback.AUIException;
import io.agora.auikit.ui.micseats.IMicSeatDialogView;
import io.agora.auikit.ui.micseats.IMicSeatItemView;
import io.agora.auikit.ui.micseats.IMicSeatsView;
import io.agora.auikit.ui.micseats.MicSeatStatus;

public class AUIMicSeatsBinder implements
        IAUIBindable,
        IMicSeatsView.ActionDelegate,
        IAUIMicSeatService.AUIMicSeatRespObserver,
        IAUIChorusService.AUIChorusRespObserver,
        IAUIJukeboxService.AUIJukeboxRespObserver,
        IAUIUserService.AUIUserRespObserver {
    private final IMicSeatsView micSeatsView;
    private final IAUIUserService userService;
    private final IAUIMicSeatService micSeatService;
    private final IAUIJukeboxService jukeboxService;
    private final IAUIChorusService chorusService;
    private Handler mMainHandler;
    private String mLeadSingerId = "";

    private LinkedList<String> mAccompanySingers = new LinkedList<String>();

    public AUIMicSeatsBinder(
            IMicSeatsView micSeatsView,
            IAUIUserService userService,
            IAUIMicSeatService micSeatService,
            IAUIJukeboxService jukeboxService,
            IAUIChorusService chorusService) {
        this.userService = userService;
        this.micSeatsView = micSeatsView;
        this.micSeatService = micSeatService;
        this.jukeboxService = jukeboxService;
        this.chorusService = chorusService;
    }

    @Override
    public void bind() {
        mMainHandler = new Handler(Looper.getMainLooper());
        userService.registerRespObserver(this);
        micSeatService.registerRespObserver(this);
        jukeboxService.registerRespObserver(this);
        chorusService.registerRespObserver(this);
        micSeatsView.setMicSeatActionDelegate(this);

        // update view
        IMicSeatItemView[] seatViewList = micSeatsView.getMicSeatItemViewList();
        for (int seatIndex = 0; seatIndex < seatViewList.length; seatIndex++) {
            AUIMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(seatIndex);
            if (seatIndex == 0 && micSeatInfo == null) {
                AUIRoomInfo roomInfo = AUIRoomContext.shared().getRoomInfo(micSeatService.getChannelName());
                if (roomInfo != null && roomInfo.owner != null) {
                    micSeatInfo = new AUIMicSeatInfo();
                    micSeatInfo.seatIndex = 0;
                    micSeatInfo.user = roomInfo.owner;
                    micSeatInfo.seatStatus = AUIMicSeatStatus.used;
                }
            }
            updateSeatView(seatIndex, micSeatInfo);
        }

        jukeboxService.getAllChooseSongList(new AUIChooseSongListCallback() {
            @Override
            public void onResult(@Nullable AUIException error, @Nullable List<AUIChooseMusicModel> songList) {
                if (songList != null && songList.size() != 0) {
                    AUIChooseMusicModel song = songList.get(0);
                    mLeadSingerId = song.owner.userId;
                    runOnUiThread(() -> updateChorusTag() );
                }
            }
        });
        chorusService.getChoristersList(new AUIChoristerListCallback() {
            @Override
            public void onResult(@Nullable AUIException error, @Nullable List<AUIChoristerModel> songList) {
                if (songList == null) {
                    return;
                }
                for (AUIChoristerModel song : songList) {
                    mAccompanySingers.add(song.userId);
                }
                if (mAccompanySingers.size() != 0) {
                    runOnUiThread(() -> updateChorusTag() );
                }
            }
        });
    }

    @Override
    public void unBind() {
        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
        userService.unRegisterRespObserver(this);
        micSeatService.unRegisterRespObserver(this);
        jukeboxService.unRegisterRespObserver(this);
        chorusService.unRegisterRespObserver(this);

        micSeatsView.setMicSeatActionDelegate(null);
    }
    private void runOnUiThread(@NonNull Runnable runnable) {
        if (mMainHandler != null) {
            if (mMainHandler.getLooper().getThread() == Thread.currentThread()) {
                runnable.run();
            } else {
                mMainHandler.post(runnable);
            }
        }
    }
    /** IAUIMicSeatService.AUIMicSeatRespDelegate implements. */
    @Override
    public void onAnchorEnterSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(seatIndex);
        updateSeatView(seatIndex, seatInfo);
    }

    @Override
    public void onAnchorLeaveSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(seatIndex);
        updateSeatView(seatIndex, seatInfo);
    }

    @Override
    public void onSeatAudioMute(int seatIndex, boolean isMute) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(seatIndex);
        updateSeatView(seatIndex, seatInfo);
    }

    @Override
    public void onSeatVideoMute(int seatIndex, boolean isMute) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(seatIndex);
        updateSeatView(seatIndex, seatInfo);
    }

    @Override
    public void onSeatClose(int seatIndex, boolean isClose) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(seatIndex);
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        if(seatInfo == null || seatView == null){
            return;
        }
        MicSeatStatus status;
        switch (seatInfo.seatStatus){
            case AUIMicSeatStatus.locked: status = MicSeatStatus.locked; break;
            case AUIMicSeatStatus.used: status = MicSeatStatus.used; break;
            default: status = MicSeatStatus.idle;
        }
        seatView.setMicSeatState(status);
    }

    @Override
    public void onShowInvited(int index) {
        IAUIMicSeatService.AUIMicSeatRespObserver.super.onShowInvited(index);
    }

    @Nullable
    @Override
    public AUIException onSeatWillLeave(String userId, Map<String, String> metadata) {
        jukeboxService.cleanUserInfo(userId, null);
        chorusService.cleanUserInfo(userId, null);
        return IAUIMicSeatService.AUIMicSeatRespObserver.super.onSeatWillLeave(userId, metadata);
    }

    private void updateSeatView(int seatIndex, @Nullable AUIMicSeatInfo micSeatInfo) {
        if(seatIndex >= micSeatsView.getMicSeatItemViewList().length){
            return;
        }

        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        AUIUserInfo userInfo = null;
        if (micSeatInfo != null && micSeatInfo.user != null && !TextUtils.isEmpty(micSeatInfo.user.userId)) {
            userInfo = userService.getUserInfo(micSeatInfo.user.userId);
            if(userInfo == null){
                userInfo = new AUIUserInfo();
                userInfo.userId = micSeatInfo.user.userId;
                userInfo.userName = micSeatInfo.user.userName;
                userInfo.userAvatar = micSeatInfo.user.userAvatar;
            }
        }

        String roomOwner = AUIRoomContext.shared().getRoomOwner(micSeatService.getChannelName());

        Log.d("AUIMicSeatsBinder", "updateSeatView >> seatIndex=" + seatIndex + ", micSeatInfo=" + micSeatInfo + ", userInfo=" + userInfo + ", roomOwner=" + roomOwner);

        // 麦位状态
        if (micSeatInfo == null || micSeatInfo.seatStatus == AUIMicSeatStatus.idle) {
            seatView.setTitleIndex(seatIndex + 1);
            seatView.setUserAvatarImageDrawable(null);
            seatView.setMicSeatState(MicSeatStatus.idle);
            seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
        } else if (micSeatInfo.seatStatus == AUIMicSeatStatus.locked) {
            seatView.setMicSeatState(MicSeatStatus.locked);
        } else {
            seatView.setMicSeatState(MicSeatStatus.used);
        }

        // 房主标识

        seatView.setRoomOwnerVisibility((userInfo != null && userInfo.userId.equals(roomOwner)) ? View.VISIBLE : View.GONE);

        // 静音状态
        boolean isAudioMute = (micSeatInfo != null && micSeatInfo.muteAudio) || (userInfo != null && userInfo.muteAudio == 1);
        seatView.setAudioMuteVisibility(isAudioMute ? View.VISIBLE : View.GONE);

        // 视频状态
        boolean isVideoMute = micSeatInfo != null && micSeatInfo.muteVideo;
        seatView.setVideoMuteVisibility(isVideoMute ? View.VISIBLE : View.GONE);

        // 用户信息
        if (userInfo != null) {
            seatView.setTitleText(userInfo.userName);
            seatView.setUserAvatarImageUrl(userInfo.userAvatar);
            if (userInfo.userId.equals(mLeadSingerId)) {
                seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.LeadSinger);
            } else if (mAccompanySingers.contains(userInfo.userId)) {
                seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.SecondarySinger);
            } else {
                seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
            }
        }
    }

    private void setLeadSingerId(String str) {
        if (str.equals(mLeadSingerId)) {
            return;
        }
        mLeadSingerId = str;
        updateChorusTag();
    }

    private void updateChorusTag() {
        IMicSeatItemView[] seatViewList = micSeatsView.getMicSeatItemViewList();
        for (int i = 0; i < seatViewList.length; i++) {
            AUIMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(i);
            IMicSeatItemView itemView = seatViewList[i];
            if (micSeatInfo == null || micSeatInfo.user == null || micSeatInfo.user.userId.isEmpty()) {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
                continue;
            }
            if (micSeatInfo.user.userId.equals(mLeadSingerId)) {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.LeadSinger);
            } else if (mAccompanySingers.contains(micSeatInfo.user.userId)) {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.SecondarySinger);
            } else {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
            }
        }
    }

    /** AUIMicSeatDialogView.ActionDelegate implements. */
    @Override
    public boolean onClickSeat(int index, IMicSeatDialogView dialogView) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(index);
        AUIUserInfo userInfo = seatInfo != null && seatInfo.user != null ? userService.getUserInfo(seatInfo.user.userId) : null;
        boolean isEmptySeat = seatInfo == null || seatInfo.user == null || seatInfo.user.userId.isEmpty() || seatInfo.seatStatus == AUIMicSeatStatus.idle;
        boolean isCurrentUser = seatInfo != null  && seatInfo.user != null && seatInfo.user.userId.equals(micSeatService.getRoomContext().currentUserInfo.userId);
        boolean isRoomOwner = micSeatService.getRoomContext().isRoomOwner(micSeatService.getChannelName());
        boolean isSeatLocked = seatInfo != null && seatInfo.seatStatus == AUIMicSeatStatus.locked;
        boolean isSeatAudioMute = seatInfo!= null && seatInfo.muteAudio;
        boolean isUserAudioMute = userInfo != null && userInfo.muteAudio != 0;
        boolean inSeat = false;
        for (int i = 0; i < micSeatService.getMicSeatSize(); i++) {
            AUIMicSeatInfo info = micSeatService.getMicSeatInfo(i);
            if (info != null && info.user != null && info.user.userId.equals(micSeatService.getRoomContext().currentUserInfo.userId)) {
                inSeat = true;
                break;
            }
        }

        // 设置弹窗用户信息
        if(userInfo != null){
            dialogView.setUserInfo(userInfo.userId);
            dialogView.setUserName(userInfo.userName);
            dialogView.setUserAvatar(userInfo.userAvatar);
        }


        // 根据角色和在线状态配置显示的选项
        if (isRoomOwner) {
            if (isEmptySeat) {
                dialogView.addCloseSeat(isSeatLocked);
            } else if (!isCurrentUser) {
                dialogView.addKickSeat();
            }
            dialogView.addMuteAudio(isSeatAudioMute);
        } else {
            if (isEmptySeat) {
                if (inSeat || isSeatLocked) {
                    return false;
                } else {
                    dialogView.addEnterSeat(true);
                }
            } else {
                if (isCurrentUser) {
                    dialogView.addLeaveSeat();
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onClickEnterSeat(int index) {
        micSeatService.enterSeat(index, null);
    }

    @Override
    public void onClickLeaveSeat(int index) {
        micSeatService.leaveSeat(null);
    }

    @Override
    public void onClickKickSeat(int index) {
        micSeatService.kickSeat(index, null);
    }

    @Override
    public void onClickCloseSeat(int index, boolean isClose) {
        micSeatService.closeSeat(index, isClose, null);
    }

    @Override
    public void onClickMuteAudio(int index, boolean mute) {
        boolean isRoomOwner = micSeatService.getRoomContext().isRoomOwner(micSeatService.getChannelName());
        if (isRoomOwner) {
            micSeatService.muteAudioSeat(index, mute, null);
        } else {
            userService.muteUserAudio(mute, null);
        }
    }

    @Override
    public void onClickMuteVideo(int index, boolean mute) {
        micSeatService.muteVideoSeat(index, mute, null);
    }

    @Override
    public void onClickInvited(int index) {
        micSeatService.onClickInvited(index);
    }

    /** IAUIMicSeatService.AUIChorusRespDelegate implements. */
    @Override
    public void onChoristerDidEnter(AUIChoristerModel chorister) {
        if (mAccompanySingers.contains(chorister.userId)) {
            return;
        }
        mAccompanySingers.add(chorister.userId);
        updateChorusTag();
    }

    @Override
    public void onChoristerDidLeave(AUIChoristerModel chorister) {
        if (mAccompanySingers.contains(chorister.userId)) {
            mAccompanySingers.remove(chorister.userId);
            updateChorusTag();
        }
    }

    @Override
    public void onSingerRoleChanged(int oldRole, int newRole) {

    }

    @Override
    public void onChoristerDidChanged() {

    }

    /** IAUIMicSeatService.AUIJukeboxRespDelegate implements. */
    @Override
    public void onAddChooseSong(@NonNull AUIChooseMusicModel song) {

    }

    @Override
    public void onRemoveChooseSong(@NonNull AUIChooseMusicModel song) {

    }

    @Override
    public void onUpdateChooseSong(@NonNull AUIChooseMusicModel song) {


    }

    @Override
    public void onUpdateAllChooseSongs(@NonNull List<AUIChooseMusicModel> songs) {
        if (songs.size() != 0) {
            AUIChooseMusicModel song = songs.get(0);
            mAccompanySingers.clear();
            setLeadSingerId(song.owner.userId);
        } else {
            mAccompanySingers.clear();
            setLeadSingerId("");
        }
    }


    /** IAUIUserService.AUIUserRespDelegate implements. */
    @Override
    public void onRoomUserSnapshot(@NonNull String roomId, @Nullable List<AUIUserInfo> userList) {
        for (int i = 0; i < micSeatService.getMicSeatSize(); i++) {
            AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(i);
            if (seatInfo != null && seatInfo.user != null) {
                updateSeatView(i, seatInfo);
            }
        }
    }

    @Override
    public void onRoomUserEnter(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {

    }

    @Override
    public void onRoomUserLeave(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {

    }

    @Override
    public void onRoomUserUpdate(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {
        for (int i = 0; i < micSeatService.getMicSeatSize(); i++) {
            AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(i);
            if (seatInfo != null && seatInfo.user != null && seatInfo.user.userId.equals(userInfo.userId)) {
                updateSeatView(i, seatInfo);
                break;
            }
        }
    }

    @Override
    public void onUserAudioMute(@NonNull String userId, boolean mute) {
        for (int i = 0; i < micSeatService.getMicSeatSize(); i++) {
            AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(i);
            if (seatInfo != null && seatInfo.user != null && seatInfo.user.userId.equals(userId)) {
                updateSeatView(i, seatInfo);
                break;
            }
        }
    }

    @Override
    public void onUserVideoMute(@NonNull String userId, boolean mute) {

    }
}
