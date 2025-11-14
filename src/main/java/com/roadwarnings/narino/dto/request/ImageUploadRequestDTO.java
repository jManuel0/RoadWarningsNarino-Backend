package com.roadwarnings.narino.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequestDTO {

    private String base64Image;
    private String fileName;
    private String contentType;
}
