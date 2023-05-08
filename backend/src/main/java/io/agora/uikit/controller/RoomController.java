package io.agora.uikit.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.agora.rtm.Metadata;
import io.agora.uikit.bean.dto.R;
import io.agora.uikit.bean.dto.RoomDto;
import io.agora.uikit.bean.dto.RoomListDto;
import io.agora.uikit.bean.dto.RoomQueryDto;
import io.agora.uikit.bean.entity.RoomListEntity;
import io.agora.uikit.bean.req.ChorusJoinReq;
import io.agora.uikit.bean.req.RoomCreateReq;
import io.agora.uikit.bean.req.RoomDestroyReq;
import io.agora.uikit.bean.req.RoomLeaveReq;
import io.agora.uikit.bean.req.RoomListReq;
import io.agora.uikit.bean.req.RoomQueryReq;
import io.agora.uikit.bean.req.SongAddReq;
import io.agora.uikit.service.IChorusService;
import io.agora.uikit.service.IMicSeatService;
import io.agora.uikit.service.IRoomService;
import io.agora.uikit.service.ISongService;

@Validated
@RestController
@RequestMapping(value = "/v1/room", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomController {
    @Autowired
    private IRoomService roomService;
    @Autowired
    private IMicSeatService micSeatService;
    @Autowired
    private ISongService songService;
    @Autowired
    private IChorusService chorusService;

    /**
     * Create
     * 
     * @param roomCreateReq
     * @return
     * @throws Exception
     */
    @PostMapping("/create")
    @ResponseBody
    public R<RoomDto> create(@Validated @RequestBody RoomCreateReq roomCreateReq) throws Exception {
        if (roomCreateReq.getRoomId() == null) {
            roomCreateReq.setRoomId(UUID.randomUUID().toString());
        }

        Metadata metadata = roomService.getMetadata();
        roomService.createMetadata(metadata, roomCreateReq);
        micSeatService.createMetadata(metadata, roomCreateReq);
        songService.createMetadata(metadata, new SongAddReq());
        chorusService.createMetadata(metadata, new ChorusJoinReq());
        roomService.create(metadata, roomCreateReq);

        return R.success(new RoomDto().setRoomName(roomCreateReq.getRoomName())
                .setRoomId(roomCreateReq.getRoomId()));
    }

    /**
     * Destroy
     * 
     * @param roomDestroyReq
     * @return
     * @throws Exception
     */
    @PostMapping("/destroy")
    @ResponseBody
    public R<RoomDto> destroy(@Validated @RequestBody RoomDestroyReq roomDestroyReq) throws Exception {
        roomService.destroy(roomDestroyReq);
        return R.success(new RoomDto().setRoomId(roomDestroyReq.getRoomId()));
    }

    /**
     * Leave
     * 
     * @param roomLeaveReq
     * @return
     * @throws Exception
     */
    @PostMapping("/leave")
    @ResponseBody
    public R<RoomDto> leave(@Validated @RequestBody RoomLeaveReq roomLeaveReq) throws Exception {
        roomService.leave(roomLeaveReq);
        return R.success(new RoomDto().setRoomId(roomLeaveReq.getRoomId()));
    }

    /**
     * List
     * 
     * @param roomLeaveReq
     * @return
     * @throws Exception
     */
    @PostMapping("/list")
    @ResponseBody
    public R<RoomListDto<RoomListEntity>> list(@Validated @RequestBody RoomListReq roomListReq) throws Exception {
        if (roomListReq.getLastCreateTime() == null || roomListReq.getLastCreateTime() == 0) {
            roomListReq.setLastCreateTime(System.currentTimeMillis());
        }
        RoomListDto<RoomListEntity> roomList = roomService.getRoomList(roomListReq);
        return R.success(roomList);
    }

    /**
     * Query
     * 
     * @param roomLeaveReq
     * @return
     * @throws Exception
     */
    @PostMapping("/query")
    @ResponseBody
    public R<RoomQueryDto> query(@Validated @RequestBody RoomQueryReq roomQueryReq) throws Exception {
        RoomQueryDto roomQueryDto = roomService.query(roomQueryReq);
        return R.success(roomQueryDto);
    }
}