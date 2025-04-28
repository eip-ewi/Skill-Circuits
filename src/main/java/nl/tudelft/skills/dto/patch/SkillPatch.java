package nl.tudelft.skills.dto.patch;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.CheckpointId;
import nl.tudelft.skills.dto.id.SubmoduleId;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.Skill;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SkillPatch extends Patch<AbstractSkill> {

    private String name;

    private SubmoduleId submodule;

    private CheckpointId checkpoint;

    private Boolean essential;

    @Override
    protected void applyOneToOne() {
        if (data instanceof Skill skill) {
            updateNonNull(name, skill::setName);
            updateNonNullId(submodule, skill::setSubmodule);
            if (checkpoint == null) {
                skill.setCheckpoint(null);
            } else {
                updateNonNullId(checkpoint, skill::setCheckpoint);
            }
        }
        updateNonNull(essential, data::setEssential);
    }

    @Override
    protected void validate() {

    }
}
