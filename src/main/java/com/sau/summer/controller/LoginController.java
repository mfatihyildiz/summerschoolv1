package com.sau.summer.controller;

import com.sau.summer.entity.Committee;
import com.sau.summer.entity.Student;
import com.sau.summer.repository.StudentRepo;
import com.sau.summer.repository.CommitteeRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {

    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private CommitteeRepo committeeRepo;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam final String username, @RequestParam final String password, final HttpSession session, final Model model) {

        // Önce Komite Üyesi mi kontrol et
        Committee committee = committeeRepo.findByEmailAndPassword(username, password);
        if (committee != null) {
            session.setAttribute("committee", committee);
            session.setAttribute("role", "committee");
            return "redirect:/dashboard";
        }

        // Sonra Öğrenci mi kontrol et
        Student student = studentRepo.findByStudentNumberAndPassword(username, password);
        if (student != null) {
            session.setAttribute("student", student); // Öğrenci nesnesini oturuma ekledik
            session.setAttribute("role", "student");
            return "redirect:/dashboard";
        }

        model.addAttribute("errorMessage", "Geçersiz kullanıcı adı veya şifre");
        return "login";
    }
}
