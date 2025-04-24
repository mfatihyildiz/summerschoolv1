package com.sau.summer.repository;

import com.sau.summer.entity.HomeCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeCourseRepo extends JpaRepository<HomeCourse, Long> {
}
