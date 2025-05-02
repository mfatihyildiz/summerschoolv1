package com.sau.summer.controller;

import com.sau.summer.entity.*;
import com.sau.summer.enums.ApplicationStatus;
import com.sau.summer.enums.EducationYear;
import com.sau.summer.repository.*;
import com.sau.summer.service.ApplicationPeriodService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationRepo applicationRepo;
    @Autowired
    private HomeCourseRepo homeCourseRepo;
    @Autowired
    private ExternalCourseRepo externalCourseRepo;
    @Autowired
    private UniversityRepo universityRepo;
    @Autowired
    private ApplicationPeriodService applicationPeriodService;

    private String checkStudentAccess(final HttpSession session, final RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");

        if (role == null) {
            session.invalidate();
            redirectAttributes.addFlashAttribute("errorMessage", "Oturumunuz sona erdi, lütfen tekrar giriş yapın.");
            return "redirect:/login";
        }

        if (("STUDENT".equals(role) && session.getAttribute("student") != null) || "ADMIN".equals(role)) {
            return null; // Öğrenci ya da Admin ise erişim serbest
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Yetkisiz erişim! Sadece öğrenciler erişebilir.");
        return "redirect:/dashboard";
    }

    private String checkCommitteeAccess(final HttpSession session, final RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");

        if (role == null) {
            session.invalidate();
            redirectAttributes.addFlashAttribute("errorMessage", "Oturumunuz sona erdi, lütfen tekrar giriş yapın.");
            return "redirect:/login";
        }

        if (("COMMITTEE".equals(role) && session.getAttribute("committee") != null) || "ADMIN".equals(role)) {
            return null; // Komite ya da Admin ise erişim serbest
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Yetkisiz erişim! Sadece komite üyeleri erişebilir.");
        return "redirect:/dashboard";
    }

    @GetMapping("/new")
    public String showNewApplicationPage(final Model model, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkStudentAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        Student student = (Student) session.getAttribute("student");
        String role = (String) session.getAttribute("role");

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();

        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);
        model.addAttribute("role", role);

        if (!isPreviewMode && !isApplicationPeriod) {
            model.addAttribute("showErrorPopup", true);
            model.addAttribute("errorMessage", "Başvurular şu anda kapalıdır. Lütfen belirlenen tarihlerde tekrar deneyin.");
            return "redirect:/dashboard";
        }

        // Öğrencinin eğitim yılı ve not ortalamasını al
        EducationYear studentYear = student.getEducationYear();
        double gpa = student.getGpa();

        final EducationYear maxAllowedYear;
        if (gpa >= 3.00) {
            maxAllowedYear = EducationYear.values()[Math.min(studentYear.ordinal() + 1, EducationYear.values().length - 1)];
        } else {
            maxAllowedYear = studentYear;
        }

        List<HomeCourse> eligibleCourses = homeCourseRepo.findAll().stream()
                .filter(course -> course.getEducationYear().ordinal() <= maxAllowedYear.ordinal())
                .toList();

        if (eligibleCourses.isEmpty()) {
            model.addAttribute("errorMessage", "Seçebileceğiniz uygun ders bulunmamaktadır.");
        } else {
            model.addAttribute("homeCourses", eligibleCourses);
        }

        return "new-application";
    }

    @PostMapping("/step2")
    public String proceedToNextStep(final @RequestParam Long selectedCourseId, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkStudentAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        session.setAttribute("selectedHomeCourseId", selectedCourseId);
        return "redirect:/applications/new/2";
    }

    @GetMapping("/new/2")
    public String showNewApplication2Page(final HttpSession session, final Model model, final RedirectAttributes redirectAttributes) {
        String redirect = checkStudentAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        String role = (String) session.getAttribute("role");
        Long homeCourseId = (Long) session.getAttribute("selectedHomeCourseId");

        if (homeCourseId == null) {
            model.addAttribute("errorMessage", "HomeCourse ID bulunamadı. Lütfen tekrar başlayın.");
            return "redirect:/dashboard";
        }

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();

        if (!isPreviewMode && !isApplicationPeriod) {
            model.addAttribute("showErrorPopup", true);
            model.addAttribute("errorMessage", "Başvurular şu anda kapalıdır. Lütfen belirlenen tarihlerde tekrar deneyin.");
            return "redirect:/dashboard";
        }

        List<String> universityNames = universityRepo.findDistinctUniversityNames();
        model.addAttribute("universityNames", universityNames);
        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);
        model.addAttribute("role", role);
        return "new-application2";
    }

    @GetMapping("/external-course-id")
    @ResponseBody
    public Object getExternalCourseId(@RequestParam final String universityName, @RequestParam final String facultyName, @RequestParam final String departmentName,
                                      @RequestParam final String courseName, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkStudentAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        try {
            // 1. Adım: UniversityId'yi al
            Long universityId = universityRepo
                    .findByUniversityNameAndFacultyNameAndDepartmentName(universityName, facultyName, departmentName)
                    .map(University::getUniversityId)
                    .orElseThrow(() -> new RuntimeException("University ID bulunamadı."));

            // 2. Adım: Aktif ExternalCourseId'yi al
            return externalCourseRepo
                    .findByUniversity_UniversityIdAndCourseNameAndIsActiveTrue(universityId, courseName)
                    .map(ExternalCourse::getExternalCourseId)
                    .orElseThrow(() -> new RuntimeException("Aktif ExternalCourse ID bulunamadı."));
        } catch (RuntimeException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @PostMapping("/set-external-course")
    public String setExternalCourseId(@RequestParam final Long externalCourseId, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkStudentAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        session.setAttribute("selectedExternalCourseId", externalCourseId);
        return "redirect:/applications/confirmation";
    }

    @GetMapping("/confirmation")
    public String showConfirmationPage(final HttpSession session, final Model model, final RedirectAttributes redirectAttributes) {
        String redirect = checkStudentAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        Student student = (Student) session.getAttribute("student");
        String role = (String) session.getAttribute("role");

        // Session'dan ID'leri al
        Long homeCourseId = (Long) session.getAttribute("selectedHomeCourseId");
        Long externalCourseId = (Long) session.getAttribute("selectedExternalCourseId");

        if (homeCourseId == null || externalCourseId == null) {
            model.addAttribute("errorMessage", "Gerekli veriler eksik. Lütfen işlemi tekrar başlatın.");
            return "confirmation";  // Hata mesajıyla birlikte sayfayı göster
        }

        // HomeCourse ve ExternalCourse verilerini çek
        HomeCourse homeCourse = homeCourseRepo.findById(homeCourseId)
                .orElseThrow(() -> new RuntimeException("Seçilen HomeCourse sistemde bulunamadı."));
        ExternalCourse externalCourse = externalCourseRepo.findById(externalCourseId)
                .orElseThrow(() -> new RuntimeException("Seçilen ExternalCourse sistemde bulunamadı."));

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();

        if (!isPreviewMode && !isApplicationPeriod) {
            model.addAttribute("showErrorPopup", true);
            model.addAttribute("errorMessage", "Başvurular şu anda kapalıdır. Lütfen belirlenen tarihlerde tekrar deneyin.");
            return "redirect:/dashboard";
        }

        // Model'e verileri ekle
        model.addAttribute("homeCourse", homeCourse);
        model.addAttribute("externalCourse", externalCourse);
        model.addAttribute("student", student);
        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);
        model.addAttribute("role", role);

        return "confirmation";
    }

    @PostMapping("/submit")
    public String submitApplication(@RequestParam final Long homeCourseId, @RequestParam final Long externalCourseId, final Model model, final HttpSession session,
                                    final RedirectAttributes redirectAttributes) {
        String redirect = checkStudentAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        Student student = (Student) session.getAttribute("student");

        // Veritabanından HomeCourse, ExternalCourse nesneleri alınıyor
        HomeCourse homeCourse = homeCourseRepo.findById(homeCourseId)
                .orElseThrow(() -> new RuntimeException("HomeCourse bulunamadı."));
        ExternalCourse externalCourse = externalCourseRepo.findById(externalCourseId)
                .orElseThrow(() -> new RuntimeException("ExternalCourse bulunamadı."));

        // AKTS kontrolü
        if (externalCourse.getEcts() < homeCourse.getEcts()) {
            model.addAttribute("errorMessage", "Dışarıdan alınacak dersin AKTS değeri, seçilen HomeCourse'un AKTS değerine eşit veya daha büyük olmalıdır.");
            return "confirmation";
        }

        // Dil kontrolü
        if (!externalCourse.getLanguage().equals(homeCourse.getLanguage())) {
            model.addAttribute("errorMessage", "Seçilen derslerin eğitim dili aynı olmalıdır!");
            return "confirmation";
        }

        // Yeni bir Application oluştur
        Application application = new Application();
        application.setHomeCourse(homeCourse);
        application.setExternalCourse(externalCourse);
        application.setStudent(student);
        application.setStatus(ApplicationStatus.PENDING);
        application.setSubmissionDate(LocalDate.now());

        // Application'ı kaydet
        applicationRepo.save(application);

        model.addAttribute("message", "Başvuru başarıyla oluşturuldu!");
        return "redirect:/dashboard";
    }

    @GetMapping("/my-applications")
    public String getMyApplications(final HttpSession session, final Model model, final RedirectAttributes redirectAttributes) {
        String redirect = checkStudentAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        Student student = (Student) session.getAttribute("student");

        String role = (String) session.getAttribute("role");

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();

        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);
        model.addAttribute("role", role);

        List<Application> applicationsList = applicationRepo.findByStudent_StudentId(student.getStudentId());
        model.addAttribute("applications", applicationsList);
        model.addAttribute("fullName", student.getName() + " " + student.getSurname());

        return "applicationPages/my-applications"; // Öğrenci başvurularını ayrı bir sayfada göster
    }

    @GetMapping("/pending")
    public String getPendingApplications(final Model model, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        String role = (String) session.getAttribute("role");

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();

        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);
        model.addAttribute("role", role);

        // PENDING statüsündeki başvuruları çek
        List<Application> pendingApplications = applicationRepo.findByStatus(ApplicationStatus.PENDING);
        model.addAttribute("pendingApplications", pendingApplications);

        return "applicationPages/pending-applications"; // Bekleyen başvurular sayfasına yönlendirme
    }

    @GetMapping("/approved")
    public String approvedApplications(final Model model, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        String role = (String) session.getAttribute("role");

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();

        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);
        model.addAttribute("role", role);

        List<Application> approvedApplications = applicationRepo.findByStatus(ApplicationStatus.APPROVED);
        model.addAttribute("approvedApplications", approvedApplications);
        return "applicationPages/approved-applications";
    }

    @PostMapping("/approve/{id}")
    public String approveApplication(@PathVariable Long id, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        Application application = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Başvuru bulunamadı!"));

        application.setStatus(ApplicationStatus.APPROVED);
        applicationRepo.save(application);

        return "redirect:/applications/pending"; // Sayfayı yenile
    }

    @GetMapping("/rejected")
    public String rejectedApplications(final Model model, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        String role = (String) session.getAttribute("role");

        boolean isPreviewMode = applicationPeriodService.isPreviewOpen();
        boolean isApplicationPeriod = applicationPeriodService.isApplicationOpen();

        model.addAttribute("isPreviewMode", isPreviewMode);
        model.addAttribute("isApplicationPeriod", isApplicationPeriod);
        model.addAttribute("role", role);

        List<Application> rejectedApplications = applicationRepo.findByStatus(ApplicationStatus.REJECTED);
        model.addAttribute("rejectedApplications", rejectedApplications);
        return "applicationPages/rejected-applications";
    }

    @PostMapping("/reject/{id}")
    public String rejectApplication(@PathVariable Long id, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        Application application = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Başvuru bulunamadı!"));

        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepo.save(application);

        return "redirect:/applications/pending"; // Sayfayı yenile
    }

    @PostMapping("/reject-approved/{id}")
    public String rejectApprovedApplication(@PathVariable Long id, final HttpSession session, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        Application application = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Başvuru bulunamadı!"));

        if (application.getStatus() == ApplicationStatus.APPROVED) {
            application.setStatus(ApplicationStatus.REJECTED);
            applicationRepo.save(application);
        }

        return "redirect:/applications/approved"; // Onaylanan başvurular sayfasına geri dön
    }

    @PostMapping("/approve-rejected/{id}")
    public String approveRejectedApplication(@PathVariable Long id, final HttpSession session, final Model model, final RedirectAttributes redirectAttributes) {
        String redirect = checkCommitteeAccess(session, redirectAttributes);
        if (redirect != null) return redirect;

        Application application = applicationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Başvuru bulunamadı!"));

        // Eğer başvuru zaten REJECTED değilse işlem yapma
        if (application.getStatus() != ApplicationStatus.REJECTED) {
            model.addAttribute("errorMessage", "Bu başvuru zaten onaylı veya beklemede!");
            return "redirect:/applications/rejected";
        }

        application.setStatus(ApplicationStatus.APPROVED);
        applicationRepo.save(application);

        return "redirect:/applications/rejected"; //Reddedilen başvurular sayfasına geri dön
    }
}
