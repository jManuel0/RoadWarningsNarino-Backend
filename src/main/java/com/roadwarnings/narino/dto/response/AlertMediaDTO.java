package com.roadwarnings.narino.dto.response;

import com.roadwarnings.narino.enums.MediaType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlertMediaDTO {

    private Long id;
    private String url;
    private MediaType type;
    private Integer position;
}

