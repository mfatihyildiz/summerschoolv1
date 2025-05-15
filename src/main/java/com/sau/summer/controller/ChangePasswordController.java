package com.sau.summer.controller;

import com.sau.summer.entity.Committee;
import com.sau.summer.entity.Student;
import com.sau.summer.repository.CommitteeRepo;
import com.sau.summer.repository.StudentRepo;
import com.sau.summer.service.ApplicationPeriodService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ChangePasswordController {

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private CommitteeRepo committeeRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @GetMapping("/change-password")
    public String showForm(final HttpSession session, final RedirectAttributes redirectAttributes, Model model, Principal principal) {
        String redirect = checkStudentAndCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        String username = principal.getName();
        Student student = studentRepo.findByUsername(username);
        if (student != null) {
            model.addAttribute("user", student);
        } else {
            Committee committee = committeeRepo.findByUsername(username);
            if (committee != null) {
                model.addAttribute("user", committee);
            }
        }

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();
        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);
        model.addAttribute("role", session.getAttribute("role"));

        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(final @RequestParam String currentPassword, final @RequestParam String newPassword,
                                 final @RequestParam String confirmPassword, final Principal principal, final Model model,
                                 final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkStudentAndCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        String username = principal.getName();

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Yeni şifreler uyuşmuyor.");
            return "change-password";
        }

        Student student = studentRepo.findByUsername(username);
        if (student != null) {
            if (!passwordEncoder.matches(currentPassword, student.getPassword())) {
                model.addAttribute("errorMessage", "Mevcut şifre yanlış.");
                return "change-password";
            }

            student.setPassword(passwordEncoder.encode(newPassword));
            studentRepo.save(student);
            model.addAttribute("successMessage", "Şifre başarıyla güncellendi.");
            return "login";
        }

        Committee committee = committeeRepo.findByUsername(username);
        if (committee != null) {
            if (!passwordEncoder.matches(currentPassword, committee.getPassword())) {
                model.addAttribute("errorMessage", "Mevcut şifre yanlış.");
                return "change-password";
            }

            committee.setPassword(passwordEncoder.encode(newPassword));
            committeeRepo.save(committee);
            model.addAttribute("successMessage", "Şifre başarıyla güncellendi.");
            return "login";
        }

        model.addAttribute("errorMessage", "Kullanıcı bulunamadı.");

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();

        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);

        return "change-password";
    }
}


