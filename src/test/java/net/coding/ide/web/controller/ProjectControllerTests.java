package net.coding.ide.web.controller;

import net.coding.ide.dto.ProjectDTO;
import net.coding.ide.entity.ProjectEntity;
import net.coding.ide.service.ProjectService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectControllerTests {

    @Mock
    private ModelMapper mapper;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController controller;

    private ProjectEntity project1;
    private ProjectEntity project2;
    private ProjectDTO projectDTO1;
    private ProjectDTO projectDTO2;

    @Before
    public void setUp() {
        project1 = new ProjectEntity();
        project1.setId(1L);
        project1.setName("Project 1");

        project2 = new ProjectEntity();
        project2.setId(2L);
        project2.setName("Project 2");

        projectDTO1 = new ProjectDTO();
        projectDTO1.setId(1L);
        projectDTO1.setName("Project 1");

        projectDTO2 = new ProjectDTO();
        projectDTO2.setId(2L);
        projectDTO2.setName("Project 2");
    }

    @Test
    public void testGetProjects() {
        List<ProjectEntity> projects = Arrays.asList(project1, project2);
        when(projectService.projects()).thenReturn(projects);
        when(mapper.map(project1, ProjectDTO.class)).thenReturn(projectDTO1);
        when(mapper.map(project2, ProjectDTO.class)).thenReturn(projectDTO2);

        List<ProjectDTO> result = controller.projects();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(projectDTO1.getId(), result.get(0).getId());
        assertEquals(projectDTO1.getName(), result.get(0).getName());
        assertEquals(projectDTO2.getId(), result.get(1).getId());
        assertEquals(projectDTO2.getName(), result.get(1).getName());
    }

    @Test
    public void testGetEmptyProjects() {
        when(projectService.projects()).thenReturn(Arrays.asList());

        List<ProjectDTO> result = controller.projects();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
