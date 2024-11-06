package nl.tudelft.skills.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.model.Task;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskPatchDTO extends Patch<Task> {

    private Long id;
    private String name;
    private Integer time;

    @Override
    protected void applyOneToOne() {
        updateNonNull(name, data::setName);
        updateNonNull(time, data::setTime);
    }

    @Override
    protected void validate() {}
}
