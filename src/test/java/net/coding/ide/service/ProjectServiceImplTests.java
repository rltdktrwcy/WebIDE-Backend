package net.coding.ide.service;

import net.coding.ide.entity.ProjectEntity;
import net.coding.ide.repository.ProjectRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ProjectServiceImplTests {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private ProjectEntity project1;
    private ProjectEntity project2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        project1 = new ProjectEntity();
        project1.setId(1L);
        project1.setName("Project 1");
        project1.setFullName("Full Project 1");
        project1.setUrl("http://project1.url");

        project2 = new ProjectEntity();
        project2.setId(2L);
        project2.setName("Project 2");
        project2.setFullName("Full Project 2");
        project2.setUrl("http://project2.url");
    }

    @Test
    public void projects_ShouldReturnAllProjects() {
        List<ProjectEntity> expectedProjects = Arrays.asList(project1, project2);
        when(projectRepository.findAll()).thenReturn(expectedProjects);

        List<ProjectEntity> actualProjects = projectService.projects();

        assertEquals(expectedProjects.size(), actualProjects.size());
        assertEquals(expectedProjects.get(0), actualProjects.get(0));
        assertEquals(expectedProjects.get(1), actualProjects.get(1));
    }

    @Test
    public void projects_ShouldReturnEmptyList_WhenNoProjects() {
        List<ProjectEntity> emptyList = Arrays.asList();
        when(projectRepository.findAll()).thenReturn(emptyList);

        List<ProjectEntity> actualProjects = projectService.projects();

        assertEquals(0, actualProjects.size());
    }
}
