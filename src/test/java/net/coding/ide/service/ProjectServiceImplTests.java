package net.coding.ide.service;

import net.coding.ide.entity.ProjectEntity;
import net.coding.ide.repository.ProjectRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import java.util.Arrays;
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

    private ProjectEntity project1;
    private ProjectEntity project2;

    @Before
    public void setUp() {
        project1 = new ProjectEntity();
        project1.setId(1L);
        project1.setName("project1");
        project1.setFullName("test/project1");
        project1.setUrl("git@test.com:test/project1.git");

        project2 = new ProjectEntity();
        project2.setId(2L);
        project2.setName("project2");
        project2.setFullName("test/project2");
        project2.setUrl("git@test.com:test/project2.git");
    }

    @Test
    public void testProjects() {
        when(projectRepository.findAll()).thenReturn(Arrays.asList(project1, project2));

        List<ProjectEntity> projects = projectService.projects();

        assertNotNull(projects);
        assertEquals(2, projects.size());
        assertEquals(project1.getName(), projects.get(0).getName());
        assertEquals(project2.getName(), projects.get(1).getName());
    }

    @Test
    public void testProjectsEmpty() {
        when(projectRepository.findAll()).thenReturn(Arrays.asList());

        List<ProjectEntity> projects = projectService.projects();

        assertNotNull(projects);
        assertEquals(0, projects.size());
    }
}
