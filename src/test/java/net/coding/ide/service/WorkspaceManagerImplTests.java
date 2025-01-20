package net.coding.ide.service;

import net.coding.ide.dto.FileDTO;
import net.coding.ide.entity.ProjectEntity;
import net.coding.ide.entity.WorkspaceEntity;
import net.coding.ide.model.FileInfo;
import net.coding.ide.model.FileSearchResultEntry;
import net.coding.ide.model.Workspace;
import net.coding.ide.model.exception.WorkspaceDeletedException;
import net.coding.ide.model.exception.WorkspaceMissingException;
import net.coding.ide.repository.ProjectRepository;
import net.coding.ide.repository.WorkspaceRepository;
import net.coding.ide.utils.RandomGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static net.coding.ide.entity.WorkspaceEntity.WsWorkingStatus.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceManagerImplTests {

    @InjectMocks
    private WorkspaceManagerImpl workspaceManager;

    @Mock
    private WorkspaceRepository wsRepo;

    @Mock
    private ProjectRepository prjRepo;

    @Mock
    private RandomGenerator randomGenerator;

    @Mock
    private KeyManager keyManager;

    @Mock
    private GitManager gitManager;

    @Mock
    private WatchedPathStore watchedPathStore;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private PlatformTransactionManager transactionManager;

    private File spaceHome;

    @Before
    public void setup() throws Exception {
        spaceHome = Files.createTempDirectory("coding-ide-test").toFile();
        workspaceManager.setSpaceHome(spaceHome);
        workspaceManager.setApplicationEventPublisher(publisher);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        workspaceManager.transactionTemplate = transactionTemplate;
    }

    @Test(expected = WorkspaceMissingException.class)
    public void testSetupWorkspaceNotFound() {
        when(wsRepo.findBySpaceKey("notExist")).thenReturn(null);
        workspaceManager.setup("notExist");
    }

    @Test(expected = WorkspaceDeletedException.class)
    public void testSetupWorkspaceDeleted() {
        WorkspaceEntity wsEntity = new WorkspaceEntity();
        wsEntity.setSpaceKey("deleted");
        wsEntity.setWorkingStatus(Deleted);

        when(wsRepo.findBySpaceKey("deleted")).thenReturn(wsEntity);
        workspaceManager.setup("deleted");
    }

    @Test
    public void testSetupSuccess() throws Exception {
        WorkspaceEntity wsEntity = new WorkspaceEntity();
        wsEntity.setSpaceKey("test");
        wsEntity.setWorkingStatus(Online);

        ProjectEntity project = new ProjectEntity();
        project.setUrl("git@example.com:test/test.git");
        wsEntity.setProject(project);

        when(wsRepo.findBySpaceKey("test")).thenReturn(wsEntity);
        when(keyManager.isKeyExist(any(Workspace.class))).thenReturn(true);

        Workspace ws = workspaceManager.setup("test");

        assertNotNull(ws);
        assertEquals("test", ws.getSpaceKey());
        assertEquals(Online, ws.getWorkspaceEntity().getWorkingStatus());
    }

    @Test
    public void testCreateFromUrl() {
        String gitUrl = "git@example.com:test/test.git";

        ProjectEntity project = new ProjectEntity();
        project.setUrl(gitUrl);
        project.setName("test");

        WorkspaceEntity wsEntity = new WorkspaceEntity();
        wsEntity.setSpaceKey("test-ws");
        wsEntity.setProject(project);
        wsEntity.setWorkingStatus(Offline);

        when(prjRepo.findByUrl(gitUrl)).thenReturn(null);
        when(prjRepo.save(any(ProjectEntity.class))).thenReturn(project);
        when(wsRepo.findNotDeletedByProject(project)).thenReturn(null);
        when(randomGenerator.generate(any())).thenReturn("test-ws");
        when(wsRepo.save(any(WorkspaceEntity.class))).thenReturn(wsEntity);
        when(transactionManager.getTransaction(any())).thenReturn(null);

        Workspace ws = workspaceManager.createFromUrl(gitUrl);

        assertNotNull(ws);
        assertEquals("test-ws", ws.getSpaceKey());
        assertEquals(gitUrl, ws.getUrl());
    }

    @Test
    public void testDelete() {
        String spaceKey = "test";
        when(wsRepo.isDeleted(spaceKey)).thenReturn(false);

        workspaceManager.delete(spaceKey);

        verify(publisher).publishEvent(any());
    }

    @Test(expected = WorkspaceDeletedException.class)
    public void testDeleteAlreadyDeleted() {
        String spaceKey = "test";
        when(wsRepo.isDeleted(spaceKey)).thenReturn(true);

        workspaceManager.delete(spaceKey);
    }

    @Test
    public void testSetEncoding() {
        String spaceKey = "test";
        WorkspaceEntity wsEntity = new WorkspaceEntity();
        wsEntity.setSpaceKey(spaceKey);
        wsEntity.setProject(new ProjectEntity());

        when(wsRepo.findBySpaceKey(spaceKey)).thenReturn(wsEntity);

        Workspace ws = new Workspace(wsEntity, spaceHome);
        workspaceManager.setEncoding(ws, "UTF-8");

        verify(wsRepo).save(wsEntity);
        assertEquals("UTF-8", wsEntity.getEncoding());
    }

    @Test
    public void testIsOnline() {
        when(wsRepo.isOnline("test")).thenReturn(true);
        assertTrue(workspaceManager.isOnline("test"));
    }

    @Test
    public void testIsDeleted() {
        when(wsRepo.isDeleted("test")).thenReturn(true);
        assertTrue(workspaceManager.isDeleted("test"));
    }
}
