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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ProjectControllerTests {

    @Mock
    private ProjectService projectService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProjectController projectController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();
    }

    @Test
    public void testProjects() {
        // Setup test data
        ProjectEntity project1 = new ProjectEntity();
        ProjectEntity project2 = new ProjectEntity();
        List<ProjectEntity> projectList = Arrays.asList(project1, project2);

        ProjectDTO dto1 = new ProjectDTO();
        ProjectDTO dto2 = new ProjectDTO();

        // Setup mock behavior
        when(projectService.projects()).thenReturn(projectList);
        when(modelMapper.map(project1, ProjectDTO.class)).thenReturn(dto1);
        when(modelMapper.map(project2, ProjectDTO.class)).thenReturn(dto2);

        // Execute test
        List<ProjectDTO> result = projectController.projects();

        // Verify
        verify(projectService).projects();
        verify(modelMapper, times(2)).map(any(ProjectEntity.class), eq(ProjectDTO.class));
        assertEquals(2, result.size());
    }

    @Test
    public void testProjectsWithEmptyList() {
        // Setup mock behavior
        when(projectService.projects()).thenReturn(new ArrayList<>());

        // Execute test
        List<ProjectDTO> result = projectController.projects();

        // Verify
        verify(projectService).projects();
        verify(modelMapper, never()).map(any(), any());
        assertTrue(result.isEmpty());
    }
}
