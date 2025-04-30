package com.sau.summer.repository;

import com.sau.summer.entity.Committee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommitteeRepo extends JpaRepository<Committee, Long> {

    Committee findByUsername(String username);
}
