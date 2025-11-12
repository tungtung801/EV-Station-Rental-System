package spring_boot.project_swp.service.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import spring_boot.project_swp.dto.request.UserLoginRequest;
import spring_boot.project_swp.dto.request.UserRegistrationRequest;
import spring_boot.project_swp.dto.response.UserLoginResponse;
import spring_boot.project_swp.dto.response.UserRegistrationResponse;
import spring_boot.project_swp.entity.Role;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.UserProfile;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.service.FileStorageService;
import spring_boot.project_swp.service.JwtService;
import spring_boot.project_swp.service.RoleService;
import spring_boot.project_swp.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private StationRepository stationRepository;
    @Mock
    private FileStorageService fileStorageService; // Dependency của UserServiceImpl

    @InjectMocks
    private UserServiceImpl userService;

    // --- Biến cho Test Đăng Kí ---
    private UserRegistrationRequest registerRequest;
    private User userForRegister;
    private Role userRole;

    // --- Biến cho Test Đăng Nhập ---
    private UserLoginRequest loginRequest;
    private User userForLogin;
    private UserLoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        // Setup cho Đăng Kí
        registerRequest = new UserRegistrationRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPhoneNumber("0123456789");
        registerRequest.setPassword("Password123@");
        registerRequest.setFullName("Test User");

        userRole = Role.builder().roleId(1L).roleName("user").build();
        userForRegister = User.builder().userId(1L).email("test@example.com").build();

        // Setup cho Đăng Nhập
        loginRequest = new UserLoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("123"); // Code của bạn không hash pass, nên test phải khớp

        userForLogin = User.builder()
                .userId(2L)
                .email("login@example.com")
                .password("123") // Mật khẩu khớp
                .role(userRole)
                .build();

        loginResponse = new UserLoginResponse();
        loginResponse.setUserId(2L);
        loginResponse.setEmail("login@example.com");
    }

    @Test
    public void testRegister_ConflictException() {
        // Arrange
        when(userRepository.existsByEmailOrPhoneNumber(registerRequest.getEmail(), registerRequest.getPhoneNumber()))
                .thenReturn(true);

        // Act & Assert
        Exception ex = assertThrows(ConflictException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("Email or phone number already in use", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Test 1: Đăng Kí ---
    @Test
    public void testRegister_Success() {
        // Arrange
        when(userRepository.existsByEmailOrPhoneNumber(registerRequest.getEmail(), registerRequest.getPhoneNumber()))
                .thenReturn(false);
        when(userMapper.toUser(registerRequest)).thenReturn(userForRegister);
        when(roleService.getRoleByName("user")).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(userForRegister);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(new UserProfile());

        // Act
        UserRegistrationResponse response = userService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(userForRegister.getUserId(), response.getUserId());
        assertEquals(userForRegister.getEmail(), response.getEmail());
        verify(userRepository).save(any(User.class));
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    // --- Test 2: Đăng Nhập ---
    @Test
    public void testLogin_Success() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(userForLogin));
        when(jwtService.generateToken(userForLogin)).thenReturn("fake-jwt-token");
        when(userMapper.toUserLoginResponse(userForLogin)).thenReturn(loginResponse);

        // Act
        UserLoginResponse response = userService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getAccessToken());
        assertEquals(userForLogin.getUserId(), response.getUserId());
    }

    @Test
    public void testLogin_UserNotFound_ThrowsConflictException() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        Exception ex = assertThrows(ConflictException.class, () -> {
            userService.login(loginRequest);
        });
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    public void testLogin_WrongPassword_ThrowsConflictException() {
        // Arrange
        loginRequest.setPassword("wrongpass");
        // Mật khẩu của User trong DB là "123"
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(userForLogin));

        // Act & Assert
        Exception ex = assertThrows(ConflictException.class, () -> {
            userService.login(loginRequest);
        });
        assertEquals("Invalid email or password", ex.getMessage());

        // Xác minh rằng token KHÔNG được tạo
        verify(jwtService, never()).generateToken(any(User.class));
    }
}