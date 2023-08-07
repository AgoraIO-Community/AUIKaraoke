package io.agora.asceneskit.karaoke.binder;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.agora.auikit.model.AUIChooseMusicModel;
import io.agora.auikit.model.AUIMusicModel;
import io.agora.auikit.model.AUIRoomContext;
import io.agora.auikit.service.IAUIJukeboxService;
import io.agora.auikit.ui.jukebox.AUIMusicInfo;
import io.agora.auikit.ui.jukebox.IAUIJukeboxChosenItemView;
import io.agora.auikit.ui.jukebox.IAUIJukeboxView;

public class AUIJukeboxBinder implements IAUIBindable, IAUIJukeboxService.AUIJukeboxRespDelegate, IAUIJukeboxView.ActionDelegate {
    private final IAUIJukeboxView jukeboxView;
    private final IAUIJukeboxService jukeboxService;

    private Handler mMainHandler;

    public AUIJukeboxBinder(IAUIJukeboxView jukeboxView, IAUIJukeboxService jukeboxService) {
        this.jukeboxView = jukeboxView;
        this.jukeboxService = jukeboxService;
    }

    @Override
    public void bind() {
        mMainHandler = new Handler(Looper.getMainLooper());
        jukeboxService.bindRespDelegate(this);
        jukeboxView.setActionDelegate(this);

        jukeboxView.setChooseSongCategories(IAUIJukeboxService.songCategories);
        jukeboxService.getMusicList(IAUIJukeboxService.songCategoryIds.get(0), IAUIJukeboxService.songPageStartIndex, IAUIJukeboxService.songPageSize, (error, songList) -> {
            runOnUiThread(()-> jukeboxView.refreshChooseSongList(IAUIJukeboxService.songCategories.get(0), transformMusicModelList(songList)));
        });
        jukeboxService.getAllChooseSongList((error, songList) -> {
            runOnUiThread(()-> jukeboxView.setChosenSongList(transformChooseMusicModelList(songList)));
        });
    }

    private List<AUIMusicInfo> transformMusicModelList(List<AUIMusicModel> modelList){
        ArrayList<AUIMusicInfo> list = new ArrayList<>();
        if(modelList != null){
            for (AUIMusicModel model : modelList) {
                list.add( new AUIMusicInfo(model.songCode, model.name, model.singer, model.poster, ""));
            }
        }
        return list;
    }

    private List<AUIMusicInfo> transformChooseMusicModelList(List<AUIChooseMusicModel> modelList){
        ArrayList<AUIMusicInfo> list = new ArrayList<>();
        if(modelList != null){
            for (AUIChooseMusicModel model : modelList) {
                list.add( new AUIMusicInfo(model.songCode, model.name, model.singer, model.poster, model.owner.userId));
            }
        }
        return list;
    }

    @Override
    public void unBind() {
        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
        jukeboxService.unbindRespDelegate(this);
        jukeboxView.setActionDelegate(null);
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

    // IAUIJukeboxView.ActionDelegate implements

    @Override
    public void onChooseSongRefreshing(String category) {
        jukeboxService.getMusicList(IAUIJukeboxService.songCategoryIds.get(IAUIJukeboxService.songCategories.indexOf(category)), IAUIJukeboxService.songPageStartIndex, IAUIJukeboxService.songPageSize, (error, songList) -> {
            runOnUiThread(() -> jukeboxView.refreshChooseSongList(category, transformMusicModelList(songList)));
        });
    }

    @Override
    public void onChooseSongLoadMore(String category, int startIndex) {
        jukeboxService.getMusicList(IAUIJukeboxService.songCategoryIds.get(IAUIJukeboxService.songCategories.indexOf(category)), IAUIJukeboxService.songPageStartIndex + startIndex / IAUIJukeboxService.songPageSize, IAUIJukeboxService.songPageSize, (error, songList) -> {
            runOnUiThread(()-> jukeboxView.loadMoreChooseSongList(category, transformMusicModelList(songList)));
        });
    }

    @Override
    public void onChosenSongItemUpdating(IAUIJukeboxChosenItemView itemView, int position, @NonNull AUIMusicInfo model) {
        // 1. 是房主可以删除歌曲，可以置顶
        // 2. 不是房主只能删除自己的歌曲
        // 3. 第二个歌曲能删除但是不能 切歌/置顶
        AUIRoomContext context = jukeboxService.getRoomContext();
        boolean isRoomOwner = context.isRoomOwner(jukeboxService.getChannelName());
        itemView.setViewStatus(
                (position == 0),
                (isRoomOwner && position != 1),
                isRoomOwner || model.getOwnerUid().equals(context.currentUserInfo.userId));
    }

    @Override
    public void onSearchSongRefreshing(String condition) {
        jukeboxService.searchMusic(condition, IAUIJukeboxService.songPageStartIndex, IAUIJukeboxService.songPageSize, (error, songList) -> {
            runOnUiThread(()-> jukeboxView.refreshSearchSongList(transformMusicModelList(songList)));
        });
    }

    @Override
    public void onSearchSongLoadMore(String condition, int startIndex) {
        jukeboxService.searchMusic(condition, IAUIJukeboxService.songPageStartIndex + startIndex / IAUIJukeboxService.songPageSize, IAUIJukeboxService.songPageSize, (error, songList) -> {
            runOnUiThread(()-> jukeboxView.loadMoreSearchSongList(transformMusicModelList(songList)));
        });
    }

    @Override
    public void onSongChosen(AUIMusicInfo song) {
        AUIMusicModel model = new AUIMusicModel();
        model.songCode = song.getSongCode();
        model.name = song.getName();
        model.singer = song.getSinger();
        model.poster = song.getPost();
        jukeboxService.chooseSong(model, null);
    }

    @Override
    public void onSongPinged(AUIMusicInfo song) {
        jukeboxService.pingSong(song.getSongCode(), null);
    }

    @Override
    public void onSongDeleted(AUIMusicInfo song) {
        jukeboxService.removeSong(song.getSongCode(), null);
    }

    @Override
    public void onSongSwitched(AUIMusicInfo song) {
        jukeboxService.removeSong(song.getSongCode(), null);
    }

    // IAUIJukeboxService.AUIJukeboxRespDelegate implements

    @Override
    public void onUpdateAllChooseSongs(@NonNull List<AUIChooseMusicModel> songs) {
        runOnUiThread(()-> jukeboxView.setChosenSongList(transformChooseMusicModelList(songs)));
    }

}
