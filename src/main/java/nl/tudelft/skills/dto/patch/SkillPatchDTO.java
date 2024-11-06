package nl.tudelft.skills.dto.patch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.model.Skill;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillPatchDTO extends Patch<Skill> {

    private Long id;

    @JsonProperty("items")
    private List<TaskPatchDTO> tasks;

    @Override
    protected void validate() {}
}
