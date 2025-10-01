package spring_boot.project_swp.controller;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class LoginController {
    private final UserService userService;

    @PostMapping("/login")
    //login = email, pass
    public ResponseEntity<String> login(@RequestBody User user){
        User existUser = userService.getUserByEmailAndPassword(user.getEmail(), user.getPassword());
        if (existUser != null) {
            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession Session){
        Session.invalidate();
        return ResponseEntity.ok("Logout successful");
    }

}
