package nl.tudelft.skills.dto.view;

import java.time.LocalDateTime;
import java.util.List;

public record CheckpointViewDTO(
      Long id,
      String name,
      LocalDateTime deadline,
      List<Long> skills
) { }
