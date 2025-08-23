// /*
//  * Skill Circuits
//  * Copyright (C) 2025 - Delft University of Technology
//  *
//  * This program is free software: you can redistribute it and/or modify
//  * it under the terms of the GNU Affero General Public License as
//  * published by the Free Software Foundation, either version 3 of the
//  * License, or (at your option) any later version.
//  *
//  * This program is distributed in the hope that it will be useful,
//  * but WITHOUT ANY WARRANTY; without even the implied warranty of
//  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  * GNU Affero General Public License for more details.
//  *
//  * You should have received a copy of the GNU Affero General Public License
//  * along with this program.  If not, see <https://www.gnu.org/licenses/>.
//  */
// package nl.tudelft.skills.test;
//
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.util.*;
//
// import jakarta.annotation.PostConstruct;
//
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Primary;
// import org.springframework.stereotype.Service;
//
// import nl.tudelft.skills.model.*;
// import nl.tudelft.skills.model.SCPerson;
// import nl.tudelft.skills.repository.*;
// import nl.tudelft.skills.repository.PersonRepository;
//
// @Primary
// @Service
// public class TestDatabaseLoader {
//
// 	/**
// 	 * ID constants.
// 	 */
// 	private final Long COURSE_ID = 30L;
// 	private final Long EDITION_ID = 69L;
//
// 	/**
// 	 * Repositories.
// 	 */
// 	@Autowired
// 	private CourseRepository courseRepository;
// 	@Autowired
// 	private EditionRepository editionRepository;
// 	@Autowired
// 	private ModuleRepository moduleRepository;
// 	@Autowired
// 	private SubmoduleRepository submoduleRepository;
// 	@Autowired
// 	private SkillRepository skillRepository;
// 	@Autowired
// 	private RegularTaskRepository regularTaskRepository;
// 	@Autowired
// 	private ChoiceTaskRepository choiceTaskRepository;
// 	@Autowired
// 	private TaskInfoRepository taskInfoRepository;
// 	@Autowired
// 	private ClickedLinkRepository clickedLinkRepository;
// 	@Autowired
// 	private PersonRepository personRepository;
// 	@Autowired
// 	private BadgeRepository badgeRepository;
// 	@Autowired
// 	private InventoryRepository inventoryRepository;
// 	@Autowired
// 	private PathRepository pathRepository;
// 	@Autowired
// 	private CheckpointRepository checkpointRepository;
// 	@Autowired
// 	private TaskCompletionRepository taskCompletionRepository;
// 	@Autowired
// 	private ExternalSkillRepository externalSkillRepository;
//
// 	/**
// 	 * Test models.
// 	 */
// 	private SCCourse courseRL = SCCourse.builder().build();
//
// 	private SCEdition editionRL2021 = SCEdition.builder().build();
//
// 	private Path pathFinderPath = Path.builder().edition(editionRL2021).name("Pathfinder").build();
//
// 	private SCModule moduleProofTechniques = SCModule.builder().name("Proof Techniques")
// 			.build();
//
// 	private SCModule module = SCModule.builder().name("Module")
// 			.build();
//
// 	private Submodule submoduleLogicBasics = Submodule.builder().name("Logic Basics")
// 			.row(0).column(0).build();
// 	private Submodule submoduleGeneralisation = Submodule.builder()
// 			.name("Generalisation").row(1).column(0).build();
// 	private Submodule submoduleCases = Submodule.builder().name("Cases").row(0)
// 			.column(1).build();
// 	private Submodule submoduleContradiction = Submodule.builder().name("Contradiction")
// 			.row(1).column(1).build();
// 	private Submodule submoduleContrapositive = Submodule.builder()
// 			.name("Contrapositive").row(0).column(2).build();
// 	private Submodule submoduleInduction = Submodule.builder().name("Induction").row(1)
// 			.column(2).build();
//
// 	private Checkpoint checkpointLectureOne = Checkpoint.builder().name("Lecture 1")
// 			.deadline((LocalDateTime.of(LocalDate.ofYearDay(2022, 42), LocalTime.MIDNIGHT))).build();
// 	private Checkpoint checkpointLectureTwo = Checkpoint.builder().name("Lecture 2")
// 			.deadline((LocalDateTime.of(LocalDate.ofYearDay(2022, 49), LocalTime.MIDNIGHT))).build();
//
// 	private Skill skillImplication = Skill.builder().name("Implication").row(0)
// 			.column(0).build();
// 	private Skill skillNegation = Skill.builder().name("Negation").row(0).column(2)
// 			.build();
// 	private Skill skillVariables = Skill.builder().name("Variables").row(0).column(4)
// 			.build();
// 	private Skill skillProofOutline = Skill.builder().name("Proof Outline").row(1)
// 			.column(3).build();
// 	private Skill skillAssumption = Skill.builder().name("Assumption").row(1).column(1)
// 			.build();
// 	private Skill skillGeneralisationPractice = Skill.builder()
// 			.name("Generalisation Practice").row(2).column(3).build();
// 	private Skill skillDividingIntoCases = Skill.builder().name("Dividing into Cases")
// 			.row(2).column(1).build();
// 	private Skill skillCasesPractice = Skill.builder().name("Cases Practice").row(3)
// 			.column(2).build();
// 	private Skill skillContradictionPractice = Skill.builder()
// 			.name("Contradiction Practice").row(3).column(3).build();
// 	private Skill skillNegateImplications = Skill.builder().name("Negate Implications")
// 			.row(4).column(1).build();
// 	private Skill skillContrapositivePractice = Skill.builder()
// 			.name("Contrapositive Practice").row(5).column(1).build();
// 	private Skill skillTransitiveProperty = Skill.builder().name("Transitive Property")
// 			.row(3).column(0).build();
// 	private Skill skillInductionPractice = Skill.builder().name("Induction Practice")
// 			.row(6).column(2).build();
// 	private Skill skillVariablesHidden = Skill.builder().name("Variables extra")
// 			.hidden(true).row(2).column(4).build();
//
// 	private TaskInfo taskInfoRead12 = TaskInfo.builder().name("Read chapter 1.2").time(1).build();
// 	private TaskInfo taskInfoDo12ae = TaskInfo.builder().name("Do exercise 1.2a-e").time(3).build();
// 	private TaskInfo taskInfoRead11 = TaskInfo.builder().name("Read chapter 1.1").time(5).build();
// 	private TaskInfo taskInfoDo11ad = TaskInfo.builder().name("Do exercise 1.1a-d").time(7).build();
// 	private TaskInfo taskInfoRead10 = TaskInfo.builder().name("Read chapter 1.0").time(13).build();
// 	private TaskInfo taskInfoDo10a = TaskInfo.builder().name("Do exercise 1.0a").time(20).build();
// 	private TaskInfo taskInfoBook = TaskInfo.builder().type(TaskType.READING).name("Read chapter 1.3")
// 			.time(15).build();
// 	private TaskInfo taskInfoVideo = TaskInfo.builder().type(TaskType.VIDEO).name("Watch video").time(18)
// 			.build();
//
// 	private RegularTask taskRead12 = RegularTask.builder()
// 			.paths(new HashSet<>(Collections.singletonList(pathFinderPath))).taskInfo(taskInfoRead12).build();
// 	private RegularTask taskDo12ae = RegularTask.builder().taskInfo(taskInfoDo12ae).build();
// 	private RegularTask taskRead11 = RegularTask.builder().taskInfo(taskInfoRead11).build();
// 	private RegularTask taskDo11ad = RegularTask.builder().taskInfo(taskInfoDo11ad).build();
// 	private RegularTask taskRead10 = RegularTask.builder().taskInfo(taskInfoRead10).build();
// 	private RegularTask taskDo10a = RegularTask.builder().taskInfo(taskInfoDo10a).build();
// 	private RegularTask taskBook = RegularTask.builder().taskInfo(taskInfoBook).build();
// 	private RegularTask taskVideo = RegularTask.builder().taskInfo(taskInfoVideo).build();
//
// 	private ChoiceTask choiceTaskBookOrVideo = ChoiceTask.builder()
// 			.tasks(new ArrayList<>(List.of(taskInfoBook, taskInfoVideo))).build();
//
// 	private Badge badge1 = Badge.builder().name("Badge 1").build();
// 	private Badge badge2 = Badge.builder().name("Badge 2").build();
//
// 	private Inventory inventory = Inventory.builder().build();
//
// 	private SCPerson person = SCPerson.builder().id(TestUserDetailsService.id).build();
//
// 	private ClickedLink clickedLink1 = ClickedLink.builder().task(taskRead11).person(person).build();
// 	private ClickedLink clickedLink2 = ClickedLink.builder().task(taskRead11).person(person)
// 			.timestamp(LocalDateTime.of(2023, 1, 1, 0, 0, 0)).build();
// 	private ClickedLink clickedLink3 = ClickedLink.builder().task(taskRead12).person(person).build();
// 	private TaskCompletion completeDo11ad = TaskCompletion.builder().task(taskDo11ad).person(person)
// 			.timestamp(LocalDateTime.of(LocalDate.ofYearDay(2022, 42), LocalTime.MIDNIGHT)).build();
// 	private TaskCompletion completeRead12 = TaskCompletion.builder().task(taskRead12).person(person)
// 			.timestamp(LocalDateTime.of(LocalDate.ofYearDay(2022, 42), LocalTime.MIDNIGHT).plusSeconds(1))
// 			.build();
// 	private TaskCompletion completeDo12ae = TaskCompletion.builder().task(taskDo12ae).person(person)
// 			.timestamp(LocalDateTime.of(LocalDate.ofYearDay(2022, 42), LocalTime.MIDNIGHT).plusSeconds(2))
// 			.build();
// 	private TaskCompletion completeRead11 = TaskCompletion.builder().task(taskRead11).person(person)
// 			.timestamp(LocalDateTime.of(LocalDate.ofYearDay(2022, 42), LocalTime.MIDNIGHT).plusSeconds(3))
// 			.build();
//
// 	public SCCourse getCourseRL() {
// 		return courseRepository.findByIdOrThrow(courseRL.getId());
// 	}
//
// 	public SCEdition getEditionRL() {
// 		return editionRepository.findByIdOrThrow(editionRL2021.getId());
// 	}
//
// 	public Path getPathFinderPath() {
// 		return pathRepository.findByIdOrThrow(pathFinderPath.getId());
// 	}
//
// 	public SCModule getModuleProofTechniques() {
// 		return moduleRepository.findByIdOrThrow(moduleProofTechniques.getId());
// 	}
//
// 	public SCModule getModule() {
// 		return moduleRepository.findByIdOrThrow(module.getId());
// 	}
//
// 	public Submodule getSubmoduleLogicBasics() {
// 		return submoduleRepository.findByIdOrThrow(submoduleLogicBasics.getId());
// 	}
//
// 	public Submodule getSubmoduleGeneralisation() {
// 		return submoduleRepository.findByIdOrThrow(submoduleGeneralisation.getId());
// 	}
//
// 	public Submodule getSubmoduleCases() {
// 		return submoduleRepository.findByIdOrThrow(submoduleCases.getId());
// 	}
//
// 	public Submodule getSubmoduleContradiction() {
// 		return submoduleRepository.findByIdOrThrow(submoduleContradiction.getId());
// 	}
//
// 	public Submodule getSubmoduleContrapositive() {
// 		return submoduleRepository.findByIdOrThrow(submoduleContrapositive.getId());
// 	}
//
// 	public Submodule getSubmoduleInduction() {
// 		return submoduleRepository.findByIdOrThrow(submoduleInduction.getId());
// 	}
//
// 	public Checkpoint getCheckpointLectureOne() {
// 		return checkpointRepository.findByIdOrThrow(checkpointLectureOne.getId());
// 	}
//
// 	public Checkpoint getCheckpointLectureTwo() {
// 		return checkpointRepository.findByIdOrThrow(checkpointLectureTwo.getId());
// 	}
//
// 	public Skill getSkillImplication() {
// 		return skillRepository.findByIdOrThrow(skillImplication.getId());
// 	}
//
// 	public Skill getSkillNegation() {
// 		return skillRepository.findByIdOrThrow(skillNegation.getId());
// 	}
//
// 	public Skill getSkillVariables() {
// 		return skillRepository.findByIdOrThrow(skillVariables.getId());
// 	}
//
// 	public Skill getSkillProofOutline() {
// 		return skillRepository.findByIdOrThrow(skillProofOutline.getId());
// 	}
//
// 	public Skill getSkillAssumption() {
// 		return skillRepository.findByIdOrThrow(skillAssumption.getId());
// 	}
//
// 	public Skill getSkillGeneralisationPractice() {
// 		return skillRepository.findByIdOrThrow(skillGeneralisationPractice.getId());
// 	}
//
// 	public Skill getSkillDividingIntoCases() {
// 		return skillRepository.findByIdOrThrow(skillDividingIntoCases.getId());
// 	}
//
// 	public Skill getSkillCasesPractice() {
// 		return skillRepository.findByIdOrThrow(skillCasesPractice.getId());
// 	}
//
// 	public Skill getSkillContradictionPractice() {
// 		return skillRepository.findByIdOrThrow(skillContradictionPractice.getId());
// 	}
//
// 	public Skill getSkillNegateImplications() {
// 		return skillRepository.findByIdOrThrow(skillNegateImplications.getId());
// 	}
//
// 	public Skill getSkillContrapositivePractice() {
// 		return skillRepository.findByIdOrThrow(skillContrapositivePractice.getId());
// 	}
//
// 	public Skill getSkillTransitiveProperty() {
// 		return skillRepository.findByIdOrThrow(skillTransitiveProperty.getId());
// 	}
//
// 	public Skill getSkillInductionPractice() {
// 		return skillRepository.findByIdOrThrow(skillInductionPractice.getId());
// 	}
//
// 	public Skill getSkillVariablesHidden() {
// 		return skillRepository.findByIdOrThrow(skillVariablesHidden.getId());
// 	}
//
// 	public RegularTask getTaskRead12() {
// 		return regularTaskRepository.findByIdOrThrow(taskRead12.getId());
// 	}
//
// 	public RegularTask getTaskDo12ae() {
// 		return regularTaskRepository.findByIdOrThrow(taskDo12ae.getId());
// 	}
//
// 	public RegularTask getTaskRead11() {
// 		return regularTaskRepository.findByIdOrThrow(taskRead11.getId());
// 	}
//
// 	public RegularTask getTaskDo11ad() {
// 		return regularTaskRepository.findByIdOrThrow(taskDo11ad.getId());
// 	}
//
// 	public RegularTask getTaskRead10() {
// 		return regularTaskRepository.findByIdOrThrow(taskRead10.getId());
// 	}
//
// 	public RegularTask getTaskDo10a() {
// 		return regularTaskRepository.findByIdOrThrow(taskDo10a.getId());
// 	}
//
// 	public RegularTask getTaskVideo() {
// 		return regularTaskRepository.findByIdOrThrow(taskVideo.getId());
// 	}
//
// 	public RegularTask getTaskBook() {
// 		return regularTaskRepository.findByIdOrThrow(taskBook.getId());
// 	}
//
// 	public ChoiceTask getChoiceTaskBookOrVideo() {
// 		return choiceTaskRepository.findByIdOrThrow(choiceTaskBookOrVideo.getId());
// 	}
//
// 	public TaskInfo getTaskInfoRead12() {
// 		return taskInfoRepository.findByIdOrThrow(taskInfoRead12.getId());
// 	}
//
// 	public TaskInfo getTaskInfoDo12ae() {
// 		return taskInfoRepository.findByIdOrThrow(taskInfoDo12ae.getId());
// 	}
//
// 	public TaskInfo getTaskInfoRead11() {
// 		return taskInfoRepository.findByIdOrThrow(taskInfoRead11.getId());
// 	}
//
// 	public TaskInfo getTaskInfoDo11ad() {
// 		return taskInfoRepository.findByIdOrThrow(taskInfoDo11ad.getId());
// 	}
//
// 	public TaskInfo getTaskInfoRead10() {
// 		return taskInfoRepository.findByIdOrThrow(taskInfoRead10.getId());
// 	}
//
// 	public TaskInfo getTaskInfoDo10a() {
// 		return taskInfoRepository.findByIdOrThrow(taskInfoDo10a.getId());
// 	}
//
// 	public TaskInfo getTaskInfoVideo() {
// 		return taskInfoRepository.findByIdOrThrow(taskInfoVideo.getId());
// 	}
//
// 	public TaskInfo getTaskInfoBook() {
// 		return taskInfoRepository.findByIdOrThrow(taskInfoBook.getId());
// 	}
//
// 	public Badge getBadge1() {
// 		return badgeRepository.findByIdOrThrow(badge1.getId());
// 	}
//
// 	public Badge getBadge2() {
// 		return badgeRepository.findByIdOrThrow(badge2.getId());
// 	}
//
// 	public Inventory getInventory() {
// 		return inventoryRepository.findByIdOrThrow(inventory.getId());
// 	}
//
// 	public SCPerson getPerson() {
// 		return personRepository.findByIdOrThrow(person.getId());
// 	}
//
// 	public TaskCompletion getCompleteDo11ad() {
// 		return taskCompletionRepository.findByIdOrThrow(completeDo11ad.getId());
// 	}
//
// 	public TaskCompletion getCompleteRead12() {
// 		return taskCompletionRepository.findByIdOrThrow(completeRead12.getId());
// 	}
//
// 	public TaskCompletion getCompleteDo12ae() {
// 		return taskCompletionRepository.findByIdOrThrow(completeDo12ae.getId());
// 	}
//
// 	public TaskCompletion getCompleteRead11() {
// 		return taskCompletionRepository.findByIdOrThrow(completeRead11.getId());
// 	}
//
// 	@PostConstruct
// 	private void init() {
// 		initCourse();
// 		initEdition();
// 		initPaths();
// 		initModule();
// 		initSubmodule();
// 		initCheckpoint();
// 		initSkill();
// 		initTask();
// 		initPerson();
// 		initClickedLink();
// 		initBadges();
// 		initTaskCompletions();
// 	}
//
// 	private void initCourse() {
// 		courseRL.setId(COURSE_ID);
// 		courseRL = courseRepository.save(courseRL);
// 	}
//
// 	private void initEdition() {
// 		editionRL2021.setId(EDITION_ID);
// 		editionRL2021 = editionRepository.save(editionRL2021);
// 	}
//
// 	private void initPaths() {
// 		pathRepository.save(pathFinderPath);
// 	}
//
// 	private void initModule() {
// 		moduleProofTechniques.setEdition(editionRL2021);
// 		moduleProofTechniques = moduleRepository.save(moduleProofTechniques);
// 		module.setEdition(editionRL2021);
// 		module = moduleRepository.save(module);
// 	}
//
// 	private void initSubmodule() {
// 		submoduleLogicBasics.setModule(moduleProofTechniques);
// 		submoduleLogicBasics = submoduleRepository.save(submoduleLogicBasics);
// 		submoduleGeneralisation.setModule(moduleProofTechniques);
// 		submoduleGeneralisation = submoduleRepository.save(submoduleGeneralisation);
// 		submoduleCases.setModule(moduleProofTechniques);
// 		submoduleCases = submoduleRepository.save(submoduleCases);
// 		submoduleContradiction.setModule(moduleProofTechniques);
// 		submoduleContradiction = submoduleRepository.save(submoduleContradiction);
// 		submoduleContrapositive.setModule(moduleProofTechniques);
// 		submoduleContrapositive = submoduleRepository.save(submoduleContrapositive);
// 		submoduleInduction.setModule(moduleProofTechniques);
// 		submoduleInduction = submoduleRepository.save(submoduleInduction);
// 	}
//
// 	private void initCheckpoint() {
// 		checkpointLectureTwo.setEdition(this.getEditionRL());
// 		checkpointLectureTwo = checkpointRepository.save(checkpointLectureTwo);
//
// 		checkpointLectureOne.setEdition(this.getEditionRL());
// 		checkpointLectureOne = checkpointRepository.save(checkpointLectureOne);
//
// 	}
//
// 	private void initSkill() {
// 		skillImplication.setSubmodule(submoduleLogicBasics);
// 		skillImplication.setCheckpoint(checkpointLectureOne);
// 		skillImplication = skillRepository.save(skillImplication);
// 		skillNegation.setSubmodule(submoduleLogicBasics);
// 		skillNegation.setCheckpoint(checkpointLectureOne);
// 		skillNegation = skillRepository.save(skillNegation);
// 		skillVariables.setSubmodule(submoduleLogicBasics);
// 		skillVariables.setCheckpoint(checkpointLectureOne);
// 		skillVariables = skillRepository.save(skillVariables);
// 		skillProofOutline.setSubmodule(submoduleGeneralisation);
// 		skillProofOutline.setCheckpoint(checkpointLectureOne);
// 		skillProofOutline = skillRepository.save(skillProofOutline);
// 		skillAssumption.setSubmodule(submoduleGeneralisation);
// 		skillAssumption.setCheckpoint(checkpointLectureOne);
// 		skillAssumption.setParents(Set.of(skillImplication));
// 		skillAssumption = skillRepository.save(skillAssumption);
// 		skillGeneralisationPractice.setSubmodule(submoduleGeneralisation);
// 		skillGeneralisationPractice
// 				.setParents(Set.of(skillAssumption, skillProofOutline, skillVariables));
// 		skillGeneralisationPractice.setCheckpoint(checkpointLectureTwo);
// 		skillGeneralisationPractice = skillRepository.save(skillGeneralisationPractice);
// 		skillDividingIntoCases.setSubmodule(submoduleCases);
// 		skillDividingIntoCases.setCheckpoint(checkpointLectureTwo);
// 		skillDividingIntoCases = skillRepository.save(skillDividingIntoCases);
// 		skillCasesPractice.setSubmodule(submoduleCases);
// 		skillCasesPractice.setCheckpoint(checkpointLectureTwo);
// 		skillCasesPractice.setParents(Set.of(skillProofOutline, skillDividingIntoCases));
// 		skillCasesPractice = skillRepository.save(skillCasesPractice);
// 		skillContradictionPractice.setSubmodule(submoduleContradiction);
// 		skillContradictionPractice.setCheckpoint(checkpointLectureTwo);
// 		skillContradictionPractice.setParents(Set.of(skillProofOutline, skillNegation));
// 		skillContradictionPractice = skillRepository.save(skillContradictionPractice);
// 		skillNegateImplications.setSubmodule(submoduleContrapositive);
// 		skillNegateImplications.setCheckpoint(checkpointLectureTwo);
// 		skillNegateImplications.setParents(Set.of(skillNegation, skillImplication));
// 		skillNegateImplications = skillRepository.save(skillNegateImplications);
// 		skillContrapositivePractice.setSubmodule(submoduleContrapositive);
// 		skillContrapositivePractice.setCheckpoint(checkpointLectureTwo);
// 		skillContrapositivePractice.setParents(Set.of(skillProofOutline, skillNegateImplications));
// 		skillContrapositivePractice = skillRepository.save(skillContrapositivePractice);
// 		skillTransitiveProperty.setSubmodule(submoduleInduction);
// 		skillTransitiveProperty.setCheckpoint(checkpointLectureTwo);
// 		skillTransitiveProperty.setParents(Set.of(skillImplication));
// 		skillTransitiveProperty = skillRepository.save(skillTransitiveProperty);
// 		skillInductionPractice.setSubmodule(submoduleInduction);
// 		skillInductionPractice.setCheckpoint(checkpointLectureTwo);
// 		skillInductionPractice
// 				.setParents(Set.of(skillTransitiveProperty, skillProofOutline, skillDividingIntoCases));
// 		skillInductionPractice = skillRepository.save(skillInductionPractice);
// 		skillVariablesHidden.setSubmodule(submoduleLogicBasics);
// 		skillVariablesHidden.setCheckpoint(checkpointLectureTwo);
// 		skillVariablesHidden.setParents(Set.of(skillVariables));
// 		skillVariablesHidden = skillRepository.save(skillVariablesHidden);
//
// 		checkpointLectureOne = checkpointRepository.save(checkpointLectureOne);
// 		checkpointLectureTwo = checkpointRepository.save(checkpointLectureTwo);
// 	}
//
// 	private void initTask() {
// 		taskRead12.setSkill(skillImplication);
// 		taskRead12.setTaskInfo(taskInfoRead12);
// 		taskInfoRead12.setTask(taskRead12);
// 		taskRead12 = regularTaskRepository.save(taskRead12);
// 		taskInfoRead12 = taskInfoRepository.save(taskInfoRead12);
//
// 		taskDo12ae.setSkill(skillImplication);
// 		taskDo12ae.setTaskInfo(taskInfoDo12ae);
// 		taskInfoDo12ae.setTask(taskDo12ae);
// 		taskDo12ae = regularTaskRepository.save(taskDo12ae);
// 		taskInfoDo12ae = taskInfoRepository.save(taskInfoDo12ae);
//
// 		taskRead11.setSkill(skillNegation);
// 		taskRead11.setTaskInfo(taskInfoRead11);
// 		taskInfoRead11.setTask(taskRead11);
// 		taskRead11 = regularTaskRepository.save(taskRead11);
// 		taskInfoRead11 = taskInfoRepository.save(taskInfoRead11);
//
// 		taskDo11ad.setSkill(skillNegation);
// 		taskDo11ad.setTaskInfo(taskInfoDo11ad);
// 		taskInfoDo11ad.setTask(taskDo11ad);
// 		taskDo11ad = regularTaskRepository.save(taskDo11ad);
// 		taskInfoDo11ad = taskInfoRepository.save(taskInfoDo11ad);
//
// 		taskRead10.setSkill(skillVariables);
// 		taskRead10.setTaskInfo(taskInfoRead10);
// 		taskRead10.setRequiredFor(Set.of(skillVariablesHidden));
// 		taskInfoRead10.setTask(taskRead10);
// 		taskRead10 = regularTaskRepository.save(taskRead10);
// 		taskInfoRead10 = taskInfoRepository.save(taskInfoRead10);
//
// 		taskDo10a.setSkill(skillVariables);
// 		taskDo10a.setTaskInfo(taskInfoDo10a);
// 		taskDo10a.setRequiredFor(Set.of(skillVariablesHidden));
// 		taskInfoDo10a.setTask(taskDo10a);
// 		taskDo10a = regularTaskRepository.save(taskDo10a);
// 		taskInfoDo10a = taskInfoRepository.save(taskInfoDo10a);
//
// 		taskVideo.setSkill(skillVariables);
// 		taskVideo.setTaskInfo(taskInfoVideo);
// 		taskInfoVideo.setTask(taskVideo);
// 		taskVideo = regularTaskRepository.save(taskVideo);
// 		taskInfoVideo = taskInfoRepository.save(taskInfoVideo);
//
// 		taskBook.setSkill(skillVariables);
// 		taskBook.setTaskInfo(taskInfoBook);
// 		taskInfoBook.setTask(taskBook);
// 		taskBook = regularTaskRepository.save(taskBook);
// 		taskInfoBook = taskInfoRepository.save(taskInfoBook);
//
// 		choiceTaskBookOrVideo.setSkill(skillVariables);
// 		choiceTaskBookOrVideo = choiceTaskRepository.save(choiceTaskBookOrVideo);
// 	}
//
// 	private void initPerson() {
// 		inventory.setPerson(person);
// 		person.setInventory(inventory);
//
// 		person = personRepository.save(person);
// 		inventory = person.getInventory();
// 	}
//
// 	private void initClickedLink() {
// 		clickedLinkRepository.save(clickedLink1);
// 		clickedLinkRepository.save(clickedLink2);
// 		clickedLinkRepository.save(clickedLink3);
// 	}
//
// 	private void initTaskCompletions() {
// 		completeDo11ad = taskCompletionRepository.save(completeDo11ad);
// 		completeRead12 = taskCompletionRepository.save(completeRead12);
// 		completeDo12ae = taskCompletionRepository.save(completeDo12ae);
// 		completeRead11 = taskCompletionRepository.save(completeRead11);
//
// 		person.setTaskCompletions(Set.of(completeDo11ad, completeRead12, completeDo12ae, completeRead11));
// 		taskDo11ad.getCompletedBy().add(completeDo11ad);
// 		taskRead12.getCompletedBy().add(completeRead12);
// 		taskDo12ae.getCompletedBy().add(completeDo12ae);
// 		taskRead11.getCompletedBy().add(completeRead11);
// 	}
//
// 	private void initBadges() {
// 		badge1 = badgeRepository.save(badge1);
// 		badge2 = badgeRepository.save(badge2);
// 	}
//
// 	/**
// 	 * Creates an external skill for testing purposes.
// 	 *
// 	 * @param  linksTo The skill the external skill should link to.
// 	 * @return         The created external skill.
// 	 */
// 	public ExternalSkill createExternalSkill(Skill linksTo) {
// 		// Create a new module for the external skill, in the same edition
// 		SCModule module = moduleRepository.save(SCModule.builder().edition(editionRL2021)
// 				.name("New module").build());
// 		// Create an external skill referencing SkillAssumption
// 		ExternalSkill externalSkill = ExternalSkill.builder().skill(linksTo).module(module)
// 				.row(0).column(0).build();
// 		externalSkill = externalSkillRepository.save(externalSkill);
//
// 		return externalSkill;
// 	}
//
// 	/**
// 	 * Creates a skill in a new edition, with a given edition id. The submodule, module and checkpoint are
// 	 * also created/saved.
// 	 *
// 	 * @param  editionId The id of the new edition.
// 	 * @param  visible   Whether the edition is visible.
// 	 * @return           The skill created within the new edition.
// 	 */
// 	public Skill createSkillInEditionHelper(Long editionId, boolean visible) {
// 		SCEdition edition = SCEdition.builder().id(editionId).isVisible(visible).build();
// 		edition = editionRepository.save(edition);
// 		SCModule module = SCModule.builder()
// 				.name("Module in " + editionId).edition(edition).build();
// 		module = moduleRepository.save(module);
// 		edition.getModules().add(module);
// 		Submodule submodule = Submodule.builder().module(module)
// 				.name("Submodule in " + editionId).column(0).row(0).build();
// 		submodule = submoduleRepository.save(submodule);
// 		module.getSubmodules().add(submodule);
// 		LocalDateTime localDateTime = LocalDateTime.of(2023, 1, 10, 10, 10, 0);
// 		Checkpoint checkpoint = Checkpoint.builder().edition(edition)
// 				.name("Checkpoint in " + editionId).deadline(localDateTime).build();
// 		checkpoint = checkpointRepository.save(checkpoint);
// 		edition.getCheckpoints().add(checkpoint);
// 		Skill skill = Skill.builder().submodule(submodule).checkpoint(checkpoint)
// 				.name("Skill in " + editionId).column(0).row(0).build();
// 		skill = skillRepository.save(skill);
// 		checkpoint.getSkills().add(skill);
// 		submodule.getSkills().add(skill);
//
// 		return skill;
// 	}
//
// 	/**
// 	 * Reset the task completions that were initialized on load.
// 	 */
// 	public void resetTaskCompletions() {
// 		person.setTaskCompletions(new HashSet<>());
// 		taskDo11ad.setCompletedBy(new HashSet<>());
// 		taskRead12.setCompletedBy(new HashSet<>());
// 		taskDo12ae.setCompletedBy(new HashSet<>());
// 		taskRead11.setCompletedBy(new HashSet<>());
// 		taskCompletionRepository.deleteAll();
// 	}
//
// 	/**
// 	 * Helper method for database setup. Creates a new module with 2 skills, 2 checkpoints (given as
// 	 * parameters) and 1 submodule.
// 	 *
// 	 * @param checkpointA The first checkpoint.
// 	 * @param checkpointB The last checkpoint.
// 	 */
// 	public void createSkillsInNewModuleHelper(Checkpoint checkpointA, Checkpoint checkpointB) {
// 		SCModule module = SCModule.builder().name("Module").edition(getEditionRL()).build();
// 		Submodule submodule = Submodule.builder().name("Submodule").module(module).row(0).column(0).build();
// 		getEditionRL().getModules().add(module);
// 		moduleRepository.save(module);
// 		editionRepository.save(getEditionRL());
// 		submoduleRepository.save(submodule);
// 		module.getSubmodules().add(submodule);
// 		moduleRepository.save(module);
// 		Skill skillA = Skill.builder().name("Skill A").row(0).column(0).checkpoint(checkpointA)
// 				.submodule(submodule)
// 				.build();
// 		Skill skillB = Skill.builder().name("Skill B").row(1).column(0).checkpoint(checkpointB)
// 				.submodule(submodule)
// 				.build();
// 		submodule.getSkills().addAll(Set.of(skillA, skillB));
// 		skillRepository.saveAll(Set.of(skillA, skillB));
// 		submoduleRepository.save(submodule);
// 		checkpointA.getSkills().add(skillA);
// 		checkpointB.getSkills().add(skillB);
// 		checkpointRepository.saveAll(Set.of(checkpointA, checkpointB));
// 	}
//
// 	/**
// 	 * Creates a task in a new module (in edition editionRL2021) for testing purposes. Adds a new module,
// 	 * submodule and skill.
// 	 *
// 	 * @return The created task.
// 	 */
// 	public RegularTask createTaskInNewModule() {
// 		SCModule module = moduleRepository.save(SCModule.builder().edition(editionRL2021)
// 				.name("New module").build());
// 		editionRL2021.getModules().add(module);
// 		editionRepository.save(editionRL2021);
// 		Submodule submodule = Submodule.builder().module(module)
// 				.name("New submodule").column(0).row(0).build();
// 		submodule = submoduleRepository.save(submodule);
// 		module.getSubmodules().add(submodule);
// 		Skill skill = Skill.builder().submodule(submodule).checkpoint(getCheckpointLectureOne())
// 				.name("New skill").column(0).row(0).build();
// 		skill = skillRepository.save(skill);
// 		getCheckpointLectureOne().getSkills().add(skill);
// 		submodule.getSkills().add(skill);
// 		submoduleRepository.save(submodule);
// 		checkpointRepository.save(getCheckpointLectureOne());
// 		TaskInfo taskInfo = TaskInfo.builder().name("New task").build();
// 		RegularTask task = RegularTask.builder().taskInfo(taskInfo).skill(skill).build();
// 		taskInfo.setTask(task);
// 		taskInfoRepository.save(taskInfo);
// 		regularTaskRepository.save(task);
// 		skill.getTasks().add(task);
// 		skillRepository.save(skill);
// 		return task;
// 	}
//
// 	/**
// 	 * Create and save a regular task by its Skill and name. The remainder of the fields are left at the
// 	 * default values.
// 	 *
// 	 * @param  skill The Skill the Task should be added to.
// 	 * @param  name  The name of the Task.
// 	 * @return       The saved RegularTask.
// 	 */
// 	public RegularTask createTaskBySkillAndName(Skill skill, String name) {
// 		TaskInfo taskInfo = TaskInfo.builder().name(name).build();
// 		RegularTask task = RegularTask.builder().taskInfo(taskInfo).skill(skill).build();
// 		taskInfo.setTask(task);
// 		task = regularTaskRepository.save(task);
// 		skill.getTasks().add(task);
// 		skillRepository.save(skill);
// 		return task;
// 	}
// }
