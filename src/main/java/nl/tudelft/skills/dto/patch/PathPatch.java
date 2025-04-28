package nl.tudelft.skills.dto.patch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.model.Path;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PathPatch extends Patch<Path> {

    private String name;

    private String description;

    @Override
    protected void applyOneToOne() {
        updateNonNull(name, data::setName);
        updateNonNull(description, data::setDescription);
    }

    @Override
    protected void validate() {

    }
}
