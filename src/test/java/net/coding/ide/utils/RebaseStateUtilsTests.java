package net.coding.ide.utils;

import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class RebaseStateUtilsTests {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Mock
    private Repository repository;

    private File gitDir;
    private File rebaseApplyDir;
    private File rebaseMergeDir;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);

        gitDir = tempFolder.newFolder(".git");
        rebaseApplyDir = new File(gitDir, "rebase-apply");
        rebaseMergeDir = new File(gitDir, "rebase-merge");

        when(repository.getDirectory()).thenReturn(gitDir);
    }

    @Test
    public void shouldReturnRebaseApplyDirWhenExists() throws IOException {
        rebaseApplyDir.mkdir();

        File result = RebaseStateUtils.getRebaseStateDir(repository);

        assertEquals(rebaseApplyDir, result);
    }

    @Test
    public void shouldReturnRebaseMergeDirWhenRebaseApplyNotExists() throws IOException {
        rebaseMergeDir.mkdir();

        File result = RebaseStateUtils.getRebaseStateDir(repository);

        assertEquals(rebaseMergeDir, result);
    }

    @Test
    public void shouldGetRebaseFile() throws IOException {
        rebaseApplyDir.mkdir();
        String fileName = "test-file";

        File result = RebaseStateUtils.getRebaseFile(repository, fileName);

        assertEquals(new File(rebaseApplyDir, fileName), result);
    }

    @Test
    public void shouldReadFileContent() throws IOException {
        File testDir = tempFolder.newFolder();
        String fileName = "test-file";
        String content = "test content";

        File testFile = new File(testDir, fileName);
        Files.write(testFile.toPath(), (content + "\n").getBytes());

        String result = RebaseStateUtils.readFile(testDir, fileName);

        assertEquals(content, result);
    }

    @Test
    public void shouldGetRebasePath() throws IOException {
        rebaseApplyDir.mkdir();
        String fileName = "test-file";

        String result = RebaseStateUtils.getRebasePath(repository, fileName);

        assertEquals("rebase-apply/test-file", result);
    }

    @Test
    public void shouldCreateFileInRepository() throws IOException {
        rebaseApplyDir.mkdir();
        String fileName = "test-file";
        String content = "test content";

        RebaseStateUtils.createFile(repository, fileName, content);

        File createdFile = new File(rebaseApplyDir, fileName);
        assertTrue(createdFile.exists());
        String fileContent = new String(Files.readAllBytes(createdFile.toPath())).trim();
        assertEquals(content, fileContent);
    }

    @Test
    public void shouldCreateFileInDirectory() throws IOException {
        File testDir = tempFolder.newFolder();
        String fileName = "test-file";
        String content = "test content";

        RebaseStateUtils.createFile(testDir, fileName, content);

        File createdFile = new File(testDir, fileName);
        assertTrue(createdFile.exists());
        String fileContent = new String(Files.readAllBytes(createdFile.toPath())).trim();
        assertEquals(content, fileContent);
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionWhenReadingNonExistentFile() throws IOException {
        File testDir = tempFolder.newFolder();
        RebaseStateUtils.readFile(testDir, "non-existent-file");
    }
}
