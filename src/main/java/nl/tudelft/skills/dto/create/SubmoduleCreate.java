package nl.tudelft.skills.dto.create;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.dto.id.SCModuleId;
import nl.tudelft.skills.dto.id.SubmoduleId;
import nl.tudelft.skills.model.Skill;
import nl.tudelft.skills.model.Submodule;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubmoduleCreate extends Create<Submodule> {

    @NotBlank
    private String name;

    @NotNull
    private SCModuleId module;

    @Nullable
    private Integer column;

    @Override
    public Class<Submodule> clazz() {
        return Submodule.class;
    }

}
