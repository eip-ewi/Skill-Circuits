package nl.tudelft.skills.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
public class DevModeController {

    private final Environment environment;

    @GetMapping("/dev-mode")
    public boolean isDevMode() {
        return List.of(environment.getActiveProfiles()).contains("development");
    }

}
