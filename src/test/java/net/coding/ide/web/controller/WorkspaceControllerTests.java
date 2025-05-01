package net.coding.ide.web.controller;

import net.coding.ide.dto.DirDTO;
import net.coding.ide.dto.FileDTO;
import net.coding.ide.dto.FileSearchResultEntryDTO;
import net.coding.ide.dto.WorkspaceDTO;
import net.coding.ide.entity.ProjectEntity;
import net.coding.ide.entity.WorkspaceEntity;
import net.coding.ide.model.FileInfo;
import net.coding.ide.model.FileSearchResultEntry;
import net.coding.ide.model.Workspace;
import net.coding.ide.model.exception.WorkspaceMissingException;
import net.coding.ide.service.GitManager;
import net.coding.ide.service.WorkspaceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceControllerTests {

    @InjectMocks
    private WorkspaceController controller;

    @Mock
    private WorkspaceManager wsMgr;

    @Mock
    private ModelMapper mapper;

    @Mock
    private GitManager gitMgr;

    @Mock
    private Workspace workspace;

    private MockHttpSession session;
    private WorkspaceEntity wsEntity;
    private WorkspaceDTO wsDTO;
    private String spaceKey = "test-workspace";

    @Before
    public void setup() {
        session = new MockHttpSession();
        wsEntity = new WorkspaceEntity();
        wsEntity.setSpaceKey(spaceKey);
        wsDTO = new WorkspaceDTO();
        wsDTO.setSpaceKey(spaceKey);

        when(workspace.getSpaceKey()).thenReturn(spaceKey);
    }

    @Test
    public void testList() {
        List<WorkspaceEntity> entities = new ArrayList<>();
        entities.add(wsEntity);

        when(wsMgr.list()).thenReturn(entities);
        when(mapper.map(any(WorkspaceEntity.class), eq(WorkspaceDTO.class))).thenReturn(wsDTO);

        List<WorkspaceDTO> result = controller.list();

        assertEquals(1, result.size());
        assertEquals(spaceKey, result.get(0).getSpaceKey());
        verify(wsMgr).list();
    }

    @Test
    public void testSetup() throws IOException {
        when(wsMgr.setup(spaceKey)).thenReturn(workspace);
        when(mapper.map(workspace, WorkspaceDTO.class)).thenReturn(wsDTO);

        WorkspaceDTO result = controller.setup(spaceKey, session);

        assertEquals(spaceKey, result.getSpaceKey());
        verify(wsMgr).setup(spaceKey);
    }

    @Test
    public void testClone() {
        String url = "git://test.repo";
        when(wsMgr.createFromUrl(url)).thenReturn(workspace);
        when(mapper.map(workspace, WorkspaceDTO.class)).thenReturn(wsDTO);

        WorkspaceDTO result = controller.clone(url, session);

        assertEquals(spaceKey, result.getSpaceKey());
        verify(wsMgr).createFromUrl(url);
    }

    @Test
    public void testQueryWorkspace() {
        when(wsMgr.getWorkspaceEntity(spaceKey)).thenReturn(wsEntity);
        when(mapper.map(wsEntity, WorkspaceDTO.class)).thenReturn(wsDTO);

        WorkspaceDTO result = controller.queryWorkspace(spaceKey);

        assertEquals(spaceKey, result.getSpaceKey());
        verify(wsMgr).getWorkspaceEntity(spaceKey);
    }

    @Test(expected = WorkspaceMissingException.class)
    public void testQueryWorkspaceNotFound() {
        when(wsMgr.getWorkspaceEntity(spaceKey)).thenReturn(null);
        controller.queryWorkspace(spaceKey);
    }

    @Test
    public void testDelete() throws Exception {
        ResponseEntity response = controller.delete(spaceKey);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(wsMgr).delete(spaceKey);
    }

    @Test
    public void testGetSettings() throws IOException {
        String content = "{}";
        when(workspace.exists(".coding-ide/settings.json")).thenReturn(false);
        when(workspace.read(".coding-ide/settings.json", false)).thenReturn(content);

        FileDTO result = controller.getSettings(workspace, false);

        assertEquals(".coding-ide/settings.json", result.getPath());
        assertEquals(content, result.getContent());
        assertFalse(result.getBase64());
    }

    @Test
    public void testSetSettings() throws IOException {
        String content = "{}";
        String ignoreContent = "";

        when(workspace.read(".coding-ide/settings.json", false)).thenReturn(content);
        when(workspace.exists(".gitignore")).thenReturn(false);
        when(workspace.read(".gitignore", false)).thenReturn(ignoreContent);
        when(gitMgr.checkIgnore(any(ByteArrayInputStream.class), eq(".coding-ide"), eq(true))).thenReturn(false);

        FileDTO result = controller.setSettings(workspace, content, false, true, true, true);

        assertEquals(".coding-ide/settings.json", result.getPath());
        assertEquals(content, result.getContent());
        verify(workspace).write(".coding-ide/settings.json", content, false, true, true);
        verify(workspace).write(eq(".gitignore"), anyString(), eq(false), eq(true), eq(true));
    }

    @Test
    public void testSearch() throws IOException {
        String keyword = "test";
        List<FileSearchResultEntry> entries = new ArrayList<>();
        FileSearchResultEntry entry = new FileSearchResultEntry("test.txt", "text/plain");
        entries.add(entry);

        when(wsMgr.search(workspace, keyword, false)).thenReturn(entries);
        when(mapper.map(entry, FileSearchResultEntryDTO.class)).thenReturn(new FileSearchResultEntryDTO());

        List<FileSearchResultEntryDTO> results = controller.search(workspace, keyword, false);

        assertEquals(1, results.size());
        verify(wsMgr).search(workspace, keyword, false);
    }

    @Test
    public void testSetEncoding() {
        String charset = "UTF-8";
        when(mapper.map(workspace, WorkspaceDTO.class)).thenReturn(wsDTO);

        WorkspaceDTO result = controller.setEncoding(workspace, charset);

        assertEquals(spaceKey, result.getSpaceKey());
        verify(wsMgr).setEncoding(workspace, charset);
        verify(workspace).setEncoding(charset);
    }
}
