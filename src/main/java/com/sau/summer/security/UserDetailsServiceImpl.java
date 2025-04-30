package com.sau.summer.security;

import com.sau.summer.entity.Student;
import com.sau.summer.entity.Committee;
import com.sau.summer.repository.StudentRepo;
import com.sau.summer.repository.CommitteeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final StudentRepo studentRepo;
    private final CommitteeRepo committeeRepo;

    @Autowired
    public UserDetailsServiceImpl(StudentRepo studentRepo, CommitteeRepo committeeRepo) {
        this.studentRepo = studentRepo;
        this.committeeRepo = committeeRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Önce öğrenciler arasında ara
        Student student = studentRepo.findByUsername(username);
        if (student != null) {
            return new UserDetailsImpl(
                    student.getStudentId(),
                    student.getUsername(),
                    student.getPassword(),
                    student.getRole().name(),
                    !student.isLocked(),
                    !student.isDisabled()
            );
        }

        // Sonra komite üyeleri arasında ara
        Committee committee = committeeRepo.findByUsername(username);
        if (committee != null) {
            return new UserDetailsImpl(
                    committee.getCommitteeId(),
                    committee.getUsername(),
                    committee.getPassword(),
                    committee.getRole().name(),
                    !committee.isLocked(),
                    !committee.isDisabled()
            );
        }

        // Eğer hiçbiri bulunamazsa hata fırlat
        throw new UsernameNotFoundException("Kullanıcı bulunamadı: " + username);
    }
}
