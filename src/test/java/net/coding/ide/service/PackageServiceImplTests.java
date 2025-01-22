package net.coding.ide.service;

import com.google.common.collect.Lists;
import net.coding.ide.dto.Package;
import net.coding.ide.model.exception.NotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PackageServiceImplTests {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private PackageServiceImpl packageService;

    private File packagesDir;

    @Before
    public void setUp() throws IOException {
        packagesDir = temporaryFolder.newFolder("packages");
        packageService = new PackageServiceImpl(packagesDir);
    }

    @Test
    public void testToPackage_ValidJson() throws IOException {
        // Create test package folder structure
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        // Create manifest.json with valid content
        String manifestJson = "{\n" +
                "  \"meta\": {\n" +
                "    \"name\": \"test-package\",\n" +
                "    \"version\": \"1.0.0\",\n" +
                "    \"description\": \"Test Package\",\n" +
                "    \"author\": \"Test Author\",\n" +
                "    \"displayName\": \"Test Display Name\"\n" +
                "  }\n" +
                "}";

        File manifestFile = new File(packageDir, "manifest.json");
        Files.write(manifestFile.toPath(), manifestJson.getBytes());

        List<Package> packages = packageService.findAll();

        assertFalse(packages.isEmpty());
        Package pkg = packages.get(0);
        assertEquals("test-package", pkg.getName());
        assertEquals("1.0.0", pkg.getVersion());
        assertEquals("Test Package", pkg.getDescription());
        assertEquals("Test Author", pkg.getAuthor());
        assertEquals("Test Display Name", pkg.getDisplayName());
        assertEquals(Package.Requirement.Required, pkg.getRequirement());
    }

    @Test
    public void testToPackage_InvalidJson() throws IOException {
        // Create test package folder structure
        File packageDir = new File(packagesDir, "invalid-package/1.0.0");
        packageDir.mkdirs();

        // Create manifest.json with invalid content
        String manifestJson = "invalid json content";
        File manifestFile = new File(packageDir, "manifest.json");
        Files.write(manifestFile.toPath(), manifestJson.getBytes());

        List<Package> packages = packageService.findAll();
        assertTrue(packages.isEmpty());
    }

    @Test
    public void testFindAll_EmptyDirectory() {
        List<Package> packages = packageService.findAll();
        assertTrue(packages.isEmpty());
    }

    @Test
    public void testFindAll_CacheLastModified() throws IOException {
        // Create initial package
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        String manifestJson = "{\n" +
                "  \"meta\": {\n" +
                "    \"name\": \"test-package\",\n" +
                "    \"version\": \"1.0.0\"\n" +
                "  }\n" +
                "}";

        File manifestFile = new File(packageDir, "manifest.json");
        Files.write(manifestFile.toPath(), manifestJson.getBytes());

        // First call to findAll()
        List<Package> firstResult = packageService.findAll();
        assertFalse(firstResult.isEmpty());

        // Second call without modification should return cached result
        List<Package> secondResult = packageService.findAll();
        assertEquals(firstResult, secondResult);
    }

    @Test
    public void testReadPackageFile_ExistingFile() throws IOException {
        // Create test file
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        String fileContent = "test content";
        Path filePath = new File(packageDir, "test.txt").toPath();
        Files.write(filePath, fileContent.getBytes());

        String result = packageService.readPackageFile("test-package", "1.0.0", "test.txt");
        assertEquals(fileContent, result);
    }

    @Test(expected = NotFoundException.class)
    public void testReadPackageFile_NonExistentFile() throws IOException {
        packageService.readPackageFile("non-existent", "1.0.0", "test.txt");
    }
}
