package nl.tudelft.skills.dto.patch;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.skills.dto.id.ChoiceTaskId;
import nl.tudelft.skills.dto.id.SkillId;

@Data
@NoArgsConstructor
public class SubtaskMove {

    @NotNull
    private ChoiceTaskId choiceTask;

}
