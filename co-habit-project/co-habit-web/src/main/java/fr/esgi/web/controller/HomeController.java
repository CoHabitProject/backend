package fr.esgi.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    // @GetMapping("/customers")
    // public String customers(@AuthenticationPrincipal OidcUser user, Model model) {
    //     model.addAttribute("username", user.getPreferredUsername());
    //     model.addAttribute("email", user.getEmail());
    //     model.addAttribute("authorities", user.getAuthorities());
    //     return "customers";
    // }

    @GetMapping("/customers")
    public String customers(@AuthenticationPrincipal(errorOnInvalidType = false) Jwt jwt)
    {
        if (jwt == null) {
            return "JWT is null - no authentication";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Hello ").append(jwt.getClaimAsString("preferred_username")).append("\n");
        sb.append("Email: ").append(jwt.getClaimAsString("email")).append("\n");
        sb.append("Authorities: ").append(jwt.getClaimAsString("realm_access.roles")).append("\n");
        return sb.toString();
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }
}
