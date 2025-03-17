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
import static org.junit.Assert.assertNotNull;
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
        project1.setName("project1");
        project1.setFullName("user/project1");
        project1.setUrl("git@example.com:user/project1.git");
        projectList.add(project1);

        ProjectEntity project2 = new ProjectEntity();
        project2.setId(2L);
        project2.setName("project2");
        project2.setFullName("user/project2");
        project2.setUrl("git@example.com:user/project2.git");
        projectList.add(project2);
    }

    @Test
    public void testProjects() {
        when(projectRepository.findAll()).thenReturn(projectList);

        List<ProjectEntity> result = projectService.projects();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("project1", result.get(0).getName());
        assertEquals("project2", result.get(1).getName());
    }

    @Test
    public void testProjectsEmpty() {
        when(projectRepository.findAll()).thenReturn(new ArrayList<>());

        List<ProjectEntity> result = projectService.projects();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
