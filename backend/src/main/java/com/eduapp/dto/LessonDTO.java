package com.eduapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDTO {
    private Long id;
    private String title;
    private String content;
    private String videoUrl;
    private String resourceUrl;
    private Long courseId;
    private Integer orderIndex;
}
