package net.coding.ide.service;

import com.google.common.collect.Lists;
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

    private List<ProjectEntity> projectEntities;

    @Before
    public void setUp() {
        projectEntities = new ArrayList<>();
        ProjectEntity project1 = new ProjectEntity();
        project1.setId(1L);
        project1.setName("Project1");
        project1.setFullName("Full Project1");
        project1.setUrl("http://project1.com");
        project1.setIconUrl("http://project1.com/icon.png");
        project1.setOwnerName("owner1");

        ProjectEntity project2 = new ProjectEntity();
        project2.setId(2L);
        project2.setName("Project2");
        project2.setFullName("Full Project2");
        project2.setUrl("http://project2.com");
        project2.setIconUrl("http://project2.com/icon.png");
        project2.setOwnerName("owner2");

        projectEntities.add(project1);
        projectEntities.add(project2);
    }

    @Test
    public void testProjects() {
        when(projectRepository.findAll()).thenReturn(projectEntities);

        List<ProjectEntity> result = projectService.projects();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Project1", result.get(0).getName());
        assertEquals("Project2", result.get(1).getName());
    }

    @Test
    public void testProjectsWithEmptyList() {
        List<ProjectEntity> emptyList = new ArrayList<>();
        when(projectRepository.findAll()).thenReturn(emptyList);

        List<ProjectEntity> result = projectService.projects();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testProjectsWithNull() {
        when(projectRepository.findAll()).thenReturn(new ArrayList<>());

        List<ProjectEntity> result = projectService.projects();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
