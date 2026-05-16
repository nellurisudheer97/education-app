package com.eduapp.service;

import com.eduapp.dto.CourseDTO;
import com.eduapp.model.Course;
import com.eduapp.model.User;
import com.eduapp.model.UserProgress;
import com.eduapp.model.UserRole;
import com.eduapp.repository.CourseRepository;
import com.eduapp.repository.UserProgressRepository;
import com.eduapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;
    
    public CourseDTO createCourse(CourseDTO courseDTO, Long instructorId) {
        User instructor = userRepository.findById(instructorId)
            .orElseThrow(() -> new RuntimeException("Instructor not found"));
        if (instructor.getRole() == UserRole.STUDENT) {
            throw new RuntimeException("Only admins and instructors can own courses");
        }
        ensureCurrentUserCanManageInstructor(instructor);
        
        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setThumbnailUrl(courseDTO.getThumbnailUrl());
        course.setInstructor(instructor);
        course.setIsPublished(false);
        
        Course savedCourse = courseRepository.save(course);
        return convertToDTO(savedCourse);
    }
    
    public CourseDTO updateCourse(Long courseId, CourseDTO courseDTO) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setThumbnailUrl(courseDTO.getThumbnailUrl());
        course.setIsPublished(courseDTO.getIsPublished());
        course.setUpdatedAt(LocalDateTime.now());
        
        Course updatedCourse = courseRepository.save(course);
        return convertToDTO(updatedCourse);
    }
    
    public List<CourseDTO> getAllPublishedCourses() {
        return courseRepository.findByIsPublishedTrue()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<CourseDTO> getAssignedCourses(Long studentId) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        ensureCurrentUserCanViewStudent(student);

        return userProgressRepository.findByUserId(studentId)
            .stream()
            .map(UserProgress::getCourse)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<CourseDTO> getInstructorCourses(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
            .orElseThrow(() -> new RuntimeException("Instructor not found"));
        ensureCurrentUserCanManageInstructor(instructor);

        return courseRepository.findByInstructorId(instructorId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public CourseDTO getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        ensureCurrentUserCanViewCourse(course);
        return convertToDTO(course);
    }

    public void assignCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        ensureCurrentUserCanManageCourse(course);
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getRole() != UserRole.STUDENT) {
            throw new RuntimeException("Courses can only be assigned to students");
        }

        if (userProgressRepository.existsByUserIdAndCourseId(studentId, courseId)) {
            return;
        }

        UserProgress progress = new UserProgress();
        progress.setUser(student);
        progress.setCourse(course);
        progress.setTotalLessons(course.getTotalLessons());
        userProgressRepository.save(progress);
    }

    @Transactional
    public void unassignCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        ensureCurrentUserCanManageCourse(course);
        userProgressRepository.deleteByUserIdAndCourseId(studentId, courseId);
    }
    
    public void deleteCourse(Long courseId) {
        courseRepository.deleteById(courseId);
    }
    
    private CourseDTO convertToDTO(Course course) {
        return new CourseDTO(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            course.getThumbnailUrl(),
            course.getInstructor().getId(),
            course.getInstructor().getFullName(),
            course.getIsPublished(),
            course.getTotalLessons()
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

    private void ensureCurrentUserCanViewStudent(User student) {
        User currentUser = getCurrentUser();
        if (isStaff(currentUser)) {
            return;
        }
        if (!currentUser.getId().equals(student.getId())) {
            throw new RuntimeException("You cannot view another student's courses");
        }
    }

    private void ensureCurrentUserCanManageInstructor(User instructor) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        if (!currentUser.getId().equals(instructor.getId())) {
            throw new RuntimeException("You can only manage your own instructor workspace");
        }
    }

    private void ensureCurrentUserCanManageCourse(Course course) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        if (!course.getInstructor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only manage your own courses");
        }
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

    private boolean isStaff(User user) {
        return user.getRole() == UserRole.ADMIN
            || user.getRole() == UserRole.INSTRUCTOR
            || user.getRole() == UserRole.DEVELOPER;
    }
}
