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
import nl.tudelft.librador.dto.DTOConverter;
import nl.tudelft.skills.dto.patch.EditionPatch;
import nl.tudelft.skills.dto.view.*;
import nl.tudelft.skills.model.SCEdition;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.EditionRepository;
import nl.tudelft.skills.repository.PersonRepository;

@Service
@AllArgsConstructor
public class EditionService {

	private final EditionControllerApi editionApi;
	private final PersonControllerApi personApi;
	private final RoleControllerApi roleApi;

	private final SCPersonService sCPersonService;

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
				roleApi.getRolesByEditions(Set.of(editionId))
						.filter(role -> role.getType() == RolePersonDetailsDTO.TypeEnum.TEACHER)
						.map(RolePersonDetailsDTO::getPerson)
						.sort(Comparator.comparing(PersonSummaryDTO::getDisplayName)).collectList().block(),
				scEdition.getEditors().isEmpty() ? Collections.emptyList()
						: personApi
								.getPeopleById(scEdition.getEditors().stream().map(SCPerson::getId).toList())
								.sort(Comparator.comparing(PersonSummaryDTO::getDisplayName)).collectList()
								.block());
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
		roleApi.addRole(new RoleCreateDTO().edition(new EditionIdDTO().id(editionId))
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
