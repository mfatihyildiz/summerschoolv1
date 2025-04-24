package com.sau.summer.controller;

import com.sau.summer.entity.University;
import com.sau.summer.repository.UniversityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/university")
public class UniversityController {

    @Autowired
    private UniversityRepo universityRepo;

    @GetMapping("/universities")
    public List<String> getUniversities() {
        return universityRepo.findDistinctUniversityNames();
    }

    @GetMapping("/faculties")
    public List<String> getFaculties(final @RequestParam String universityName) {
        return universityRepo.findDistinctFacultyNamesByUniversityName(universityName);
    }

    @GetMapping("/departments")
    public List<String> getDepartments(final @RequestParam String universityName, final @RequestParam String facultyName) {
        return universityRepo.findDistinctDepartmentsByFacultyAndUniversity(universityName, facultyName);
    }

    @PostMapping("/add-university")
    public ResponseEntity<String> addUniversity(@RequestBody University university) {
        try {
            universityRepo.save(university);
            return ResponseEntity.ok("Üniversite eklendi!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ekleme hatası: " + e.getMessage());
        }
    }
}
