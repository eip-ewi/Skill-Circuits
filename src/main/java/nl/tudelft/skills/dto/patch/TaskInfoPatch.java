package nl.tudelft.skills.dto.patch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.tudelft.librador.dto.patch.Patch;
import nl.tudelft.skills.dto.id.PathId;
import nl.tudelft.skills.enums.TaskType;
import nl.tudelft.skills.model.RegularTask;
import nl.tudelft.skills.model.TaskInfo;

import java.util.HashSet;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TaskInfoPatch extends Patch<TaskInfo> {

    private String name;

    private TaskType type;

    private Integer time;

    private String link;

    @Override
    protected void applyOneToOne() {
        updateNonNull(name, data::setName);
        updateNonNull(type, data::setType);
        updateNonNull(time, data::setTime);
        if (link != null) {
            data.setLink(link.isBlank() ? null : link);
        }
    }

    @Override
    protected void validate() {
    }
}
