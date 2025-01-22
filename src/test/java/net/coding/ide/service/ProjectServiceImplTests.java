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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceImplTests {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private List<ProjectEntity> projectList;

    @Before
    public void setUp() {
        projectList = new ArrayList<>();
        ProjectEntity project1 = new ProjectEntity();
        project1.setId(1L);
        project1.setName("Project 1");
        project1.setFullName("Test Project 1");
        project1.setUrl("http://test1.com");

        ProjectEntity project2 = new ProjectEntity();
        project2.setId(2L);
        project2.setName("Project 2");
        project2.setFullName("Test Project 2");
        project2.setUrl("http://test2.com");

        projectList.add(project1);
        projectList.add(project2);
    }

    @Test
    public void testProjects() {
        when(projectRepository.findAll()).thenReturn(projectList);

        List<ProjectEntity> result = projectService.projects();

        assertEquals(2, result.size());
        assertEquals("Project 1", result.get(0).getName());
        assertEquals("Project 2", result.get(1).getName());
        assertEquals("http://test1.com", result.get(0).getUrl());
        assertEquals("http://test2.com", result.get(1).getUrl());
    }

    @Test
    public void testProjectsEmpty() {
        when(projectRepository.findAll()).thenReturn(new ArrayList<>());

        List<ProjectEntity> result = projectService.projects();

        assertEquals(0, result.size());
    }
}
