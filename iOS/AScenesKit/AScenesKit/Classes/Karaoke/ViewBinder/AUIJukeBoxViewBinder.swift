//
//  AUIJukeBoxViewBinder.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/10.
//

import Foundation
import AUIKitCore

private let kChartIds = [3, 4, 2, 6]
let kListPageCount: Int = 10
open class AUIJukeBoxViewBinder: NSObject {
    private weak var jukeBoxView: AUIJukeBoxView? {
        didSet {
            jukeBoxView?.uiDelegate = self
        }
    }
    private weak var serviceDelegate: AUIMusicServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            serviceDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    //歌曲查询列表
    private var searchMusicList: [AUIMusicModel]?
    //点歌列表
    private var musicListMap: [Int: [AUIMusicModel]] = [:]
    //已点列表
    private var addedMusicList: [AUIChooseMusicModel] = [] {
        didSet {
            if let topSong = addedMusicList.first,
                topSong.userId == serviceDelegate?.getRoomContext().currentUserInfo.userId,
                !topSong.isPlaying {
                self.serviceDelegate?.updatePlayStatus(songCode: topSong.songCode, playStatus: .playing) { error in
                    
                }
            }
            self.jukeBoxView?.selectedSongCount = addedMusicList.count
        }
    }
    /// 已点歌曲key的map
    private var addedMusicSet: NSMutableSet = NSMutableSet()
    
    /// 可置顶歌曲key
    private var pinEnableSet: NSMutableSet = NSMutableSet()
    
    /// 可删除歌曲key
    private var deleteEnableSet: NSMutableSet = NSMutableSet()
    
    deinit {
        aui_info("AUIJukeBoxViewBinder deinit", tag: "AUIJukeBoxViewBinder")
    }
    
    public override init() {
        super.init()
        aui_info("AUIJukeBoxViewBinder init", tag: "AUIJukeBoxViewBinder")
    }
    
    public func bind(jukeBoxView: AUIJukeBoxView, service: AUIMusicServiceDelegate) {
        self.jukeBoxView = jukeBoxView
        self.serviceDelegate = service
    }
}

extension AUIJukeBoxViewBinder: AUIJukeBoxViewDelegate {

    public func cleanSearchText(view: AUIJukeBoxView) {
        self.searchMusicList = nil
    }
    
    public func search(view: AUIJukeBoxView, text: String, completion: @escaping ([AUIJukeBoxItemDataProtocol]?)->()) {
        serviceDelegate?.searchMusic(keyword: text, page: 1, pageSize: kListPageCount, completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUIToast.show(text:err.localizedDescription)
                return
            }
            self.searchMusicList = list ?? []
            completion(list)
        })
    }
    
    public func onSegmentedChanged(view: AUIJukeBoxView, segmentIndex: Int) -> Bool {
        return false
    }
    
    public func onTabsDidChanged(view: AUIJukeBoxView, tabIndex: Int) -> Bool {
        return false
    }
    
    public func onSelectSong(view: AUIJukeBoxView, tabIndex: Int, index: Int) {
        guard let model = searchMusicList == nil ? musicListMap[tabIndex]?[index] : searchMusicList?[index] else {return}
        self.serviceDelegate?.chooseSong(songModel: model, completion: { error in
            guard let err = error else {return}
            AUIToast.show(text:err.localizedDescription)
        })
    }
    
    public func onRemoveSong(view: AUIJukeBoxView, index: Int) {
        let song = self.addedMusicList[index]
        self.serviceDelegate?.removeSong(songCode: song.songCode, completion: { error in
            guard let err = error else {return}
            AUIToast.show(text:err.localizedDescription)
        })
    }
    
    public func onNextSong(view: AUIJukeBoxView, index: Int) {
        AUIAlertView.theme_defaultAlert()
            .isShowCloseButton(isShow: false)
            .title(title: aui_localized("switchToNextSong"))
            .rightButton(title: "确认")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                self.onRemoveSong(view: view, index: index)
            })
            .leftButton(title: "取消")
            .show()
    }
    
    public func onPinSong(view: AUIJukeBoxView, index: Int) {
        let song = self.addedMusicList[index]
        self.serviceDelegate?.pinSong(songCode: song.songCode, completion: { error in
            guard let err = error else {return}
            AUIToast.show(text:err.localizedDescription)
        })
    }
    
    public func songIsSelected(view: AUIJukeBoxView, songCode: String) -> Bool {
        return addedMusicSet.contains(songCode)
    }
    
    public func pingEnable(view: AUIJukeBoxView, songCode: String) -> Bool {
        return pinEnableSet.contains(songCode)
    }
    
    public func deleteEnable(view: AUIJukeBoxView, songCode: String) -> Bool {
        return deleteEnableSet.contains(songCode)
    }
    
    public func getSearchMusicList(view: AUIJukeBoxView) -> [AUIJukeBoxItemDataProtocol]? {
        return self.searchMusicList
    }
    
    public func getMusicList(view: AUIJukeBoxView, tabIndex: Int) -> [AUIJukeBoxItemDataProtocol] {
        return self.musicListMap[tabIndex] ?? []
    }
    
    public func getSelectedSongList(view: AUIJukeBoxView) -> [AUIJukeBoxItemSelectedDataProtocol] {
        return self.addedMusicList
    }
    
    public func onRefreshMusicList(view: AUIJukeBoxView, tabIndex: Int, completion: @escaping ([AUIJukeBoxItemDataProtocol]?)->()) {
        let idx = tabIndex
        aui_info("onRefreshMusicList tabIndex: \(idx)", tag: "AUIJukeBoxViewBinder")
        self.serviceDelegate?.getMusicList(chartId: kChartIds[idx],
                                           page: 1,
                                           pageSize: kListPageCount,
                                           completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUIToast.show(text:err.localizedDescription)
                return
            }
            self.musicListMap[idx] = list ?? []
            completion(list)
        })
    }
    
    public func onLoadMoreMusicList(view: AUIJukeBoxView, tabIndex: Int, completion: @escaping ([AUIJukeBoxItemDataProtocol]?)->()) {
        let idx = tabIndex
        let musicListCount = musicListMap[idx]?.count ?? 0
        let page = 1 + musicListCount / kListPageCount
        if musicListCount % kListPageCount > 0 {
            //no more data
            completion(nil)
            return
        }
        aui_info("onLoadMoreMusicList tabIndex: \(idx) page: \(page)", tag: "AUIJukeBoxViewBinder")
        self.serviceDelegate?.getMusicList(chartId: kChartIds[idx],
                                           page: page,
                                           pageSize: kListPageCount,
                                           completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUIToast.show(text:err.localizedDescription)
                return
            }
            if let list = list {
                let musicList = self.musicListMap[idx] ?? []
                self.musicListMap[idx] = musicList + list
            }
            completion(list)
        })
    }
    
    public func onRefreshAddedMusicList(view: AUIJukeBoxView, completion: @escaping ([AUIJukeBoxItemSelectedDataProtocol]?) -> ()) {
        aui_info("onRefreshAddedMusicList", tag: "AUIJukeBoxViewBinder")
        self.serviceDelegate?.getAllChooseSongList(completion: { [weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUIToast.show(text:err.localizedDescription)
                return
            }
            self.addedMusicList = list ?? []
            completion(list)
        })
    }
}


