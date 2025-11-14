package com.roadwarnings.narino.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponseDTO {

    private String url;
    private String publicId;
    private String format;
    private Integer width;
    private Integer height;
    private Long bytes;
}
