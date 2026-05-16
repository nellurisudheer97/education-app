package com.eduapp.controller;

import com.eduapp.dto.CourseDTO;
import com.eduapp.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<CourseDTO> createCourse(
            @RequestBody CourseDTO courseDTO,
            @RequestParam Long instructorId) {
        return ResponseEntity.ok(courseService.createCourse(courseDTO, instructorId));
    }
    
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllPublishedCourses() {
        return ResponseEntity.ok(courseService.getAllPublishedCourses());
    }
    
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getCourseById(courseId));
    }
    
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseDTO>> getInstructorCourses(@PathVariable Long instructorId) {
        return ResponseEntity.ok(courseService.getInstructorCourses(instructorId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CourseDTO>> getAssignedCourses(@PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.getAssignedCourses(studentId));
    }

    @PostMapping("/{courseId}/assign")
    public ResponseEntity<String> assignCourse(
            @PathVariable Long courseId,
            @RequestParam Long studentId) {
        courseService.assignCourse(courseId, studentId);
        return ResponseEntity.ok("Course assigned successfully");
    }

    @DeleteMapping("/{courseId}/assign")
    public ResponseEntity<String> unassignCourse(
            @PathVariable Long courseId,
            @RequestParam Long studentId) {
        courseService.unassignCourse(courseId, studentId);
        return ResponseEntity.ok("Course unassigned successfully");
    }
    
    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long courseId,
            @RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, courseDTO));
    }
    
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<String> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok("Course deleted successfully");
    }
}
