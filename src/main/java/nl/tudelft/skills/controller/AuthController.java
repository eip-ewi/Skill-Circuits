package nl.tudelft.skills.controller;

import liquibase.pro.packaged.A;
import lombok.AllArgsConstructor;
import nl.tudelft.skills.security.AuthorisationService;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/auth")
public class AuthController {

    private final AuthorisationService authorisationService;

    public record Auth(boolean canEditBlocks, boolean canEditItems, boolean canCompleteItems) {}

    @GetMapping("module/{moduleId}")
    public Auth getAuthForModule(@PathVariable Long moduleId, @RequestParam(defaultValue = "false") boolean studentMode) {
        boolean canEditBlocks = authorisationService.canEditModule(moduleId);
        boolean canEditItems = authorisationService.canEditModule(moduleId);
        boolean canCompleteItems = true;
        return new Auth(canEditBlocks, canEditItems, canCompleteItems);
    }

}
