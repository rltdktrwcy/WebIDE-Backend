package net.coding.ide.web.controller;

import net.coding.ide.dto.BranchDTO;
import net.coding.ide.dto.DiffDTO;
import net.coding.ide.dto.FileDTO;
import net.coding.ide.model.*;
import net.coding.ide.model.exception.GitOperationException;
import net.coding.ide.service.GitManager;
import net.coding.ide.service.WorkspaceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitControllerTests {

    @InjectMocks
    private GitController gitController;

    @Mock
    private GitManager gitManager;

    @Mock
    private WorkspaceManager workspaceManager;

    @Mock
    private Workspace workspace;

    @Before
    public void setup() {
        when(workspace.getSpaceKey()).thenReturn("test-workspace");
    }

    @Test
    public void testStatus() throws Exception {
        CommitStatus expectedStatus = new CommitStatus(true);
        when(gitManager.getStatus(workspace)).thenReturn(expectedStatus);

        CommitStatus actualStatus = gitController.status(workspace);

        assertEquals(expectedStatus, actualStatus);
        verify(gitManager).getStatus(workspace);
    }

    @Test
    public void testCommit() throws Exception {
        List<String> files = Arrays.asList("file1.txt", "file2.txt");
        String message = "test commit";
        FileInfo fileInfo = new FileInfo();

        when(gitManager.commit(workspace, files, message)).thenReturn(files);
        when(workspaceManager.getFileInfo(any(), anyString())).thenReturn(fileInfo);

        List<FileInfo> result = gitController.commit(workspace, "file1.txt,file2.txt", message);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gitManager).commit(workspace, files, message);
    }

    @Test
    public void testCommitAll() throws Exception {
        List<String> files = Arrays.asList("file1.txt", "file2.txt");
        String message = "test commit all";
        FileInfo fileInfo = new FileInfo();

        when(gitManager.commitAll(workspace, message)).thenReturn(files);
        when(workspaceManager.getFileInfo(any(), anyString())).thenReturn(fileInfo);

        List<FileInfo> result = gitController.commitAll(workspace, message);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gitManager).commitAll(workspace, message);
    }

    @Test
    public void testDiff() throws Exception {
        String path = "test.txt";
        String oldRef = "HEAD~1";
        String newRef = "HEAD";
        String diffContent = "test diff content";

        when(gitManager.diff(workspace, path, oldRef, newRef)).thenReturn(diffContent);

        DiffDTO result = gitController.diff(workspace, path, oldRef, newRef);

        assertNotNull(result);
        assertEquals(diffContent, result.getDiff());
    }

    @Test
    public void testSync() throws Exception {
        gitController.sync(workspace);
        verify(gitManager).sync(workspace);
    }

    @Test
    public void testPull() throws Exception {
        when(gitManager.pull(workspace)).thenReturn(true);

        ResponseEntity response = gitController.pull(workspace);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testPush() throws Exception {
        PushResponse expectedResponse = new PushResponse();
        when(gitManager.push(workspace)).thenReturn(expectedResponse);

        PushResponse actualResponse = gitController.push(workspace, null);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testGetBranch() throws Exception {
        String branchName = "master";
        when(gitManager.getBranch(workspace)).thenReturn(branchName);

        BranchDTO result = gitController.getBranch(workspace);

        assertNotNull(result);
        assertEquals(branchName, result.getName());
    }

    @Test
    public void testCreateBranch() throws Exception {
        String branchName = "feature-branch";
        Branches branches = Branches.of("master", Arrays.asList("master", "feature-branch"), Arrays.asList("origin/master"));

        when(gitManager.getBranches(workspace)).thenReturn(branches);

        Branches result = gitController.createBranch(workspace, branchName);

        assertNotNull(result);
        assertEquals(branches, result);
        verify(gitManager).createBranch(workspace, branchName);
        verify(gitManager).checkout(workspace, branchName, null);
    }

    @Test
    public void testDeleteBranch() throws Exception {
        String branchName = "feature-branch";
        Branches branches = Branches.of("master", Arrays.asList("master"), Arrays.asList("origin/master"));

        when(gitManager.hasBranch(workspace, branchName)).thenReturn(true);
        when(gitManager.getBranches(workspace)).thenReturn(branches);

        Branches result = gitController.deleteBranch(workspace, branchName);

        assertNotNull(result);
        assertEquals(branches, result);
        verify(gitManager).deleteBranch(workspace, branchName);
    }

    @Test
    public void testCreateStash() throws Exception {
        String message = "stash message";

        ResponseEntity response = gitController.createStash(workspace, message, false);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(gitManager).createStash(workspace, false, message);
    }

    @Test
    public void testRead() throws Exception {
        String path = "test.txt";
        String ref = "HEAD";
        String encoding = "UTF-8";
        String content = "file content";

        when(workspace.getEncoding()).thenReturn("UTF-8");
        when(gitManager.readFileFromRef(workspace, ref, path, encoding, false)).thenReturn(content);

        FileDTO result = gitController.read(workspace, ref, path, encoding, false);

        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(path, result.getPath());
    }
}
