
package BlossomCraft.controller;

import BlossomCraft.entity.User;
import BlossomCraft.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // SIGNUP
    @PostMapping("/signup")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String mobile,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "signup";
        }

        try {
            userService.registerUser(name, email, mobile, password);
            return "redirect:/login?registered";

        } catch (RuntimeException e) {
            model.addAttribute("error",
                    "This email is already registered. Please login instead.");
            return "signup";
        }
    }

    // CUSTOMER LOGIN
    @PostMapping("/login")
    public String loginUser(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        User user = userService.authenticateUser(email, password);

        if (user == null) {
            model.addAttribute("error",
                    "Invalid email or password. Please try again.");
            return "login";
        }

        session.setAttribute("loggedInUserId", user.getId());
        session.setAttribute("loggedInUserName", user.getFullName());

        return "redirect:/";
    }

    // CUSTOMER LOGOUT
    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {

        session.removeAttribute("loggedInUserId");
        session.removeAttribute("loggedInUserName");

        return "redirect:/";
    }
}
