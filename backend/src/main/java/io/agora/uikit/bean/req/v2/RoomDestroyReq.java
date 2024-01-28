package io.agora.uikit.bean.req.v2;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RoomDestroyReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;
}
