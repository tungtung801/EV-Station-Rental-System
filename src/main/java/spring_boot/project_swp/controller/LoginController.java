package spring_boot.project_swp.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.respone.ApiResponse;
import spring_boot.project_swp.dto.respone.UserLoginResponse;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginController {
    UserService userService;

    @PostMapping("/login")
//login = email, pass
    public ApiResponse<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession Session) {
        Session.invalidate();
        return ResponseEntity.ok("Logout successful");
    }

}
