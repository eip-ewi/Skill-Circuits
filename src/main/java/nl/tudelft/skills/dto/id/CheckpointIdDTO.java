package nl.tudelft.skills.dto.id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import nl.tudelft.librador.dto.id.IdDTO;
import nl.tudelft.skills.model.Checkpoint;
import nl.tudelft.skills.repository.CheckpointRepository;
import org.springframework.data.repository.CrudRepository;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CheckpointIdDTO extends IdDTO<Checkpoint, Long> {
    public CheckpointIdDTO(Long id) {
        super(id);
    }
    @Override
    public Class<? extends CrudRepository<Checkpoint, Long>> repositoryClass() {
        return CheckpointRepository.class;
    }

    @Override
    public Class<? extends Checkpoint> targetClass() {
        return Checkpoint.class;
    }
}
