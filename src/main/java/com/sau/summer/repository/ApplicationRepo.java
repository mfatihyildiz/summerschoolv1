package com.sau.summer.repository;

import com.sau.summer.entity.Application;
import com.sau.summer.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepo extends JpaRepository<Application, Long> {

    //Application tablosu içindeki student nesnesinin içindeki studentId alanına göre sorgu yapar
    List<Application> findByStudent_StudentId(Long studentId);

    List<Application> findByStatus(ApplicationStatus status);
}
