package nl.tudelft.skills.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.skills.dto.id.SkillId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegularTaskCreate {

    @NotBlank
    private String name;

    @NotNull
    private SkillId skill;

}
