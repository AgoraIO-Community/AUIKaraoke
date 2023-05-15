package io.agora.auikit.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import io.agora.auikit.model.AUiChooseMusicModel;
import io.agora.auikit.model.AUiMusicModel;
import io.agora.auikit.model.AUiPlayStatus;
import io.agora.auikit.service.callback.AUiCallback;
import io.agora.auikit.service.callback.AUiChooseSongListCallback;
import io.agora.auikit.service.callback.AUiMusicListCallback;

/**
 * 点歌器Service抽象协议
 */
public interface IAUiJukeboxService extends IAUiCommonService<IAUiJukeboxService.AUiJukeboxRespDelegate>{

    // 0 -> "项目热歌榜单"
    // 1 -> "声网热歌榜"
    // 2 -> "新歌榜" ("热门新歌")
    // 3 -> "嗨唱推荐"
    // 4 -> "抖音热歌"
    // 5 -> "古风热歌"
    // 6 -> "KTV必唱"
    List<String> songCategories = Arrays.asList("热门新歌", "嗨歌推荐", "抖音热歌", "KTV必唱");
    List<Integer> songCategoryIds = Arrays.asList(2, 3, 4, 6);
    int songPageSize = 10;
    int songPageStartIndex = 1;

    /**
     * 获取歌曲列表
     *
     * @param chartId 榜单类型
     * @param page 页数，从1开始
     * @param pageSize 一页返回数量，最大50
     * @param completion 成功/失败回调
     */
    void getMusicList(int chartId, int page, int pageSize, @Nullable AUiMusicListCallback completion);

    /**
     * 搜索歌曲
     *
     * @param keyword 关键字
     * @param page 页数，从1开始
     * @param pageSize 一页返回数量，最大50
     * @param completion 成功/失败回调
     */
    void searchMusic(String keyword, int page, int pageSize, @Nullable AUiMusicListCallback completion);

    /**
     * 获取当前点歌列表
     *
     * @param completion 成功/失败回调
     */
    void getAllChooseSongList(@Nullable AUiChooseSongListCallback completion);

    /**
     * 点一首歌
     *
     * @param song 歌曲对象(是否需要只传songNo，后端通过mcc查？)
     * @param completion 成功/失败回调
     */
    void chooseSong(@NonNull AUiMusicModel song, @Nullable AUiCallback completion);

    /**
     * 移除一首自己点的歌
     *
     * @param songCode 歌曲id
     * @param completion 成功/失败回调
     */
    void removeSong(@NonNull String songCode, @Nullable AUiCallback completion);

    /**
     * 置顶歌曲
     *
     * @param songCode 歌曲id
     * @param completion 成功/失败回调
     */
    void pingSong(@NonNull String songCode, @Nullable AUiCallback completion);

    /**
     * 更新播放状态
     * @param songCode 歌曲id
     * @param playStatus 播放状态 0：待播放，1：播放中
     */
    void updatePlayStatus(@NonNull String songCode, @AUiPlayStatus int playStatus,@Nullable AUiCallback completion);

    interface AUiJukeboxRespDelegate {

        /**
         * 新增一首歌曲回调
         *
         * @param song 新增歌曲
         */
        default void onAddChooseSong(@NonNull AUiChooseMusicModel song) {
        }

        /**
         * 删除一首歌歌曲回调
         *
         * @param song 删除歌曲
         */
        default void onRemoveChooseSong(@NonNull AUiChooseMusicModel song) {
        }

        /**
         * 更新一首歌曲回调（例如pin）
         *
         * @param song 更新歌曲
         */
        default void onUpdateChooseSong(@NonNull AUiChooseMusicModel song) {
        }

        /**
         * 更新所有歌曲回调（例如pin）
         *
         * @param songs 更新歌曲
         */
        default void onUpdateAllChooseSongs(@NonNull List<AUiChooseMusicModel> songs){}
    }
}
