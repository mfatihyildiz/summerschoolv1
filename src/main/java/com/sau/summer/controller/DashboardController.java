package com.sau.summer.controller;

import com.sau.summer.entity.Committee;
import com.sau.summer.entity.Student;
import com.sau.summer.service.ApplicationPeriodService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    @Autowired
    private ApplicationPeriodService applicationPeriodService;

    // **Öğrenci ve komite erişimi kontrolü**
    private String checkStudentAndCommitteeAccess(final HttpSession session, final RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");

        if (role == null) {
            session.invalidate();
            redirectAttributes.addFlashAttribute("errorMessage", "Oturumunuz sona erdi, lütfen tekrar giriş yapın.");
            return "redirect:/login";
        }

        if (("student".equals(role) && session.getAttribute("student") != null) || ("committee".equals(role) && session.getAttribute("committee") != null)) {
            return null;
        }

        session.invalidate();
        redirectAttributes.addFlashAttribute("errorMessage", "Yetkisiz erişim! Lütfen tekrar giriş yapın.");
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(final HttpSession session, final Model model, final RedirectAttributes redirectAttributes) {
        String redirect = checkStudentAndCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        String role = (String) session.getAttribute("role");
        String fullName = "Bilinmeyen Kullanıcı";

        if ("student".equals(role)) {
            Student student = (Student) session.getAttribute("student");
            if (student != null) {
                fullName = student.getName() + " " + student.getSurname();
            }
            model.addAttribute("role", "student");
        } else if ("committee".equals(role)) {
            Committee committee = (Committee) session.getAttribute("committee");
            if (committee != null) {
                fullName = committee.getName() + " " + committee.getSurname();
            }
            model.addAttribute("role", "committee");
        }

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();

        model.addAttribute("fullName", fullName);
        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);

        return "dashboard";
    }
}
