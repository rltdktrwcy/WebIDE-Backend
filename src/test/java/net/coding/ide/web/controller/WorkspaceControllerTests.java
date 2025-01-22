package net.coding.ide.web.controller;

import com.google.common.collect.Lists;
import net.coding.ide.dto.*;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
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

    @Before
    public void setup() {
        session = new MockHttpSession();
    }

    @Test
    public void testList() {
        List<WorkspaceEntity> entities = new ArrayList<>();
        WorkspaceEntity entity = new WorkspaceEntity();
        entities.add(entity);

        WorkspaceDTO dto = new WorkspaceDTO();
        when(wsMgr.list()).thenReturn(entities);
        when(mapper.map(entity, WorkspaceDTO.class)).thenReturn(dto);

        List<WorkspaceDTO> result = controller.list();

        verify(wsMgr).list();
        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    public void testSetup() throws IOException {
        String spaceKey = "test-space";
        WorkspaceDTO dto = new WorkspaceDTO();

        when(wsMgr.setup(spaceKey)).thenReturn(workspace);
        when(mapper.map(workspace, WorkspaceDTO.class)).thenReturn(dto);

        WorkspaceDTO result = controller.setup(spaceKey, session);

        verify(wsMgr).setup(spaceKey);
        assertEquals(dto, result);
    }

    @Test
    public void testClone() {
        String url = "git://test.repo";
        WorkspaceDTO dto = new WorkspaceDTO();

        when(wsMgr.createFromUrl(url)).thenReturn(workspace);
        when(mapper.map(workspace, WorkspaceDTO.class)).thenReturn(dto);
        when(workspace.getSpaceKey()).thenReturn("test-space");

        WorkspaceDTO result = controller.clone(url, session);

        verify(wsMgr).createFromUrl(url);
        assertEquals(dto, result);
    }

    @Test
    public void testQueryWorkspace() {
        String spaceKey = "test-space";
        WorkspaceEntity entity = new WorkspaceEntity();
        WorkspaceDTO dto = new WorkspaceDTO();

        when(wsMgr.getWorkspaceEntity(spaceKey)).thenReturn(entity);
        when(mapper.map(entity, WorkspaceDTO.class)).thenReturn(dto);

        WorkspaceDTO result = controller.queryWorkspace(spaceKey);

        verify(wsMgr).getWorkspaceEntity(spaceKey);
        assertEquals(dto, result);
    }

    @Test(expected = WorkspaceMissingException.class)
    public void testQueryWorkspaceNotFound() {
        String spaceKey = "non-existent";
        when(wsMgr.getWorkspaceEntity(spaceKey)).thenReturn(null);

        controller.queryWorkspace(spaceKey);
    }

    @Test
    public void testGetSettings() throws IOException {
        String path = ".coding-ide/settings.json";
        when(workspace.exists(path)).thenReturn(true);
        when(workspace.read(path, false)).thenReturn("{}");

        FileDTO result = controller.getSettings(workspace, false);

        assertEquals(path, result.getPath());
        assertEquals("{}", result.getContent());
        assertFalse(result.getBase64());
    }

    @Test
    public void testSetSettings() throws IOException {
        String content = "{}";
        String path = ".coding-ide/settings.json";

        when(workspace.read(path, false)).thenReturn(content);

        FileDTO result = controller.setSettings(workspace, content, false, true, true, false);

        verify(workspace).write(path, content, false, true, true);
        assertEquals(path, result.getPath());
        assertEquals(content, result.getContent());
    }

    @Test
    public void testSearch() throws IOException {
        String keyword = "test";
        List<FileSearchResultEntry> entries = new ArrayList<>();
        FileSearchResultEntry entry = new FileSearchResultEntry("test.txt", "text/plain");
        entries.add(entry);

        FileSearchResultEntryDTO dto = new FileSearchResultEntryDTO();
        when(wsMgr.search(workspace, keyword, false)).thenReturn(entries);
        when(mapper.map(entry, FileSearchResultEntryDTO.class)).thenReturn(dto);

        List<FileSearchResultEntryDTO> result = controller.search(workspace, keyword, false);

        verify(wsMgr).search(workspace, keyword, false);
        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    public void testSetEncoding() {
        String charset = "UTF-8";
        WorkspaceDTO dto = new WorkspaceDTO();

        when(mapper.map(workspace, WorkspaceDTO.class)).thenReturn(dto);

        WorkspaceDTO result = controller.setEncoding(workspace, charset);

        verify(wsMgr).setEncoding(workspace, charset);
        verify(workspace).setEncoding(charset);
        assertEquals(dto, result);
    }
}
