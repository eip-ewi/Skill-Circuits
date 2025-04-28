package nl.tudelft.skills.dto.patch;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.PathId;
import nl.tudelft.skills.dto.id.SkillId;
import nl.tudelft.skills.enums.TaskType;
import nl.tudelft.skills.model.RegularTask;

import java.util.HashSet;
import java.util.List;

@Data
@NoArgsConstructor
public class TaskMove {

    @NotNull
    private SkillId skill;

    @NotNull
    private Integer index;

}
