package net.coding.ide.service;

import net.coding.ide.dto.FileDTO;
import net.coding.ide.entity.ProjectEntity;
import net.coding.ide.entity.WorkspaceEntity;
import net.coding.ide.model.FileInfo;
import net.coding.ide.model.GitStatus;
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
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
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
    private ApplicationEventPublisher publisher;

    @Mock
    private WatchedPathStore watchedPathStore;

    @Mock
    private TransactionTemplate transactionTemplate;

    private File spaceHome;

    @Before
    public void setUp() throws Exception {
        spaceHome = Files.createTempDirectory("coding-ide-test").toFile();
        workspaceManager.setSpaceHome(spaceHome);
        workspaceManager.setApplicationEventPublisher(publisher);
    }

    private WorkspaceEntity mockWorkspaceEntity(String spaceKey) {
        WorkspaceEntity wsEntity = new WorkspaceEntity();
        wsEntity.setSpaceKey(spaceKey);
        wsEntity.setEncoding("UTF-8");
        wsEntity.setWorkingStatus(WorkspaceEntity.WsWorkingStatus.Online);

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("test-project");
        wsEntity.setProject(projectEntity);

        return wsEntity;
    }

    private Workspace mockWorkspace(String spaceKey) {
        WorkspaceEntity wsEntity = mockWorkspaceEntity(spaceKey);
        return new Workspace(wsEntity, spaceHome);
    }

    @Test
    public void testSetupSuccess() throws Exception {
        String spaceKey = "test-workspace";
        WorkspaceEntity wsEntity = mockWorkspaceEntity(spaceKey);

        when(wsRepo.findBySpaceKey(spaceKey)).thenReturn(wsEntity);
        when(keyManager.isKeyExist(any(Workspace.class))).thenReturn(true);

        Workspace workspace = workspaceManager.setup(spaceKey);

        assertNotNull(workspace);
        assertEquals(spaceKey, workspace.getSpaceKey());
        verify(wsRepo).findBySpaceKey(spaceKey);
    }

    @Test(expected = WorkspaceMissingException.class)
    public void testSetupWorkspaceNotFound() {
        String spaceKey = "non-existent";
        when(wsRepo.findBySpaceKey(spaceKey)).thenReturn(null);

        workspaceManager.setup(spaceKey);
    }

    @Test(expected = WorkspaceDeletedException.class)
    public void testSetupWorkspaceDeleted() {
        String spaceKey = "deleted-workspace";
        WorkspaceEntity wsEntity = mockWorkspaceEntity(spaceKey);
        wsEntity.setWorkingStatus(WorkspaceEntity.WsWorkingStatus.Deleted);

        when(wsRepo.findBySpaceKey(spaceKey)).thenReturn(wsEntity);

        workspaceManager.setup(spaceKey);
    }

    @Test
    public void testCreateFromUrl() throws Exception {
        String gitUrl = "git@github.com:test/repo.git";
        String spaceKey = "test-space";

        ProjectEntity project = new ProjectEntity();
        project.setUrl(gitUrl);

        WorkspaceEntity wsEntity = mockWorkspaceEntity(spaceKey);
        wsEntity.setProject(project);

        when(prjRepo.findByUrl(gitUrl)).thenReturn(project);
        when(wsRepo.findNotDeletedByProject(project)).thenReturn(null);
        when(wsRepo.save(any(WorkspaceEntity.class))).thenReturn(wsEntity);
        when(randomGenerator.generate(any())).thenReturn(spaceKey);
        when(keyManager.isKeyExist(any(Workspace.class))).thenReturn(true);
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgumentAt(0, TransactionCallback.class);
            return callback.doInTransaction(null);
        });

        Workspace workspace = workspaceManager.createFromUrl(gitUrl);

        assertNotNull(workspace);
        assertEquals(spaceKey, workspace.getSpaceKey());
        assertEquals(gitUrl, workspace.getProject().getUrl());
    }

    @Test
    public void testDelete() throws Exception {
        String spaceKey = "test-workspace";
        when(wsRepo.isDeleted(spaceKey)).thenReturn(false);

        workspaceManager.delete(spaceKey);

        verify(publisher).publishEvent(any());
        verify(wsRepo).isDeleted(spaceKey);
    }

    @Test(expected = WorkspaceDeletedException.class)
    public void testDeleteAlreadyDeleted() {
        String spaceKey = "deleted-workspace";
        when(wsRepo.isDeleted(spaceKey)).thenReturn(true);

        workspaceManager.delete(spaceKey);
    }

    @Test
    public void testGetFileInfo() throws Exception {
        String spaceKey = "test-workspace";
        String path = "/test.txt";

        Workspace workspace = mockWorkspace(spaceKey);

        Path filePath = workspace.getPath(path);
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);

        when(gitManager.status(any(), any())).thenReturn(GitStatus.UNTRACKED);

        FileInfo fileInfo = workspaceManager.getFileInfo(workspace, path);

        assertNotNull(fileInfo);
        assertEquals("/test.txt", fileInfo.getPath());
        assertEquals("test.txt", fileInfo.getName());
        assertFalse(fileInfo.isDir());
        assertEquals(GitStatus.UNTRACKED, fileInfo.getGitStatus());
    }

    @Test
    public void testReadFile() throws Exception {
        String spaceKey = "test-workspace";
        String path = "/test.txt";
        String content = "test content";
        String encoding = "UTF-8";

        Workspace workspace = mockWorkspace(spaceKey);

        Path filePath = workspace.getPath(path);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, content.getBytes());

        FileDTO fileDTO = workspaceManager.readFile(workspace, path, encoding, false);

        assertNotNull(fileDTO);
        assertEquals(path, fileDTO.getPath());
        assertEquals(content, fileDTO.getContent());
        assertEquals(encoding, fileDTO.getEncoding());
        assertFalse(fileDTO.getBase64());
    }

    @Test
    public void testListFiles() throws Exception {
        String spaceKey = "test-workspace";
        String path = "/";
        Workspace workspace = mockWorkspace(spaceKey);

        Files.createDirectories(workspace.getPath("/dir1"));
        Files.createFile(workspace.getPath("/file1.txt"));
        Files.createFile(workspace.getPath("/file2.txt"));

        when(gitManager.status(any(), any())).thenReturn(GitStatus.UNTRACKED);

        List<FileInfo> files = workspaceManager.listFiles(workspace, path, true, true);

        assertNotNull(files);
        assertEquals(3, files.size());
    }

    @Test
    public void testSetEncoding() {
        String spaceKey = "test-workspace";
        String encoding = "UTF-8";
        WorkspaceEntity wsEntity = mockWorkspaceEntity(spaceKey);

        Workspace workspace = new Workspace(wsEntity, spaceHome);

        when(wsRepo.findBySpaceKey(spaceKey)).thenReturn(wsEntity);

        workspaceManager.setEncoding(workspace, encoding);

        verify(wsRepo).save(wsEntity);
        assertEquals(encoding, wsEntity.getEncoding());
    }
}
