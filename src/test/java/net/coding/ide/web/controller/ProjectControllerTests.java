package net.coding.ide.web.controller;

import net.coding.ide.dto.ProjectDTO;
import net.coding.ide.entity.ProjectEntity;
import net.coding.ide.service.ProjectService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ProjectControllerTests {

    @Mock
    private ModelMapper mapper;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProjectsWithData() {
        // Setup test data
        ProjectEntity project1 = new ProjectEntity();
        ProjectEntity project2 = new ProjectEntity();
        List<ProjectEntity> projects = Arrays.asList(project1, project2);

        ProjectDTO dto1 = new ProjectDTO();
        dto1.setId(1L);
        dto1.setName("Project 1");

        ProjectDTO dto2 = new ProjectDTO();
        dto2.setId(2L);
        dto2.setName("Project 1");

        // Setup mocks
        when(projectService.projects()).thenReturn(projects);
        when(mapper.map(project1, ProjectDTO.class)).thenReturn(dto1);
        when(mapper.map(project2, ProjectDTO.class)).thenReturn(dto2);

        // Execute
        List<ProjectDTO> result = projectController.projects();

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Project 1", result.get(0).getName());
        assertEquals("Project 1", result.get(1).getName());
        verify(projectService).projects();
    }

    @Test
    public void testProjectsWithEmptyList() {
        // Setup
        when(projectService.projects()).thenReturn(new ArrayList<>());

        // Execute
        List<ProjectDTO> result = projectController.projects();

        // Verify
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(projectService).projects();
    }
}
