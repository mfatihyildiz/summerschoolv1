package com.sau.summer.controller;

import com.sau.summer.service.ApplicationPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.sau.summer.entity.Committee;
import com.sau.summer.entity.Student;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    @Autowired
    private ApplicationPeriodService applicationPeriodService;

    private String checkStudentAndCommitteeAccess(final HttpSession session, final RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");

        if (role == null) {
            session.invalidate();
            redirectAttributes.addFlashAttribute("errorMessage", "Oturumunuz sona erdi, lütfen tekrar giriş yapın.");
            return "redirect:/login";
        }

        if (("STUDENT".equals(role) && session.getAttribute("student") != null) ||
                (("COMMITTEE".equals(role) || "ADMIN".equals(role)) && session.getAttribute("committee") != null)) {
            return null;
        }

        session.invalidate();
        redirectAttributes.addFlashAttribute("errorMessage", "Yetkisiz erişim! Lütfen tekrar giriş yapın.");
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String showProfile(final HttpSession session, final Model model, final RedirectAttributes redirectAttributes) {

        String redirect = checkStudentAndCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        Object user = session.getAttribute("student");
        if (user != null) {
            Student student = (Student) user;
            model.addAttribute("user", student);
            model.addAttribute("role", "STUDENT");
            return "profile";
        }

        user = session.getAttribute("committee");
        if (user != null) {
            Committee committee = (Committee) user;
            model.addAttribute("user", committee);
            model.addAttribute("role", "COMMITTEE");
            return "profile";
        }

        user = session.getAttribute("committee");
        if (user != null) {
            Committee committee = (Committee) user;
            model.addAttribute("user", committee);
            model.addAttribute("role", "ADMIN");
            return "profile";
        }

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();

        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);

        return "redirect:/login";
    }
}
