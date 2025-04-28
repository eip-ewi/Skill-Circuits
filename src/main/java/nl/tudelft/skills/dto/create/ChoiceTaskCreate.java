package nl.tudelft.skills.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.dto.id.SkillId;
import nl.tudelft.skills.model.ChoiceTask;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChoiceTaskCreate {

    @NotNull
    private SkillId skill;
}
