package spring_boot.project_swp.service.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import spring_boot.project_swp.dto.request.UserProfileRequest;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.UserProfile;
import spring_boot.project_swp.entity.UserProfileStatusEnum;
import spring_boot.project_swp.mapper.UserProfileMapper;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.service.FileStorageService;
import spring_boot.project_swp.service.impl.UserProfileServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private User user;
    private UserProfile userProfile;
    private MockMultipartFile idCardFile;
    private UserProfileRequest userProfileRequest;

    @BeforeEach
    void setUp() {
        user = User.builder().userId(1L).build();
        userProfile = UserProfile.builder()
                .profileId(1L)
                .user(user)
                .status(UserProfileStatusEnum.NULL) // Trạng thái ban đầu
                .build();

        idCardFile = new MockMultipartFile(
                "idCardFile",
                "idcard.jpg",
                "image/jpeg",
                "some-image-bytes".getBytes()
        );

        userProfileRequest = UserProfileRequest.builder()
                .idCardFile(idCardFile) // Gán file vào request
                .bio("Test bio")
                .build();
    }

    // --- Test 5: Tải lên CCCD ---
    @Test
    public void testUpdateUserProfile_UploadIdCard_ExistingProfile() {
        // Arrange
        Long userId = 1L;
        String savedFileName = "unique-uuid-idcard.jpg";

        when(userProfileRepository.findByUserUserId(userId)).thenReturn(Optional.of(userProfile));
        when(fileStorageService.saveFile(idCardFile)).thenReturn(savedFileName);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        // Giả lập mapper sẽ map trường 'bio'
        doNothing().when(userProfileMapper).updateUserProfileFromRequest(any(), any());

        // Act
        userProfileService.updateUserProfile(userId, userProfileRequest);

        // Assert
        // Bắt đối tượng UserProfile được truyền vào hàm save
        ArgumentCaptor<UserProfile> userProfileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(userProfileCaptor.capture());

        UserProfile capturedProfile = userProfileCaptor.getValue();

        // Kiểm tra file đã được lưu
        assertEquals(savedFileName, capturedProfile.getIdCardUrl());
        // Kiểm tra trạng thái đã chuyển sang PENDING
        assertEquals(UserProfileStatusEnum.PENDING, capturedProfile.getStatus());
        // Kiểm tra mapper đã được gọi để cập nhật các trường khác (như bio)
        verify(userProfileMapper).updateUserProfileFromRequest(eq(userProfileRequest), eq(userProfile));
    }

    @Test
    public void testUpdateUserProfile_UploadIdCard_NewProfile() {
        // Arrange
        Long userId = 1L;
        String savedFileName = "new-profile-id.jpg";

        // Giả lập KHÔNG tìm thấy profile, nhưng tìm thấy User
        when(userProfileRepository.findByUserUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(fileStorageService.saveFile(idCardFile)).thenReturn(savedFileName);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        doNothing().when(userProfileMapper).updateUserProfileFromRequest(any(), any());

        // Act
        userProfileService.updateUserProfile(userId, userProfileRequest);

        // Assert
        ArgumentCaptor<UserProfile> userProfileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(userProfileCaptor.capture());

        UserProfile capturedProfile = userProfileCaptor.getValue();

        // Kiểm tra profile mới được tạo đúng
        assertNull(capturedProfile.getProfileId()); // Vì là profile mới
        assertEquals(user, capturedProfile.getUser());
        assertEquals(savedFileName, capturedProfile.getIdCardUrl());
        assertEquals(UserProfileStatusEnum.PENDING, capturedProfile.getStatus());
    }
}