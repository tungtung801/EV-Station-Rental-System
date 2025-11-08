package spring_boot.project_swp.service.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spring_boot.project_swp.dto.response.VehicleResponse;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.VehicleMapper;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.repository.VehicleModelRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.FileStorageService;
import spring_boot.project_swp.service.impl.VehicleServiceImpl;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private VehicleModelRepository vehicleModelRepository; // Dependency
    @Mock
    private StationRepository stationRepository; // Dependency
    @Mock
    private VehicleMapper vehicleMapper;
    @Mock
    private FileStorageService fileService; // Dependency

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle vehicle;
    private VehicleResponse vehicleResponse;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder().vehicleId(1L).licensePlate("29-A1 123.45").build();
        vehicleResponse = new VehicleResponse();
        vehicleResponse.setVehicleId(1L);
        vehicleResponse.setLicensePlate("29-A1 123.45");
    }

    // --- Test 3: List Xe ---
    @Test
    public void testFindAll_Success() {
        // Arrange
        List<Vehicle> vehicles = List.of(vehicle);
        List<VehicleResponse> responses = List.of(vehicleResponse);

        when(vehicleRepository.findAll()).thenReturn(vehicles);
        when(vehicleMapper.toVehicleResponseList(vehicles)).thenReturn(responses);

        // Act
        List<VehicleResponse> result = vehicleService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(vehicleResponse.getVehicleId(), result.get(0).getVehicleId());
    }

    // --- Test 4: Tìm Xe ---
    @Test
    public void testFindById_Success() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toVehicleResponse(vehicle)).thenReturn(vehicleResponse);

        // Act
        VehicleResponse result = vehicleService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(vehicle.getVehicleId(), result.getVehicleId());
        verify(vehicleRepository).findById(1L);
    }

    @Test
    public void testFindById_NotFound() {
        // Arrange
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception ex = assertThrows(NotFoundException.class, () -> {
            vehicleService.findById(99L);
        });
        assertEquals("Vehicle not found", ex.getMessage());
    }
}