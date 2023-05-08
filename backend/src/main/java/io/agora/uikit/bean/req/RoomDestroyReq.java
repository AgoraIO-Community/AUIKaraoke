package io.agora.uikit.bean.req;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class RoomDestroyReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;
}
