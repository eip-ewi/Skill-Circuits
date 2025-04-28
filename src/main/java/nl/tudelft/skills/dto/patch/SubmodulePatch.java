package nl.tudelft.skills.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.CheckpointId;
import nl.tudelft.skills.dto.id.SCModuleId;
import nl.tudelft.skills.dto.id.SubmoduleId;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Submodule;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubmodulePatch extends Patch<Submodule> {

    private String name;

    private SCModuleId module;

    @Override
    protected void applyOneToOne() {
        updateNonNull(name, data::setName);
        updateNonNullId(module, data::setModule);
    }

    @Override
    protected void validate() {

    }
}
