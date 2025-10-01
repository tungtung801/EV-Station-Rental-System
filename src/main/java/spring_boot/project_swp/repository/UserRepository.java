package spring_boot.project_swp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_boot.project_swp.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User getUserByEmailAndPassword(String email, String password);

    User findByPhoneNumber(String phoneNumber);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
