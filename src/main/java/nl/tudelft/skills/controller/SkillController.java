package nl.tudelft.skills.controller;

import lombok.AllArgsConstructor;
import nl.tudelft.librador.resolver.annotations.PathEntity;
import nl.tudelft.skills.dto.patch.SkillPatchDTO;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.service.SkillService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/skill")
public class SkillController {

    private final SkillService skillService;

    @ResponseBody
    @Transactional
    @PatchMapping("{skill}/position")
    public void updatePosition(@PathEntity Skill skill, @RequestParam Integer column) {
        skill.setColumn(column);
    }

    @ResponseBody
    @PatchMapping
    public void updateSkill(@RequestBody SkillPatchDTO patch) {
        skillService.patchSkill(patch);
    }

}
