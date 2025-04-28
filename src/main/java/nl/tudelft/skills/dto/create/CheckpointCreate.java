package nl.tudelft.skills.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.create.Create;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.SCEditionId;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.SCEdition;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CheckpointCreate extends Create<Checkpoint> {

    @NotNull
    private String name;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime deadline;

    @NotNull
    private SCEditionId edition;

    @Override
    public Class<Checkpoint> clazz() {
        return Checkpoint.class;
    }

}
