package com.sau.summer.controller;

import com.sau.summer.entity.*;
import com.sau.summer.enums.EducationYear;
import com.sau.summer.enums.Language;
import com.sau.summer.enums.Role;
import com.sau.summer.enums.Semester;
import com.sau.summer.repository.*;
import com.sau.summer.service.ApplicationPeriodService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private ApplicationPeriodRepo applicationPeriodRepo;
    @Autowired
    private ApplicationRepo applicationRepo;
    @Autowired
    private CommitteeRepo committeeRepo;
    @Autowired
    private ExternalCourseRepo externalCourseRepo;
    @Autowired
    private HomeCourseRepo homeCourseRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private UniversityRepo universityRepo;
    @Autowired
    private ApplicationPeriodService applicationPeriodService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    // **Komite erişimi kontrolü**
    private String checkCommitteeAccess(HttpSession session, RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");

        if (Role.COMMITTEE.name().equals(role) || Role.ADMIN.name().equals(role)) {
            Committee committee = (Committee) session.getAttribute("committee");
            if (committee != null) {
                return null; // Komite erişebilir, yönlendirme yok
            }
            session.invalidate();
            redirectAttributes.addFlashAttribute("errorMessage", "Komite üyesi bilgisi bulunamadı! Lütfen tekrar giriş yapın.");
            return "redirect:/login";
        }

        if (Role.STUDENT.name().equals(role)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Yetkisiz erişim! Komite yetkisi gereklidir.");
            return "redirect:/dashboard";
        }

        session.invalidate();
        redirectAttributes.addFlashAttribute("errorMessage", "Yetkisiz erişim! Lütfen tekrar giriş yapın.");
        return "redirect:/login";
    }

    // **Ayarlar sayfasını yükle**
    @GetMapping
    public String showSettingsPage(final Model model, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect; // ✅ Eğer yönlendirme gerekiyorsa, direkt yönlendir
        }

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();
        String role = (String) session.getAttribute("role");

        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);
        model.addAttribute("role", role);

        model.addAttribute("currentPeriod", applicationPeriodRepo.findTopByOrderByIdDesc().orElse(new ApplicationPeriod()));
        model.addAttribute("applications", applicationRepo.findAll());
        model.addAttribute("committees", committeeRepo.findAll());
        model.addAttribute("externalCourses", externalCourseRepo.findAll());
        model.addAttribute("homeCourses", homeCourseRepo.findAll());
        model.addAttribute("students", studentRepo.findAll());
        model.addAttribute("universities", universityRepo.findAll());
        model.addAttribute("applicationPeriods", applicationPeriodRepo.findAll());

        model.addAttribute("semesters", Arrays.asList(Semester.values()));
        model.addAttribute("educationYears", Arrays.asList(EducationYear.values()));
        model.addAttribute("languages", Arrays.asList(Language.values()));

        return "settings";
    }

    // **Yeni tarihleri kaydetme işlemi**
    @PostMapping("/update-period")
    public String updateApplicationPeriod(@RequestParam("previewStart") final String previewStart, @RequestParam("previewEnd") final String previewEnd,
                                          @RequestParam("applyStart") final String applyStart, @RequestParam("applyEnd") final String applyEnd,
                                          final HttpSession session, final Model model, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        try {
            ApplicationPeriod newPeriod = new ApplicationPeriod();
            newPeriod.setPreviewStartDate(LocalDateTime.parse(previewStart, formatter));
            newPeriod.setPreviewEndDate(LocalDateTime.parse(previewEnd, formatter));
            newPeriod.setApplicationStartDate(LocalDateTime.parse(applyStart, formatter));
            newPeriod.setApplicationEndDate(LocalDateTime.parse(applyEnd, formatter));

            applicationPeriodRepo.save(newPeriod);
            model.addAttribute("successMessage", "Yeni başvuru dönemi başarıyla eklendi!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Tarih formatı hatalı! Lütfen doğru formatta girin.");
        }
        return "redirect:/settings";
    }

    // **Silme İşlemleri (Sadece Komite Yetkisiyle)**
    @PostMapping("/delete-application")
    public String deleteApplication(@RequestParam("id") final Long id, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        applicationRepo.deleteById(id);
        return "redirect:/settings";
    }

    @PostMapping("/delete-committee")
    public String deleteCommittee(@RequestParam("id") final Long id, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        committeeRepo.deleteById(id);
        return "redirect:/settings";
    }

    @PostMapping("/delete-application-period")
    public String deleteApplicationPeriod(@RequestParam("id") final Long id, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        applicationPeriodRepo.deleteById(id);
        return "redirect:/settings";
    }

    @PostMapping("/delete-external-course")
    public String deleteExternalCourse(@RequestParam("id") final Long id, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        externalCourseRepo.deleteById(id);
        return "redirect:/settings";
    }

    @PostMapping("/delete-home-course")
    public String deleteHomeCourse(@RequestParam("id") final Long id, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        homeCourseRepo.deleteById(id);
        return "redirect:/settings";
    }

    @PostMapping("/delete-student")
    public String deleteStudent(@RequestParam("id") final Long id, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        studentRepo.deleteById(id);
        return "redirect:/settings";
    }

    @PostMapping("/delete-university")
    public String deleteUniversity(@RequestParam("id") final Long id, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        universityRepo.deleteById(id);
        return "redirect:/settings";
    }

    // **Ekleme İşlemleri (Sadece Komite Yetkisiyle)**
    @PostMapping("/add-committee")
    public String addCommittee(@RequestParam final String name, @RequestParam final String surname, @RequestParam final String email, @RequestParam final String password,
                               @RequestParam final String username, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        Committee committee = new Committee();
        committee.setName(name);
        committee.setSurname(surname);
        committee.setUsername(username);
        committee.setEmail(email);
        committee.setPassword(passwordEncoder.encode(password));
        committeeRepo.save(committee);
        return "redirect:/settings";
    }

    @PostMapping("/add-student")
    public String addStudent(@RequestParam final String studentNumber, @RequestParam final String name, @RequestParam final String email,
                             @RequestParam final String surname, @RequestParam final String password, @RequestParam final String year,
                             @RequestParam final Semester semester, @RequestParam final EducationYear educationYear, @RequestParam final double gpa,
                             final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        Student student = new Student();
        student.setStudentNumber(studentNumber);
        student.setUsername(studentNumber);
        student.setName(name);
        student.setSurname(surname);
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode(password));
        student.setSemester(semester);
        student.setEducationYear(educationYear);
        student.setYear(year);
        student.setGpa(gpa);
        studentRepo.save(student);
        return "redirect:/settings";
    }

    @PostMapping("/add-home-course")
    public String addHomeCourse(@RequestParam final String courseName, @RequestParam final int ects, @RequestParam final Language language,
                                @RequestParam final int theoreticalHours, @RequestParam final int practicalHours, @RequestParam final Semester semester,
                                @RequestParam final EducationYear educationYear, @RequestParam final String description,
                                final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) {
            return redirect;
        }

        HomeCourse course = new HomeCourse();
        course.setCourseName(courseName);
        course.setEcts(ects);
        course.setLanguage(language);
        course.setTheoreticalHours(theoreticalHours);
        course.setPracticalHours(practicalHours);
        course.setSemester(semester);
        course.setEducationYear(educationYear);
        course.setDescription(description);
        homeCourseRepo.save(course);
        return "redirect:/settings";
    }

    @PostMapping("/lock-student")
    public String lockOrUnlockStudentfinal (@RequestParam("id") Long studentId, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Yetkisiz erişim!");
            return "redirect:/dashboard";
        }

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı."));

        student.setLocked(!student.isLocked()); // toggle işlemi
        studentRepo.save(student);
        return "redirect:/settings";
    }

    @PostMapping("/disable-student")
    public String disableOrEnableStudent(final @RequestParam("id") Long studentId, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Yetkisiz erişim!");
            return "redirect:/dashboard";
        }

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Öğrenci bulunamadı."));

        student.setDisabled(!student.isDisabled()); // toggle işlemi
        studentRepo.save(student);
        return "redirect:/settings";
    }
}