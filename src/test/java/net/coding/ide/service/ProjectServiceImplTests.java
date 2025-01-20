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
import static org.mockito.Mockito.verify;
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
        ProjectEntity project = new ProjectEntity();
        project.setId(1L);
        project.setName("test-project");
        project.setFullName("test/test-project");
        project.setUrl("git@test.com:test/test-project.git");
        projectList.add(project);
    }

    @Test
    public void projects_ShouldReturnEmptyList_WhenNoProjects() {
        when(projectRepository.findAll()).thenReturn(new ArrayList<>());

        List<ProjectEntity> result = projectService.projects();

        verify(projectRepository).findAll();
        assertEquals(0, result.size());
    }

    @Test
    public void projects_ShouldReturnProjectList_WhenProjectsExist() {
        when(projectRepository.findAll()).thenReturn(projectList);

        List<ProjectEntity> result = projectService.projects();

        verify(projectRepository).findAll();
        assertEquals(1, result.size());
        assertEquals("test-project", result.get(0).getName());
        assertEquals("test/test-project", result.get(0).getFullName());
        assertEquals("git@test.com:test/test-project.git", result.get(0).getUrl());
    }
}
