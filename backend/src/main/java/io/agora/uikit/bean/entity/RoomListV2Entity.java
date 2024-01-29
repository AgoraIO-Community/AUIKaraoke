package io.agora.uikit.bean.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Map;

@Data
@Accessors(chain = true)
@Document(collection = "room_list_v2")
public class RoomListV2Entity {
    // Room id
    @MongoId
    private String roomId;

    // payload
    private Map<String, Object> payload;

    // update time
    private Long updateTime;
    // Create time
    private Long createTime;
}
