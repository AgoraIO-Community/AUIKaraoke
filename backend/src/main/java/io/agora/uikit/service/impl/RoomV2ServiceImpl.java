package io.agora.uikit.service.impl;

import io.agora.uikit.bean.dto.v2.RoomCreateDto;
import io.agora.uikit.bean.dto.v2.RoomListDto;
import io.agora.uikit.bean.dto.v2.RoomQueryDto;
import io.agora.uikit.bean.entity.RoomListV2Entity;
import io.agora.uikit.bean.enums.ReturnCodeEnum;
import io.agora.uikit.bean.exception.BusinessException;
import io.agora.uikit.bean.req.v2.*;
import io.agora.uikit.repository.RoomListV2Repository;
import io.agora.uikit.service.IRoomV2Service;
import io.agora.uikit.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@Service
public class RoomV2ServiceImpl implements IRoomV2Service {
    @Resource
    private RedisUtil redisUtil;

    @Resource
    @Lazy()
    private RoomListV2Repository roomListV2Repository;

    private static final long TRY_LOCK_TIMEOUT_SECOND = 10;

    @Override
    public void acquireLock(String roomId) throws Exception {
        String lockName = getLockName(roomId);
        log.info("acquireLock, roomId:{}, lockName:{}", roomId, lockName);

        if (!redisUtil.tryLock(lockName, TRY_LOCK_TIMEOUT_SECOND)) {
            log.error("acquireLock, failed, roomId:{}, lockName:{}, TRY_LOCK_TIMEOUT_SECOND:{}", roomId, lockName,
                    TRY_LOCK_TIMEOUT_SECOND);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_ACQUIRE_LOCK_ERROR);
        }
    }

    @Override
    public void addRoomList(AddRoomReq addRoomReq) {
        var roomListEntity = new RoomListV2Entity()
                .setRoomId(addRoomReq.getRoomId())
                .setPayload(addRoomReq.getPayload())
                .setUpdateTime(System.currentTimeMillis())
                .setCreateTime(System.currentTimeMillis());
        // Insert data
        roomListV2Repository.insert(roomListEntity);
    }

    @Override
    public void update(RoomUpdateReq roomUpdateReq) throws Exception {
        acquireLock(roomUpdateReq.getRoomId());
        Optional<RoomListV2Entity> opt = roomListV2Repository.findById(roomUpdateReq.getRoomId());
        if (opt.isEmpty()) {
            releaseLock(roomUpdateReq.getRoomId());
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_NOT_EXISTS_ERROR);
        }

        RoomListV2Entity roomListEntity = opt.get();
        roomListEntity.setPayload(roomUpdateReq.getPayload());
        roomListEntity.setUpdateTime(System.currentTimeMillis());
        roomListV2Repository.save(roomListEntity);
        releaseLock(roomUpdateReq.getRoomId());
        log.info("update, success, roomUpdateReq:{}", roomUpdateReq);
    }

    @Override
    public RoomCreateDto create(RoomCreateReq roomCreateReq) throws Exception {
        acquireLock(roomCreateReq.getRoomId());
        Optional<RoomListV2Entity> opt = roomListV2Repository.findById(roomCreateReq.getRoomId());
        if (opt.isPresent()) {
            releaseLock(roomCreateReq.getRoomId());
            return new RoomCreateDto()
                    .setRoomId(opt.get().getRoomId())
                    .setPayload(opt.get().getPayload())
                    .setUpdateTime(opt.get().getUpdateTime())
                    .setCreateTime(opt.get().getCreateTime());
        }
        long currentTime = System.currentTimeMillis();
        addRoomList(new AddRoomReq()
                .setRoomId(roomCreateReq.getRoomId())
                .setPayload(roomCreateReq.getPayload())
                .setUpdateTime(currentTime)
                .setCreateTime(currentTime));

        log.info("create, success, roomCreateReq:{}", roomCreateReq);
        releaseLock(roomCreateReq.getRoomId());
        return new RoomCreateDto()
                .setRoomId(roomCreateReq.getRoomId())
                .setPayload(roomCreateReq.getPayload())
                .setUpdateTime(currentTime)
                .setCreateTime(currentTime);
    }

    @Override
    public void destroy(RoomDestroyReq roomDestroyReq) throws Exception {
        removeRoomList(roomDestroyReq);
        log.info("destroy, success, roomDestroyReq:{}", roomDestroyReq);
    }

    @Override
    public String getLockName(String lockName) {
        return DigestUtils.md5DigestAsHex((lockName).getBytes());
    }


    @Override
    public RoomListDto<RoomListV2Entity> getRoomList(RoomListReq roomListReq) {
        log.info("getRoomList, roomListReq:{}", roomListReq);

        Pageable pageable = PageRequest.of(0, roomListReq.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));
        Page<RoomListV2Entity> roomList = roomListV2Repository.findByCreateTimeLessThan(roomListReq.getLastCreateTime(),
                pageable);

        return new RoomListDto<RoomListV2Entity>()
                .setPageSize(roomListReq.getPageSize())
                .setCount(roomList.getNumberOfElements())
                .setList(roomList.getContent());
    }


    @Override
    public RoomQueryDto query(RoomQueryReq roomQueryReq) throws Exception {
        Optional<RoomListV2Entity> opt = roomListV2Repository.findById(roomQueryReq.getRoomId());
        if (opt.isEmpty()) {
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_NOT_EXISTS_ERROR);
        }
        RoomListV2Entity roomListEntity = opt.get();
        return new RoomQueryDto()
                .setCreateTime(roomListEntity.getCreateTime())
                .setUpdateTime(roomListEntity.getUpdateTime())
                .setRoomId(roomListEntity.getRoomId())
                .setPayload(roomListEntity.getPayload());
    }

    @Override
    public void releaseLock(String roomId) throws Exception {
        String lockName = getLockName(roomId);
        log.info("releaseLock, roomId:{}, lockName:{}", roomId, lockName);

        if (!redisUtil.unlock(lockName)) {
            log.error("releaseLock, failed, roomId:{}, lockName:{}", roomId, lockName);
            throw new BusinessException(HttpStatus.OK.value(), ReturnCodeEnum.ROOM_RELEASE_LOCK_ERROR);
        }
    }

    @Override
    public void removeRoomList(RoomDestroyReq roomDestroyReq) {
        roomListV2Repository.deleteById(roomDestroyReq.getRoomId());
        log.info("removeRoomList, roomDestroyReq:{}", roomDestroyReq);
    }
}