extension AUIJukeBoxViewBinder: AUIMusicRespDelegate {
    
    public func _notifySongDidAdded(song: AUIChooseMusicModel) {
        addedMusicSet.add(song.songCode)
        if serviceDelegate?.currentUserIsRoomOwner() ?? false == true {
            deleteEnableSet.add(song.songCode)
            pinEnableSet.add(song.songCode)
        } else if song.owner?.userId == serviceDelegate?.getRoomContext().commonConfig?.userId {
            deleteEnableSet.add(song.songCode)
        }
    }
    public func _notifySongDidRemove(song: AUIChooseMusicModel) {
        addedMusicSet.remove(song.songCode)
        deleteEnableSet.remove(song.songCode)
        pinEnableSet.remove(song.songCode)
    }
    
    public func onAddChooseSong(song: AUIChooseMusicModel) {
        aui_info("onAddChooseSong \(song.name)", tag: "AUIJukeBoxViewBinder")
        addedMusicList.append(song)
        _notifySongDidAdded(song: song)
        jukeBoxView?.addedMusicTableView.reloadData()
        jukeBoxView?.allMusicTableView.reloadData()
    }
    
    public func onRemoveChooseSong(song: AUIChooseMusicModel) {
        aui_info("onRemoveChooseSong \(song.name)", tag: "AUIJukeBoxViewBinder")
        self.addedMusicList.removeAll(where: {$0.songCode == song.songCode})
        _notifySongDidRemove(song: song)
        self.jukeBoxView?.addedMusicTableView.reloadData()
        self.jukeBoxView?.allMusicTableView.reloadData()
    }
    
    public func onUpdateChooseSong(song: AUIChooseMusicModel) {
        aui_info("onUpdateChooseSong \(song.name)", tag: "AUIJukeBoxViewBinder")
        if let index = self.addedMusicList.firstIndex(where: {$0.songCode == song.songCode}) {
            self.addedMusicList.remove(at: index)
            self.addedMusicList.insert(song, at: index)
        } else {
            self.addedMusicList.append(song)
            _notifySongDidAdded(song: song)
        }
        self.jukeBoxView?.addedMusicTableView.reloadData()
        self.jukeBoxView?.allMusicTableView.reloadData()
    }
    
    public func onUpdateAllChooseSongs(songs: [AUIChooseMusicModel]) {
        aui_info("onUpdateAllChooseSongs", tag: "AUIJukeBoxViewBinder")
        self.addedMusicList = songs
        addedMusicSet.removeAllObjects()
        pinEnableSet.removeAllObjects()
        deleteEnableSet.removeAllObjects()
        addedMusicList.forEach { song in
            self._notifySongDidAdded(song: song)
        }
        self.jukeBoxView?.addedMusicTableView.reloadData()
        self.jukeBoxView?.allMusicTableView.reloadData()
    }
}
