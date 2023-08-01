package io.agora.asceneskit.karaoke.binder;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import io.agora.auikit.model.AUIChooseMusicModel;
import io.agora.auikit.model.AUIChoristerModel;
import io.agora.auikit.model.AUIMicSeatInfo;
import io.agora.auikit.model.AUIMicSeatStatus;
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
        IAUIMicSeatService.AUIMicSeatRespDelegate,
        IAUIChorusService.AUIChorusRespDelegate,
        IAUIJukeboxService.AUIJukeboxRespDelegate,
        IAUIUserService.AUIUserRespDelegate {
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
        userService.bindRespDelegate(this);
        micSeatService.bindRespDelegate(this);
        jukeboxService.bindRespDelegate(this);
        chorusService.bindRespDelegate(this);
        micSeatsView.setMicSeatActionDelegate(this);

        // update view
        IMicSeatItemView[] seatViewList = micSeatsView.getMicSeatItemViewList();
        for (int seatIndex = 0; seatIndex < seatViewList.length; seatIndex++) {
            AUIMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(seatIndex);
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
        userService.unbindRespDelegate(this);
        micSeatService.unbindRespDelegate(this);
        jukeboxService.unbindRespDelegate(this);
        chorusService.unbindRespDelegate(this);

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
    public void onSeatListChange(List<AUIMicSeatInfo> seatInfoList) {
        IAUIMicSeatService.AUIMicSeatRespDelegate.super.onSeatListChange(seatInfoList);
    }

    @Override
    public void onAnchorEnterSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        seatView.setTitleText(userInfo.userName);
        seatView.setUserAvatarImageUrl(userInfo.userAvatar);
    }

    @Override
    public void onAnchorLeaveSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        updateSeatView(seatIndex, null);
    }

    @Override
    public void onSeatAudioMute(int seatIndex, boolean isMute) {
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        seatView.setAudioMuteVisibility(isMute ? View.VISIBLE : View.GONE);
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
        IAUIMicSeatService.AUIMicSeatRespDelegate.super.onShowInvited(index);
    }

    private void updateSeatView(int seatIndex, @Nullable AUIMicSeatInfo micSeatInfo) {
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        if (micSeatInfo == null || micSeatInfo.seatStatus == AUIMicSeatStatus.idle) {
            seatView.setTitleIndex(seatIndex + 1);
            seatView.setAudioMuteVisibility(View.GONE);
            seatView.setVideoMuteVisibility(View.GONE);
            seatView.setUserAvatarImageDrawable(null);
            seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
            return;
        }
        AUIUserInfo userInfo = null;
        if (micSeatInfo.user != null) {
            userInfo = userService.getUserInfo(micSeatInfo.user.userId);
        }
        seatView.setRoomOwnerVisibility((seatIndex == 0) ? View.VISIBLE : View.GONE);


        seatView.setMicSeatState(MicSeatStatus.locked);

        boolean isAudioMute = (micSeatInfo.muteAudio != 0);
        if (userInfo != null) {
            isAudioMute = isAudioMute || (userInfo.muteAudio == 1);
        }
        seatView.setAudioMuteVisibility(isAudioMute ? View.VISIBLE : View.GONE);

        boolean isVideoMute = (micSeatInfo.muteVideo != 0);
        seatView.setVideoMuteVisibility(isVideoMute ? View.VISIBLE : View.GONE);

        if (micSeatInfo.user != null) {
            seatView.setTitleText(micSeatInfo.user.userName);
            seatView.setUserAvatarImageUrl(micSeatInfo.user.userAvatar);

            if (micSeatInfo.user.userId.equals(mLeadSingerId)) {
                seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.LeadSinger);
            } else if (mAccompanySingers.contains(micSeatInfo.user.userId)) {
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
            if (micSeatInfo.user == null || micSeatInfo.user.userId.isEmpty()) {
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
        dialogView.setUserInfo(seatInfo.user.userId);
        dialogView.setUserName(seatInfo.user.userName);
        dialogView.setUserAvatar(seatInfo.user.userAvatar);
        boolean isEmptySeat = (seatInfo.user == null || seatInfo.user.userId.length() == 0);
        boolean isCurrentUser = seatInfo.user != null && seatInfo.user.userId.equals(micSeatService.getRoomContext().currentUserInfo.userId);
        boolean isRoomOwner = micSeatService.getRoomContext().isRoomOwner(micSeatService.getChannelName());
        boolean inSeat = false;
        for (int i = 0; i <= 7; i++) {
            AUIMicSeatInfo info = micSeatService.getMicSeatInfo(i);
            if (info.user != null && info.user.userId.equals(micSeatService.getRoomContext().currentUserInfo.userId)) {
                inSeat = true;
                break;
            }
        }
        if (isRoomOwner) {
            if (isEmptySeat) {
                dialogView.addCloseSeat((seatInfo.seatStatus == AUIMicSeatStatus.locked));
            } else {
                if (isCurrentUser) {
                    dialogView.addMuteAudio((seatInfo.muteAudio != 0 || userService.getUserInfo(seatInfo.user.userId).muteAudio != 0));
                } else {
                    dialogView.addKickSeat();
                    dialogView.addMuteAudio((seatInfo.muteAudio != 0) || userService.getUserInfo(seatInfo.user.userId).muteAudio != 0);
                }
            }
        } else {
            if (isEmptySeat) {
                if (inSeat) {
                    return false;
                } else {
                    dialogView.addEnterSeat(true);
                }
            } else {
                if (isCurrentUser) {
                    dialogView.addLeaveSeat();
                    dialogView.addMuteAudio((seatInfo.muteAudio != 0 || userService.getUserInfo(seatInfo.user.userId).muteAudio != 0));
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

    }

    @Override
    public void onRoomUserEnter(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {

    }

    @Override
    public void onRoomUserLeave(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {

    }

    @Override
    public void onRoomUserUpdate(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {

    }

    @Override
    public void onUserAudioMute(@NonNull String userId, boolean mute) {
        for (int i = 0; i <= 7; i++) {
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
