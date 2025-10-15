package spring_boot.project_swp.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import spring_boot.project_swp.entity.Role;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.repository.RoleRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.service.RoleService;

import java.time.LocalDate;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;

    @Override
    public void run(String... args) throws Exception {
        if (roleService.findByRoleName("admin").isEmpty()) {
            Role role1 = new Role();
            role1.setRoleName("admin");
            roleService.createRole(role1);

            Role role2 = new Role();
            role2.setRoleName("staff");
            roleService.createRole(role2);

            Role role = new Role();
            role.setRoleName("user");
            roleService.createRole(role);

        }

        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            User user1 = new User();
            user1.setFullName("Admin");
            user1.setEmail("admin@gmail.com");
            user1.setPhoneNumber("0123456789");
            user1.setPassword("Admin@123");
            user1.setRole(roleService.findByRoleName("admin").get());
            userRepository.save(user1);
        }
    }
}