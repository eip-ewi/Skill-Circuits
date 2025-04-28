package nl.tudelft.skills.dto.view;

import nl.tudelft.labracore.api.dto.PersonSummaryDTO;

import java.util.List;

public record EditionView(
        long id,
        String name,
        CourseView course,
        List<CheckpointView> checkpoints,
        List<ModuleView> modules,
        List<PathView> paths,
        List<PersonSummaryDTO> teachers,
        List<PersonSummaryDTO> editors
) {

    public record CourseView(long id, String name, String code) {}

}
