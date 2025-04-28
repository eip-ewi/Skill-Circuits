package nl.tudelft.skills.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.PathId;
import nl.tudelft.skills.model.ChoiceTask;
import nl.tudelft.skills.model.Task;

import java.util.HashSet;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChoiceTaskPatch extends Patch<ChoiceTask> {

    private Integer minTasks;

    @Override
    protected void applyOneToOne() {
        updateNonNull(minTasks, data::setMinTasks);
    }

    @Override
    protected void validate() {
    }
}
