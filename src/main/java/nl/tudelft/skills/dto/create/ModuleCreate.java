package nl.tudelft.skills.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.skills.dto.id.SCEditionId;
import nl.tudelft.skills.model.SCModule;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ModuleCreate extends Create<SCModule> {

    @NotBlank
    private String name;

    @NotNull
    private SCEditionId edition;

    @Override
    public Class<SCModule> clazz() {
        return SCModule.class;
    }
}
