package nl.tudelft.skills.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.CheckpointId;
import nl.tudelft.skills.dto.id.SubmoduleId;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.model.Skill;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CheckpointPatch extends Patch<Checkpoint> {

    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime deadline;

    @Override
    protected void applyOneToOne() {
        updateNonNull(name, data::setName);
        updateNonNull(deadline, data::setDeadline);
    }

    @Override
    protected void validate() {

    }
}
