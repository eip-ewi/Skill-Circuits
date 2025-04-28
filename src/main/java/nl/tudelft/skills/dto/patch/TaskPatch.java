package nl.tudelft.skills.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.PathId;
import nl.tudelft.skills.enums.TaskType;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.model.Task;

import java.util.HashSet;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TaskPatch extends Patch<Task> {

    private String name;

    private List<PathId> paths;

    @Override
    protected void applyOneToOne() {
        updateNonNull(name, data::setName);
    }

    @Override
    protected void applyManyToManyDominant() {
        updateNonNullId(paths, paths -> data.setPaths(new HashSet<>(paths)));
    }

    @Override
    protected void validate() {
    }
}
