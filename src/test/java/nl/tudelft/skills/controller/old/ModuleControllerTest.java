// /*
//  * Skill Circuits
//  * Copyright (C) 2022 - Delft University of Technology
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
// package nl.tudelft.skills.controller.old;
//
// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.transaction.annotation.Transactional;
//
// import nl.tudelft.skills.TestSkillCircuitsApplication;
//
// @Transactional
// @AutoConfigureMockMvc
// @SpringBootTest(classes = TestSkillCircuitsApplication.class)
// public class ModuleControllerTest extends ControllerTest {
//
// 	//	@MockBean
// 	//	private ModuleService moduleService;
// 	//	private final TaskCompletionService taskCompletionService;
// 	//	private final TaskCompletionRepository taskCompletionRepository;
// 	//	private final ModuleController moduleController;
// 	//	private final RoleControllerApi roleControllerApi;
// 	//	private final ModuleRepository moduleRepository;
// 	//	private final HttpSession session;
// 	//	private final ClickedLinkService clickedLinkService;
// 	//	private final ClickedLinkRepository clickedLinkRepository;
// 	//
// 	//	//	Playlist feature
// 	//	private final ResearchParticipantService researchParticipantService;
// 	//	private final AuthorisationService authorisationService;
// 	//
// 	//	@Autowired
// 	//	public ModuleControllerTest(ModuleController moduleController, RoleControllerApi roleControllerApi,
// 	//			ModuleRepository moduleRepository, TaskCompletionService taskCompletionService,
// 	//			TaskCompletionRepository taskCompletionRepository, ClickedLinkService clickedLinkService,
// 	//			ClickedLinkRepository clickedLinkRepository,
// 	//			ResearchParticipantService researchParticipantService,
// 	//			AuthorisationService authorisationService) {
// 	//		this.moduleController = moduleController;
// 	//		this.roleControllerApi = roleControllerApi;
// 	//		this.moduleRepository = moduleRepository;
// 	//		this.taskCompletionRepository = taskCompletionRepository;
// 	//		this.taskCompletionService = taskCompletionService;
// 	//		this.clickedLinkService = clickedLinkService;
// 	//		this.clickedLinkRepository = clickedLinkRepository;
// 	//		this.researchParticipantService = researchParticipantService;
// 	//		this.authorisationService = authorisationService;
// 	//		this.session = mock(HttpSession.class);
// 	//	}
// 	//
// 	//	@Test
// 	//	@WithUserDetails("admin")
// 	//	void createModule() throws Exception {
// 	//		String element = mvc.perform(post("/module").with(csrf())
// 	//				.content(getModuleCreateFormData())
// 	//				.contentType(MediaType.APPLICATION_FORM_URLENCODED))
// 	//				.andExpect(status().isOk())
// 	//				.andReturn().getResponse().getContentAsString();
// 	//
// 	//		Matcher idMatcher = Pattern.compile("id=\"module-(\\d+)\"").matcher(element);
// 	//		assertThat(idMatcher.find()).isTrue();
// 	//
// 	//		Long id = Long.parseLong(idMatcher.group(1));
// 	//		assertThat(moduleRepository.existsById(id)).isTrue();
// 	//
// 	//		assertThat(element.replaceAll("\n\\s+", "\n"))
// 	//				.contains("<h2\nid=\"module-" + id + "-name\"\nclass=\"module-title\">Module</h2>");
// 	//	}
// 	//
// 	//	@Test
// 	//	void createModuleSetup() throws Exception {
// 	//		new ModuleController(moduleRepository, moduleService,
// 	//				session, taskCompletionService, clickedLinkService, researchParticipantService,
// 	//				authorisationService)
// 	//				.createModuleInEditionSetup(
// 	//						SCModuleCreateDTO.builder()
// 	//								.name("Module").edition(new SCEditionIdDTO(db.getEditionRL().getId()))
// 	//								.build(),
// 	//						Mockito.mock(Model.class));
// 	//
// 	//		assertThat(moduleRepository.findAll().stream()
// 	//				.filter(m -> m.getName().equals("Module")).findFirst()).isNotEmpty();
// 	//	}
// 	//
// 	//	private String getModuleCreateFormData() throws Exception {
// 	//		return EntityUtils.toString(new UrlEncodedFormEntity(List.of(
// 	//				new BasicNameValuePair("name", "Module"),
// 	//				new BasicNameValuePair("edition.id", Long.toString(db.getEditionRL().getId())))));
// 	//	}
// 	//
// 	//	@Test
// 	//	void deleteModule() {
// 	//		Long moduleId = db.getModuleProofTechniques().getId();
// 	//
// 	//		assertThat(moduleRepository.existsById(moduleId)).isTrue();
// 	//
// 	//		new ModuleController(moduleRepository, moduleService,
// 	//				session, taskCompletionService, clickedLinkService, researchParticipantService,
// 	//				authorisationService)
// 	//				.deleteModule(moduleId);
// 	//
// 	//		assertThat(moduleRepository.existsById(moduleId)).isFalse();
// 	//		assertThat(taskCompletionRepository.findAll()).hasSize(0);
// 	//		assertThat(clickedLinkRepository.findAll()).hasSize(0);
// 	//	}
// 	//
// 	//	@Test
// 	//	void deleteModuleSetup() {
// 	//		Long moduleId = db.getModuleProofTechniques().getId();
// 	//
// 	//		assertThat(moduleRepository.existsById(moduleId)).isTrue();
// 	//
// 	//		new ModuleController(moduleRepository, moduleService,
// 	//				session, taskCompletionService, clickedLinkService, researchParticipantService,
// 	//				authorisationService)
// 	//				.deleteModuleSetup(moduleId);
// 	//
// 	//		assertThat(moduleRepository.existsById(moduleId)).isFalse();
// 	//		assertThat(taskCompletionRepository.findAll()).hasSize(0);
// 	//		assertThat(clickedLinkRepository.findAll()).hasSize(0);
// 	//	}
// 	//
// 	//	@Test
// 	//	void patchModule() {
// 	//		new ModuleController(moduleRepository, moduleService,
// 	//				session, taskCompletionService, clickedLinkService, researchParticipantService,
// 	//				authorisationService).patchModule(
// 	//						SCModulePatchDTO.builder()
// 	//								.id(db.getModuleProofTechniques().getId())
// 	//								.name("Module 2.0")
// 	//								.edition(new SCEditionIdDTO(
// 	//										db.getModuleProofTechniques().getEdition().getId()))
// 	//								.build());
// 	//
// 	//		SCModule module = moduleRepository.findByIdOrThrow(db.getModuleProofTechniques().getId());
// 	//
// 	//		assertThat(module.getName()).isEqualTo("Module 2.0");
// 	//	}
// 	//
// 	//	@Test
// 	//	void toggleStudentMode() {
// 	//		ModuleController moduleController = new ModuleController(moduleRepository, moduleService,
// 	//				session, taskCompletionService, clickedLinkService, researchParticipantService,
// 	//				authorisationService);
// 	//		when(session.getAttribute("student-mode-" + db.getEditionRL().getId()))
// 	//				.thenReturn(null)
// 	//				.thenReturn(true)
// 	//				.thenReturn(false);
// 	//		moduleController.toggleStudentMode(db.getModuleProofTechniques().getId());
// 	//		verify(session).setAttribute("student-mode-" + db.getEditionRL().getId(), true);
// 	//		moduleController.toggleStudentMode(db.getModuleProofTechniques().getId());
// 	//		verify(session).setAttribute("student-mode-" + db.getEditionRL().getId(), false);
// 	//		moduleController.toggleStudentMode(db.getModuleProofTechniques().getId());
// 	//		verify(session, times(2)).setAttribute("student-mode-" + db.getEditionRL().getId(), true);
// 	//	}
// 	//
// 	//	@Test
// 	//	void getSkillsOfModule() {
// 	//		ModuleController moduleController = new ModuleController(moduleRepository, moduleService,
// 	//				session, taskCompletionService, clickedLinkService, researchParticipantService,
// 	//				authorisationService);
// 	//		assertThat(moduleController.getSkillsOfModule(db.getModuleProofTechniques().getId()))
// 	//				.containsExactly(Stream
// 	//						.of(
// 	//								db.getSkillAssumption(),
// 	//								db.getSkillCasesPractice(),
// 	//								db.getSkillContradictionPractice(),
// 	//								db.getSkillContrapositivePractice(),
// 	//								db.getSkillDividingIntoCases(),
// 	//								db.getSkillGeneralisationPractice(),
// 	//								db.getSkillImplication(),
// 	//								db.getSkillInductionPractice(),
// 	//								db.getSkillNegateImplications(),
// 	//								db.getSkillNegation(),
// 	//								db.getSkillProofOutline(),
// 	//								db.getSkillTransitiveProperty(),
// 	//								db.getSkillVariables(),
// 	//								db.getSkillVariablesHidden())
// 	//						.map(s -> mapper.map(s, SkillSummaryDTO.class)).toList()
// 	//						.toArray(new SkillSummaryDTO[0]));
// 	//	}
// }
