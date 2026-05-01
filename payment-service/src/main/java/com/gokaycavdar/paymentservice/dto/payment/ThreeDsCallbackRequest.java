package com.gokaycavdar.paymentservice.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreeDsCallbackRequest {

    @NotBlank(message = "Conversation id is required")
    private String conversationId;

    @NotBlank(message = "Status is required")
    private String status;

    private String paymentId;
    private String conversationData;
    private String mdStatus;
    private String reason;
}
