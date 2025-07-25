package com.sau.summer.controller;

import com.sau.summer.entity.Committee;
import com.sau.summer.enums.Role;
import com.sau.summer.repository.CommitteeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.sql.Connection;

@Controller
public class InitController {
    @Autowired
    private CommitteeRepo committeeRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DataSource dataSource;

    @GetMapping("/initialize")
    public @ResponseBody String showInitializePage() {
        return """
        <html>
        <body>
            <h2>System Initialization</h2>
            <button id='adminBtn'>Create Admin User</button>
            <button id='dumpBtn' style='margin-left:20px;'>Import Home Course SQL Dump</button>
            <div id='result' style='margin-top:20px;'></div>
            <script>
                document.getElementById('adminBtn').onclick = function() {
                    fetch('/initialize/admin', {method: 'POST'})
                        .then(response => response.text())
                        .then(html => { document.getElementById('result').innerHTML = html; })
                        .catch(err => { document.getElementById('result').innerHTML = '<b style="color:red;">Error: ' + err + '</b>'; });
                };
                document.getElementById('dumpBtn').onclick = function() {
                    fetch('/initialize/dump', {method: 'POST'})
                        .then(response => response.text())
                        .then(html => { document.getElementById('result').innerHTML = html; })
                        .catch(err => { document.getElementById('result').innerHTML = '<b style="color:red;">Error: ' + err + '</b>'; });
                };
            </script>
        </body>
        </html>
        """;
    }

    @PostMapping("/initialize/admin")
    @ResponseBody
    public String initializeAdmin() {
        if (committeeRepo.findByUsername("admin") != null) {
            return "<html><body><h2>Admin committee user already exists.</h2></body></html>";
        }
        Committee admin = new Committee();
        admin.setUsername("admin");
        admin.setName("Admin");
        admin.setSurname("User");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setRole(Role.ADMIN);
        admin.setLocked(false);
        admin.setDisabled(false);
        committeeRepo.save(admin);
        return "<html><body><h2>Admin committee user created.<br>Username: admin<br>Password: admin</h2>"
                + "<script>setTimeout(function(){ window.location.href='/login'; }, 2000);</script>"
                + "<p>Redirecting to login page...</p></body></html>";
    }

    @PostMapping("/initialize/dump")
    @ResponseBody
    public String initializeFromDump() {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new org.springframework.core.io.ClassPathResource("homeCourse_dump.sql"));            return "<html><body><h2>SQL dump imported successfully.</h2></body></html>";
        } catch (Exception e) {
            return "<html><body><h2>Error importing SQL dump: " + e.getMessage() + "</h2></body></html>";
        }
    }
}
