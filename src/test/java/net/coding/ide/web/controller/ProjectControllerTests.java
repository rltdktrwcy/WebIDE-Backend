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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class ProjectControllerTests {

    @Mock
    private ProjectService projectService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProjectController projectController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetProjects() {
        // Setup test data
        ProjectEntity project1 = new ProjectEntity();
        List<ProjectEntity> projects = Arrays.asList(project1);

        ProjectDTO dto1 = new ProjectDTO();
        dto1.setId(1L);
        dto1.setName("Project 1");

        // Setup mocks
        when(projectService.projects()).thenReturn(projects);
        when(modelMapper.map(project1, ProjectDTO.class)).thenReturn(dto1);

        // Execute
        List<ProjectDTO> result = projectController.projects();

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dto1.getId(), result.get(0).getId());
        assertEquals(dto1.getName(), result.get(0).getName());

        verify(projectService).projects();
        verify(modelMapper).map(any(ProjectEntity.class), eq(ProjectDTO.class));
    }

    @Test
    public void testGetProjectsEmpty() {
        // Setup
        when(projectService.projects()).thenReturn(Collections.emptyList());

        // Execute
        List<ProjectDTO> result = projectController.projects();

        // Verify
        assertNotNull(result);
        assertEquals(0, result.size());

        verify(projectService).projects();
        verify(modelMapper, never()).map(any(), any());
    }
}
