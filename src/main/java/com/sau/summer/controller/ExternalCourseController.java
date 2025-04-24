package com.sau.summer.controller;

import com.sau.summer.entity.ExternalCourse;
import com.sau.summer.entity.Student;
import com.sau.summer.entity.University;
import com.sau.summer.repository.ExternalCourseRepo;
import com.sau.summer.repository.UniversityRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/external")
public class ExternalCourseController {

    @Autowired
    private ExternalCourseRepo externalCourseRepo;
    @Autowired
    private UniversityRepo universityRepo;

    @GetMapping("/external-course-id")
    public ResponseEntity<Long> getExternalCourseId(final @RequestParam String universityName, final @RequestParam String facultyName,
                                                    final @RequestParam String departmentName, final @RequestParam String courseName) {
        Optional<Long> externalCourseId = externalCourseRepo.findExternalCourseId(universityName, facultyName, departmentName, courseName);

        return externalCourseId.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // Ders Adlarını Getiren Endpoint
    @GetMapping("/courses")
    public ResponseEntity<List<String>> getCoursesByUniversityDetails(final @RequestParam String universityName, final @RequestParam String facultyName,
                                                                      final @RequestParam String departmentName) {

        // University tablosunda sorgu yaparak universityId'yi bul
        Optional<University> optionalUniversity = universityRepo.findByUniversityNameAndFacultyNameAndDepartmentName(
                universityName, facultyName, departmentName
        );

        University university = optionalUniversity.orElseThrow(() ->
                new RuntimeException("Belirtilen üniversite, fakülte ve bölüm bilgileri bulunamadı."));

        if (university == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of("Üniversite bilgilerine uygun kayıt bulunamadı."));
        }

        // ExternalCourse tablosunda universityId ile sorgu yaparak courseName'leri al
        List<String> courseNames = externalCourseRepo.findCourseNamesByUniversityId(university.getUniversityId());

        if (courseNames.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of("Seçilen bilgilere uygun ders bulunamadı."));
        }

        return ResponseEntity.ok(courseNames);
    }

    @PostMapping("/add-course")
    public ResponseEntity<String> addCourse(final @RequestBody ExternalCourse externalCourse, final HttpSession session) {
        // Oturumdan student nesnesini al
        Student student = (Student) session.getAttribute("student");

        if (student == null) {
            session.invalidate(); // Eğer öğrenci bilgisi eksikse oturumu temizle
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Oturum süresi doldu veya öğrenci bulunamadı. Lütfen tekrar giriş yapın.");
        }

        // Üniversiteyi tablodan bul
        University university = universityRepo
                .findByUniversityNameAndFacultyNameAndDepartmentName(
                        externalCourse.getUniversity().getUniversityName(),
                        externalCourse.getUniversity().getFacultyName(),
                        externalCourse.getUniversity().getDepartmentName()
                ).orElseThrow(() -> new RuntimeException("Üniversite bilgileri bulunamadı."));

        // ExternalCourse'a University ID'yi set et
        externalCourse.setUniversity(university);
        externalCourse.setStudent(student);
        externalCourseRepo.save(externalCourse);

        return ResponseEntity.ok("Ders başarıyla eklendi!");
    }
}
