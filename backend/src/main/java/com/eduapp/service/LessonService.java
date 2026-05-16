package com.eduapp.service;

import com.eduapp.dto.LessonDTO;
import com.eduapp.model.Course;
import com.eduapp.model.Lesson;
import com.eduapp.model.User;
import com.eduapp.model.UserRole;
import com.eduapp.repository.CourseRepository;
import com.eduapp.repository.LessonRepository;
import com.eduapp.repository.UserProgressRepository;
import com.eduapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonService {
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;
    
    public LessonDTO createLesson(LessonDTO lessonDTO) {
        Course course = courseRepository.findById(lessonDTO.getCourseId())
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        Lesson lesson = new Lesson();
        lesson.setTitle(lessonDTO.getTitle());
        lesson.setContent(lessonDTO.getContent());
        lesson.setVideoUrl(lessonDTO.getVideoUrl());
        lesson.setResourceUrl(lessonDTO.getResourceUrl());
        lesson.setCourse(course);
        lesson.setOrderIndex(lessonDTO.getOrderIndex());
        
        Lesson savedLesson = lessonRepository.save(lesson);
        
        // Update course total lessons
        int totalLessons = lessonRepository.findByCourseId(course.getId()).size();
        course.setTotalLessons(totalLessons);
        courseRepository.save(course);
        
        return convertToDTO(savedLesson);
    }
    
    public List<LessonDTO> getLessonsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        ensureCurrentUserCanViewCourse(course);

        return lessonRepository.findByCourseId(courseId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public LessonDTO getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
        ensureCurrentUserCanViewCourse(lesson.getCourse());
        return convertToDTO(lesson);
    }
    
    public LessonDTO updateLesson(Long lessonId, LessonDTO lessonDTO) {
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        lesson.setTitle(lessonDTO.getTitle());
        lesson.setContent(lessonDTO.getContent());
        lesson.setVideoUrl(lessonDTO.getVideoUrl());
        lesson.setResourceUrl(lessonDTO.getResourceUrl());
        lesson.setOrderIndex(lessonDTO.getOrderIndex());
        lesson.setUpdatedAt(LocalDateTime.now());
        
        Lesson updatedLesson = lessonRepository.save(lesson);
        return convertToDTO(updatedLesson);
    }
    
    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }
    
    private LessonDTO convertToDTO(Lesson lesson) {
        return new LessonDTO(
            lesson.getId(),
            lesson.getTitle(),
            lesson.getContent(),
            lesson.getVideoUrl(),
            lesson.getResourceUrl(),
            lesson.getCourse().getId(),
            lesson.getOrderIndex()
        );
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        return userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private void ensureCurrentUserCanViewCourse(Course course) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == UserRole.ADMIN || course.getInstructor().getId().equals(currentUser.getId())) {
            return;
        }
        if (!userProgressRepository.existsByUserIdAndCourseId(currentUser.getId(), course.getId())) {
            throw new RuntimeException("This course is not assigned to you");
        }
    }
}
