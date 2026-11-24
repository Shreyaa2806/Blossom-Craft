
package BlossomCraft.service;

import BlossomCraft.entity.User;
import BlossomCraft.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // REGISTER NEW USER
    public User registerUser(String fullName, String email,
                             String mobile, String password) {

        email = email.trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already registered!");
        }

        User user = new User();

        user.setFullName(fullName.trim());
        user.setEmail(email);
        user.setMobile(mobile.trim());
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    // LOGIN EXISTING USER
    public User authenticateUser(String email, String password) {

        if (email == null || password == null ||
                email.trim().isEmpty() || password.isEmpty()) {
            return null;
        }

        email = email.trim().toLowerCase();

        Optional<User> optionalUser =
                userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            if (passwordEncoder.matches(
                    password, user.getPassword())) {
                return user;
            }
        }

        return null;
    }
}
