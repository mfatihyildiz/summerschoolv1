package com.sau.summer.repository;

import com.sau.summer.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepo extends JpaRepository<Student, Long> {

    Student findByStudentNumberAndPassword(String studentNumber, String password);

    Student findByStudentNumber(String studentNumber);
}

