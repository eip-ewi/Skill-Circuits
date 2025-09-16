/*
 * Skill Circuits
 * Copyright (C) 2025 - Delft University of Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package nl.tudelft.skills.service;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.api.EditionControllerApi;
import nl.tudelft.labracore.api.PersonControllerApi;
import nl.tudelft.labracore.api.RoleControllerApi;
import nl.tudelft.labracore.api.dto.*;
import nl.tudelft.labracore.api.dto.EditionDetailsDTO;
import nl.tudelft.labracore.api.dto.RoleDetailsDTO;
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.patch.EditionPatch;
import nl.tudelft.skills.dto.view.*;
import nl.tudelft.skills.dto.view.TaskStatsDTO;
import nl.tudelft.skills.model.*;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.*;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.PersonRepository;

@Service
@AllArgsConstructor
public class EditionService {

	private final EditionControllerApi editionApi;
	private final PersonControllerApi personApi;

	private final SCPersonService sCPersonService;
	private final TaskRepository taskRepository;
	private final ClickedLinkRepository clickedLinkRepository;
	private final PathPreferenceRepository pathPreferenceRepository;
	private final RoleControllerApi roleControllerApi;

	private final EditionRepository editionRepository;
	private final PersonRepository personRepository;

	private final DTOConverter dtoConverter;

	public EditionView getEdition(Long editionId) {
		EditionDetailsDTO edition = requireNonNull(editionApi.getEditionById(editionId).block());
		SCEdition scEdition = editionRepository.getOrCreate(editionId);
		return new EditionView(
				edition.getId(),
				edition.getCourse().getName() + " - " + edition.getName(),
				new EditionView.CourseView(edition.getCourse().getId(), edition.getCourse().getName(),
						edition.getCourse().getCode()),
				scEdition.isVisible(),
				scEdition.getCheckpoints().stream()
						.map(checkpoint -> new CheckpointView(checkpoint.getId(), checkpoint.getName(),
								checkpoint.getDeadline()))
						.toList(),
				scEdition.getModules().stream().map(m -> new ModuleView(m.getId(), m.getName())).toList(),
				scEdition.getPaths().stream()
						.map(path -> new PathView(path.getId(), path.getName(), path.getDescription()))
						.toList(),
				roleControllerApi.getRolesByEditions(Set.of(editionId))
						.filter(role -> role.getType() == RolePersonDetailsDTO.TypeEnum.TEACHER)
						.map(RolePersonDetailsDTO::getPerson)
						.sort(Comparator.comparing(PersonSummaryDTO::getDisplayName)).collectList().block(),
				scEdition.getEditors().isEmpty() ? Collections.emptyList()
						: personApi
								.getPeopleById(scEdition.getEditors().stream().map(SCPerson::getId).toList())
								.sort(Comparator.comparing(PersonSummaryDTO::getDisplayName)).collectList()
								.block());
	}

	/**
	 * Collects statistics for each task in a given edition. The statistics contain information about the name
	 * of the tasks, skills, checkpoints, submodules and modules, counts of link clicks, completions and path
	 * inclusions.
	 *
	 * @param  id The id of the edition, the statistics needs to be collected for
	 * @return    a list of TaskStatsDTO objects, containing the task level statistics for the given edition
	 */
	public List<TaskStatsDTO> teacherStatsTaskLevel(Long id) {
		List<Task> tasks = taskRepository.findAllBySkillSubmoduleModuleEditionId(id);

		// Collect all the TaskInfo into a list from the existing tasks
		List<TaskInfo> taskInfos = tasks.stream().flatMap(t -> {
			List<TaskInfo> info = new ArrayList<>();
			if (t instanceof RegularTask) {
				info = List.of(((RegularTask) t).getTaskInfo());
			} else if (t instanceof ChoiceTask) {
				info = ((ChoiceTask) t).getTasks();
			}
			return info.stream();
		}).collect(Collectors.toList());

		return taskInfos.stream().map(t -> {
			// Get the ids of the users who completed the current task
			Set<Long> usersCompletedTask = t.getCompletedBy().stream()
					.map(c -> c.getPerson().getId())
					.collect(Collectors.toSet());

			// Filter out the non-student users
			Set<Long> studentsCompletedTask = usersCompletedTask.isEmpty() ? Set.of()
					: roleControllerApi.getRolesById(Set.of(id), usersCompletedTask).toStream()
							.filter(r -> r.getType().equals(RoleDetailsDTO.TypeEnum.STUDENT))
							.map(r -> r.getPerson().getId()).collect(Collectors.toSet());

			// The TaskInfo should belong to either a RegularTask or a ChoiceTask object
			Task taskCategory = t.getChoiceTask() == null ? t.getTask() : t.getChoiceTask();

			// Get the ids of the users who have the current task on their path
			Set<Long> usersHaveTaskOnPath = taskCategory == null ? Set.of()
					: taskCategory.getPaths().stream()
							.flatMap(p -> pathPreferenceRepository
									.findAllByPathId(p.getId()).stream().map(x -> x.getPerson().getId()))
							.collect(Collectors.toSet());

			// Filter out the non-student users and count th remaining users
			long numOfStudentsHaveTaskOnPath = usersHaveTaskOnPath.isEmpty() ? 0
					: roleControllerApi.getRolesById(Set.of(id), usersHaveTaskOnPath).toStream()
							.filter(r -> r.getType().equals(RoleDetailsDTO.TypeEnum.STUDENT)).count();

			// Get the ids of the people who clicked on the link for the current task
			List<Long> peopleClickedLink = clickedLinkRepository.getByTask(t).stream()
					.map(c -> c.getPerson().getId()).collect(Collectors.toList());

			// Collect unique students from the people who clicked on the link
			Set<Long> studentsClickedLink = peopleClickedLink.isEmpty() ? Set.of()
					: roleControllerApi.getRolesById(Set.of(id),
							new HashSet<>(peopleClickedLink)).toStream()
							.filter(r -> r.getType().equals(RoleDetailsDTO.TypeEnum.STUDENT))
							.map(x -> x.getPerson().getId()).collect(Collectors.toSet());

			// Count all the clicks made by students on the current link
			long numOfStudentsClickedLink = peopleClickedLink.stream().filter(studentsClickedLink::contains)
					.count();

			// Count students who clicked on the link and completed the task
			long studentsClickedAndCompleted = studentsCompletedTask.stream()
					.filter(studentsClickedLink::contains).count();

			return new TaskStatsDTO(t.getId(),
					t.getName(),
					taskCategory == null ? "Unknown" : taskCategory.getSkill().getName(),
					taskCategory == null ? "Unknown"
							: taskCategory.getSkill().getCheckpoint() == null ? "No checkpoint"
									: taskCategory.getSkill().getCheckpoint().getName(),
					taskCategory == null ? "Unknown" : taskCategory.getSkill().getSubmodule().getName(),
					taskCategory == null ? "Unknown"
							: taskCategory.getSkill().getSubmodule().getModule().getName(),
					studentsCompletedTask.size(),
					numOfStudentsHaveTaskOnPath,
					numOfStudentsClickedLink,
					studentsClickedLink.size(),
					studentsClickedAndCompleted);
		}).collect(Collectors.toList());
	}

	public Set<Long> getManagedEditionIds(SCPerson person, boolean includeStale) {
		Set<Long> managedEditions = new HashSet<>();
		personRepository.findById(person.getId()).ifPresent(scPerson -> scPerson.getEditorInEditions()
				.forEach(edition -> managedEditions.add(edition.getId())));
		managedEditions.addAll(requireNonNull(personApi.getRolesForPerson(person.getId())
				.filter(role -> role.getType() == RoleEditionDetailsDTO.TypeEnum.TEACHER)
				.filter(role -> includeStale || (!role.getEdition().getIsArchived()
						&& role.getEdition().getEndDate().isAfter(LocalDateTime.now())))
				.map(role -> role.getEdition().getId())
				.collectList()
				.block()));
		return managedEditions;
	}

	public List<ManagedEditionView> getManagedEditions(SCPerson person, boolean includeStale) {
		Set<Long> editionIds = getManagedEditionIds(person, includeStale);
		if (editionIds.isEmpty()) {
			return Collections.emptyList();
		}
		List<EditionDetailsDTO> editionDetails = requireNonNull(editionApi
				.getEditionsById(new ArrayList<>(editionIds))
				.sort(Comparator
						.<EditionDetailsDTO, String>comparing(edition -> edition.getCourse().getCode())
						.thenComparing(EditionDetailsDTO::getStartDate))
				.collectList().block());
		return editionDetails.stream().map(edition -> {
			SCEdition scEdition = editionRepository.getOrCreate(edition.getId());
			return new ManagedEditionView(
					edition.getId(),
					edition.getName(),
					new ManagedEditionView.CourseView(edition.getCourse().getId(),
							edition.getCourse().getName(), edition.getCourse().getCode()),
					!scEdition.getCheckpoints().isEmpty() || !scEdition.getPaths().isEmpty()
							|| !scEdition.getModules().isEmpty());
		}).toList();
	}

	public Set<Long> getVisibleEnrolledEditionIds(SCPerson person) {
		List<RoleEditionDetailsDTO> roles = requireNonNull(personApi.getRolesForPerson(person.getId())
				.collectList()
				.block());
		Set<Long> roleInNonHiddenEditionIds = roles.isEmpty() ? Collections.emptySet()
				: roles.stream()
						.filter(role -> editionRepository.getOrCreate(role.getEdition().getId()).isVisible())
						.filter(role -> !editionRepository.getOrCreate(role.getEdition().getId()).getModules()
								.isEmpty())
						.map(role -> role.getEdition().getId())
						.collect(Collectors.toSet());

		Set<Long> managedEditionIds = getManagedEditionIds(person, true);
		Set<Long> allEditionIds = new HashSet<>();
		allEditionIds.addAll(roleInNonHiddenEditionIds);
		allEditionIds.addAll(managedEditionIds);
		return allEditionIds;
	}

	public EditionsView getSortedEditions(SCPerson person) {
		Set<Long> allEditionIds = getVisibleEnrolledEditionIds(person);

		List<EditionDetailsDTO> editions = allEditionIds.isEmpty() ? Collections.emptyList()
				: requireNonNull(
						editionApi.getEditionsById(new ArrayList<>(allEditionIds)).collectList().block());

		List<EditionDetailsDTO> current = new ArrayList<>();
		List<EditionDetailsDTO> upcoming = new ArrayList<>();
		List<EditionDetailsDTO> finished = new ArrayList<>();
		List<EditionDetailsDTO> archived = new ArrayList<>();

		editions.stream().sorted(Comparator.comparing(EditionDetailsDTO::getStartDate)).forEach(edition -> {
			if (edition.getIsArchived()) {
				archived.addFirst(edition);
			} else if (edition.getEndDate().isBefore(LocalDateTime.now())) {
				finished.addFirst(edition);
			} else if (edition.getStartDate().isAfter(LocalDateTime.now())) {
				upcoming.addLast(edition);
			} else {
				current.addFirst(edition);
			}
		});

		return new EditionsView(current, upcoming, finished, archived);
	}

	public List<EditionDetailsDTO> getOpenEditions(SCPerson person) {
		Set<Long> editionIds = editionRepository.findAllOpen().stream().map(SCEdition::getId)
				.collect(Collectors.toSet());
		editionIds.removeAll(getVisibleEnrolledEditionIds(person));
		return requireNonNull(editionApi.getEditionsById(new ArrayList<>(editionIds))
				.filter(edition -> !edition.getIsArchived())
				.filter(edition -> edition.getEndDate().isAfter(LocalDateTime.now()))
				.collectList().block());
	}

	public void addPersonToEdition(SCPerson person, Long editionId) {
		roleControllerApi.addRole(new RoleCreateDTO().edition(new EditionIdDTO().id(editionId))
				.person(new PersonIdDTO().id(person.getId())).type(RoleCreateDTO.TypeEnum.STUDENT)).block();
	}

	@Transactional
	public void patchEdition(SCEdition edition, EditionPatch patch) {
		editionRepository.save(patch.apply(edition, dtoConverter));
		editionRepository.save(edition);
	}

	@Transactional
	public void addEditor(SCEdition edition, Long editorId) {
		edition.getEditors().add(sCPersonService.getOrCreate(editorId));
		editionRepository.save(edition);
	}

	@Transactional
	public void removeEditor(SCEdition edition, SCPerson editor) {
		edition.getEditors().remove(editor);
		editionRepository.save(edition);
	}
}
