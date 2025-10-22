package spring_boot.project_swp.config;

import lombok.AllArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import spring_boot.project_swp.dto.request.RoleRequest;
import spring_boot.project_swp.entity.Location;
import spring_boot.project_swp.entity.Role;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.entity.VehicleModel;
import spring_boot.project_swp.repository.LocationRepository;
import spring_boot.project_swp.repository.RoleRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.DiscountRepository;
import spring_boot.project_swp.entity.Discount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import spring_boot.project_swp.repository.VehicleModelRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.service.RoleService;

import java.time.LocalDate;
import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
private final DiscountRepository discountRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final LocationRepository locationRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final StationRepository stationRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleService.findByRoleName("admin").isEmpty()) {
            RoleRequest adminRoleRequest = new RoleRequest();
            adminRoleRequest.setRoleName("admin");
            roleService.createRole(adminRoleRequest);

            RoleRequest staffRoleRequest = new RoleRequest();
            staffRoleRequest.setRoleName("staff");
            roleService.createRole(staffRoleRequest);

            RoleRequest userRoleRequest = new RoleRequest();
            userRoleRequest.setRoleName("user");
            roleService.createRole(userRoleRequest);

        }

        // Tạo dữ liệu giảm giá mẫu nếu chưa tồn tại
        if (discountRepository.count() == 0) {
            Discount sale20 = Discount.builder()
                .code("SALE20")
                .description("20% off trên tất cả đơn thuê")
                .amountPercentage(new BigDecimal("20.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .usageLimit(100)
                .isActive(true) // Thêm dòng này
                .build();

            Discount fixed100 = Discount.builder()
                .code("FIXED100")
                .description("Giảm 100.000 VNĐ cho đơn thuê từ 3 ngày")
                .amountFixed(new BigDecimal("100000"))
                .minRentalDuration(3)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(2))
                .isActive(true) // Thêm dòng này
                .build();

            Discount longRent = Discount.builder()
                .code("LONGRENT")
                .description("15% off cho thuê từ 7 ngày")
                .amountPercentage(new BigDecimal("15.00"))
                .minRentalDuration(7)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .isActive(true) // Thêm dòng này
                .build();

            Discount limited50 = Discount.builder()
                .code("LIMITED50")
                .description("25% off (chỉ 50 lượt)")
                .amountPercentage(new BigDecimal("25.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusWeeks(3))
                .usageLimit(50)
                .isActive(true) // Thêm dòng này
                .build();

            discountRepository.saveAll(List.of(sale20, fixed100, longRent, limited50));
        }

        // Tạo user admin nếu chưa tồn tại
        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            User user1 = new User();
            user1.setFullName("Admin");
            user1.setEmail("admin@gmail.com");
            user1.setPhoneNumber("0123456789");
            user1.setPassword("Admin@123");
            user1.setRole(roleRepository.findByRoleName("admin").orElse(null));
            userRepository.save(user1);
        }
        // Initialize Locations
        Location hanoi = locationRepository.findByLocationName("Hanoi")
                .orElseGet(() -> {
                    Location newHanoi = Location.builder()
                            .locationName("Hanoi")
                            .locationType("City")
                            .isActive(true)
                            .build();
                    return locationRepository.save(newHanoi);
                });

        Location hoanKiem = locationRepository.findByLocationNameAndParent("Hoan Kiem", hanoi)
                .orElseGet(() -> {
                    Location newHoanKiem = Location.builder()
                            .locationName("Hoan Kiem")
                            .locationType("District")
                            .parent(hanoi)
                            .isActive(true)
                            .build();
                    return locationRepository.save(newHoanKiem);
                });

        Location baDinh = locationRepository.findByLocationNameAndParent("Ba Dinh", hanoi)
                .orElseGet(() -> {
                    Location newBaDinh = Location.builder()
                            .locationName("Ba Dinh")
                            .locationType("District")
                            .parent(hanoi)
                            .isActive(true)
                            .build();
                    return locationRepository.save(newBaDinh);
                });

        Location hangTrong = locationRepository.findByLocationNameAndParent("Hang Trong", hoanKiem)
                .orElseGet(() -> {
                    Location newHangTrong = Location.builder()
                            .locationName("Hang Trong")
                            .locationType("Ward")
                            .parent(hoanKiem)
                            .isActive(true)
                            .build();
                    return locationRepository.save(newHangTrong);
                });

        Location phucTan = locationRepository.findByLocationNameAndParent("Phuc Tan", hoanKiem)
                .orElseGet(() -> {
                    Location newPhucTan = Location.builder()
                            .locationName("Phuc Tan")
                            .locationType("Ward")
                            .parent(hoanKiem)
                            .isActive(true)
                            .build();
                    return locationRepository.save(newPhucTan);
                });

        // Initialize Vehicle Models
        VehicleModel modelA = null;
        if (vehicleModelRepository.findByModelName("Xe Dap Dien A").isEmpty()) {
            modelA = VehicleModel.builder()
                    .modelName("Xe Dap Dien A")
                    .description("Model A electric bike")
                    .brand("VinFast")
                    .type("Electric Bicycle")
                    .capacityKWh(10)
                    .build();
            modelA = vehicleModelRepository.save(modelA);
        } else {
            modelA = vehicleModelRepository.findByModelName("Xe Dap Dien A").get();
        }

        VehicleModel modelB = null;
        if (vehicleModelRepository.findByModelName("Xe May Dien B").isEmpty()) {
            modelB = VehicleModel.builder()
                    .modelName("Xe May Dien B")
                    .description("Model B electric scooter")
                    .brand("VinFast")
                    .type("Electric Scooter")
                    .capacityKWh(20)
                    .build();
            modelB = vehicleModelRepository.save(modelB);
        } else {
            modelB = vehicleModelRepository.findByModelName("Xe May Dien B").get();
        }

        // Initialize Stations
        Station station1 = stationRepository.findStationByStationName("Station Hoan Kiem 1")
                .orElseGet(() -> {
                    Station newStation1 = Station.builder()
                            .stationName("Station Hoan Kiem 1")
                            .address("123 Hang Trong, Hoan Kiem")
                            .totalDocks(10)
                            .availableDocks(10)
                            .isActive(true)
                            .location(hangTrong)
                            .build();
                    return stationRepository.save(newStation1);
                });

        Station station2 = stationRepository.findStationByStationName("Station Hoan Kiem 2")
                .orElseGet(() -> {
                    Station newStation2 = Station.builder()
                            .stationName("Station Hoan Kiem 2")
                            .address("456 Phuc Tan, Hoan Kiem")
                            .totalDocks(15)
                            .availableDocks(15)
                            .isActive(true)
                            .location(phucTan)
                            .build();
                    return stationRepository.save(newStation2);
                });

        // Initialize Vehicles
        if (modelA != null && station1 != null && vehicleRepository.findByLicensePlate("29-A1 123.45").isEmpty()) {
            Vehicle vehicle1 = Vehicle.builder()
                    .licensePlate("29-A1 123.45")
                    .batteryCapacity(100)
                    .currentBattery(80)
                    .vehicleStatus("Available")
                    .pricePerHour(15000.0)
                    .vehicleModel(modelA)
                    .station(station1)
                    .build();
            vehicleRepository.save(vehicle1);
        }

        if (modelB != null && station1 != null && vehicleRepository.findByLicensePlate("29-B1 678.90").isEmpty()) {
            Vehicle vehicle2 = Vehicle.builder()
                    .licensePlate("29-B1 678.90")
                    .batteryCapacity(120)
                    .currentBattery(90)
                    .vehicleStatus("Available")
                    .pricePerHour(20000.0)
                    .vehicleModel(modelB)
                    .station(station1)
                    .build();
            vehicleRepository.save(vehicle2);
        }

        if (modelA != null && station2 != null && vehicleRepository.findByLicensePlate("30-C1 111.22").isEmpty()) {
            Vehicle vehicle3 = Vehicle.builder()
                    .licensePlate("30-C1 111.22")
                    .batteryCapacity(90)
                    .currentBattery(70)
                    .vehicleStatus("Available")
                    .pricePerHour(15000.0)
                    .vehicleModel(modelA)
                    .station(station2)
                    .build();
            vehicleRepository.save(vehicle3);
    }
}
}