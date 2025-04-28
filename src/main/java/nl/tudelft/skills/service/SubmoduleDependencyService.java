package nl.tudelft.skills.service;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.lib.security.user.Person;
import nl.tudelft.skills.model.AbstractSkill;
import nl.tudelft.skills.model.Submodule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Responsible for determining dependencies between submodules.
 */
@Service
@AllArgsConstructor
public class SubmoduleDependencyService {

    ///
    /// The parents of a submodule are the submodules of the parents of all its skills.
    /// A submodule is never a parent of itself.
    ///
    public Set<Submodule> getSubmoduleParents(Submodule submodule) {
        return submodule.getSkills().stream().flatMap(skill -> skill.getParents().stream()).map(AbstractSkill::getSubmodule).filter(parent -> !Objects.equals(submodule, parent)).collect(Collectors.toSet());
    }

    ///
    /// The children of a submodule are the submodules of the children of all its skills.
    /// A submodule is never a child of itself.
    ///
    public Set<Submodule> getSubmoduleChildren(Submodule submodule) {
        return submodule.getSkills().stream().flatMap(skill -> skill.getChildren().stream()).map(AbstractSkill::getSubmodule).filter(child -> !Objects.equals(submodule, child)).collect(Collectors.toSet());
    }

}
