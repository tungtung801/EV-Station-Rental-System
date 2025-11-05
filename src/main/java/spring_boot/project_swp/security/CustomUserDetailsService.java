package spring_boot.project_swp.security;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    if (user.getRole() != null) {
      grantedAuthorities.add(new SimpleGrantedAuthority(user.getRole().getRoleName()));
    }

    boolean isEnabled = user.getAccountStatus() == true;

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        isEnabled, // enabled
        true, // accountNonExpired
        true, // credentialsNonExpired
        true, // accountNonLocked
        grantedAuthorities);
  }
}
