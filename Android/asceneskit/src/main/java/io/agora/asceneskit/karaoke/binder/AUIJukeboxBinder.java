package io.agora.asceneskit.karaoke.binder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.auikit.model.AUIChooseMusicModel;
import io.agora.auikit.model.AUIMusicModel;
import io.agora.auikit.model.AUIRoomContext;
import io.agora.auikit.service.IAUIChorusService;
import io.agora.auikit.service.IAUIJukeboxService;
import io.agora.auikit.service.IAUIMicSeatService;
import io.agora.auikit.service.callback.AUIException;
import io.agora.auikit.ui.jukebox.AUIMusicInfo;
import io.agora.auikit.ui.jukebox.IAUIJukeboxChosenItemView;
import io.agora.auikit.ui.jukebox.IAUIJukeboxView;

public class AUIJukeboxBinder implements IAUIBindable, IAUIJukeboxService.AUIJukeboxRespObserver, IAUIJukeboxView.ActionDelegate {
    private final IAUIJukeboxView jukeboxView;
    private final IAUIJukeboxService jukeboxService;
    private IAUIMicSeatService micSeatService;
    private IAUIChorusService chorusService;

    private Handler mMainHandler;
    private final Context context;

    public AUIJukeboxBinder(Context context,
                            IAUIJukeboxView jukeboxView,
                            IAUIJukeboxService jukeboxService,
                            IAUIMicSeatService micSeatService,
                            IAUIChorusService chorusService) {
        this.context = context;
        this.jukeboxView = jukeboxView;
        this.jukeboxService = jukeboxService;
        this.micSeatService = micSeatService;
        this.chorusService = chorusService;
    }

    @Override
    public void bind() {
        mMainHandler = new Handler(Looper.getMainLooper());
        jukeboxService.registerRespObserver(this);
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
        jukeboxService.unRegisterRespObserver(this);
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
        jukeboxService.chooseSong(model, error -> {
            if(error != null){
                runOnUiThread(() -> {
                    Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onSongPinged(AUIMusicInfo song) {
        jukeboxService.pingSong(song.getSongCode(), error -> {
            if(error != null){
                runOnUiThread(() -> {
                    Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onSongDeleted(AUIMusicInfo song) {
        jukeboxService.removeSong(song.getSongCode(), error -> {
            if(error != null){
                runOnUiThread(() -> {
                    Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onSongSwitched(AUIMusicInfo song) {
        jukeboxService.removeSong(song.getSongCode(), error -> {
            if(error != null){
                runOnUiThread(() -> {
                    Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // IAUIJukeboxService.AUIJukeboxRespDelegate implements

    @Override
    public void onUpdateAllChooseSongs(@NonNull List<AUIChooseMusicModel> songs) {
        runOnUiThread(()-> jukeboxView.setChosenSongList(transformChooseMusicModelList(songs)));
    }

    @Nullable
    @Override
    public AUIException onSongWillAdd(String userId, Map<String, String> metaData) {
        boolean onSeat = micSeatService.getMicSeatIndex(userId) >= 0;
        if(onSeat){
            return null;
        }
        return new AUIException(AUIException.ERROR_CODE_PERMISSION_LEAK, "");
    }

}
