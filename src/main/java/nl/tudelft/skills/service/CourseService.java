package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.CourseControllerApi;
import nl.tudelft.labracore.api.dto.CourseSummaryDTO;
import nl.tudelft.labracore.lib.security.user.Person;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
@AllArgsConstructor
public class CourseService {

    private final CourseControllerApi courseApi;

    public Set<Long> getManagedCourseIds(Person person) {
        return requireNonNull(courseApi.getAllCoursesByManager(person.getId()).map(CourseSummaryDTO::getId).collect(Collectors.toSet()).block());
    }

}
