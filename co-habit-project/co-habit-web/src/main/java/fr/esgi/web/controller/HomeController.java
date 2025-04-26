package fr.esgi.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/customers")
    public String customers(@AuthenticationPrincipal OidcUser user, Model model) {
        model.addAttribute("username", user.getPreferredUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("authorities", user.getAuthorities());
        return "customers";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }
}
