package nl.tudelft.skills.dto.patch;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.model.SCModule;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModulePatch extends Patch<SCModule> {

    private String name;

    @Override
    protected void applyOneToOne() {
        updateNonNull(name, data::setName);
    }

    @Override
    protected void validate() {

    }
}
