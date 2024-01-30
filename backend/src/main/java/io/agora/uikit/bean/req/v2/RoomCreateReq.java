package io.agora.uikit.bean.req.v2;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
@Accessors(chain = true)
public class RoomCreateReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    // Room payload
    private Map<String, Object> payload;
}
