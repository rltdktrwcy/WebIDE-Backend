package net.coding.ide.service;

import net.coding.ide.entity.ProjectEntity;
import net.coding.ide.repository.ProjectRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceImplTests {

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Mock
    private ProjectRepository projectRepository;

    private List<ProjectEntity> mockProjects;

    @Before
    public void setUp() {
        // Create mock project data
        mockProjects = new ArrayList<>();

        ProjectEntity project1 = new ProjectEntity();
        project1.setId(1L);
        project1.setName("Project 1");
        project1.setFullName("Full Project 1");
        project1.setUrl("http://example.com/project1");
        project1.setOwnerName("owner1");

        ProjectEntity project2 = new ProjectEntity();
        project2.setId(2L);
        project2.setName("Project 2");
        project2.setFullName("Full Project 2");
        project2.setUrl("http://example.com/project2");
        project2.setOwnerName("owner2");

        mockProjects.addAll(Arrays.asList(project1, project2));
    }

    @Test
    public void testProjects() {
        // Setup
        when(projectRepository.findAll()).thenReturn(mockProjects);

        // Execute
        List<ProjectEntity> result = projectService.projects();

        // Verify
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 2 projects", 2, result.size());
        assertEquals("First project should match", mockProjects.get(0), result.get(0));
        assertEquals("Second project should match", mockProjects.get(1), result.get(1));
    }

    @Test
    public void testProjectsWithEmptyList() {
        // Setup
        when(projectRepository.findAll()).thenReturn(new ArrayList<>());

        // Execute
        List<ProjectEntity> result = projectService.projects();

        // Verify
        assertNotNull("Result should not be null", result);
        assertEquals("Should return empty list", 0, result.size());
    }
}
