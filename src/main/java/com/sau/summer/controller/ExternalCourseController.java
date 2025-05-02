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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Optional<Long> externalCourseId = externalCourseRepo.findActiveExternalCourseId(universityName, facultyName, departmentName, courseName);

        return externalCourseId.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // Ders Adlarını Getiren Endpoint
    @GetMapping("/courses")
    public ResponseEntity<List<String>> getCoursesByUniversityDetails(final @RequestParam String universityName, final @RequestParam String facultyName,
                                                                      final @RequestParam String departmentName) {

        Optional<University> optionalUniversity = universityRepo.findByUniversityNameAndFacultyNameAndDepartmentName(
                universityName, facultyName, departmentName
        );

        University university = optionalUniversity.orElseThrow(() ->
                new RuntimeException("Belirtilen üniversite, fakülte ve bölüm bilgileri bulunamadı."));

        List<String> courseNames = externalCourseRepo.findActiveCourseNamesByUniversityId(university.getUniversityId());

        if (courseNames.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of("Seçilen bilgilere uygun aktif ders bulunamadı."));
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

    @GetMapping("/check-duplicate-course")
    @ResponseBody
    public boolean checkDuplicateExternalCourse(final @RequestParam String universityName, final @RequestParam String facultyName,
            final @RequestParam String departmentName, final @RequestParam String courseName) {

        // Önce üniversiteyi bul
        Optional<University> universityOpt = universityRepo.findByUniversityNameAndFacultyNameAndDepartmentName(
                universityName, facultyName, departmentName
        );

        if (universityOpt.isEmpty()) {
            return false; // Üniversite yoksa zaten ders de yok demektir
        }

        University university = universityOpt.get();

        // Sonra aynı university_id ve course_name olan external course var mı kontrol et
        Optional<ExternalCourse> externalCourseOpt = externalCourseRepo.findByUniversityIdAndCourseName(
                university.getUniversityId(), courseName
        );

        return externalCourseOpt.isPresent(); // true: zaten var, false: eklenebilir
    }

    @PutMapping("/deactivate-course")
    @ResponseBody
    public String deactivateCourse(@RequestParam Long universityId, @RequestParam String courseName) {
        Optional<ExternalCourse> courseOpt = externalCourseRepo.findByUniversityIdAndCourseName(universityId, courseName);

        if (courseOpt.isPresent()) {
            ExternalCourse course = courseOpt.get();
            course.setIsActive(false);
            externalCourseRepo.save(course);
            return "Başarılı";
        } else {
            return "Hata: Ders bulunamadı.";
        }
    }

    @GetMapping("/get-course-details")
    @ResponseBody
    public Map<String, Object> getCourseDetails(@RequestParam Long id) {
        ExternalCourse course = externalCourseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("External Course bulunamadı."));

        Map<String, Object> response = new HashMap<>();
        response.put("id", course.getExternalCourseId());
        response.put("courseName", course.getCourseName());
        response.put("universityName", course.getUniversity().getUniversityName());
        response.put("facultyName", course.getUniversity().getFacultyName());
        response.put("departmentName", course.getUniversity().getDepartmentName());
        response.put("ects", course.getEcts());
        response.put("language", course.getLanguage());
        response.put("theoreticalHours", course.getTheoreticalHours());
        response.put("practicalHours", course.getPracticalHours());
        response.put("description", course.getDescription());

        return response;
    }
}
