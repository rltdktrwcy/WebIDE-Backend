package net.coding.ide.service;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import net.coding.ide.dto.Package;
import net.coding.ide.model.exception.NotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PackageServiceImplTests {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private PackageServiceImpl packageService;

    private File packagesDir;

    private Gson gson = new Gson();

    @Before
    public void setup() throws Exception {
        packagesDir = tempFolder.newFolder("packages");
        packageService = new PackageServiceImpl(packagesDir);
    }

    @Test
    public void testToPackageWithValidJson() throws Exception {
        // Create test package folder structure
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        // Create manifest.json with valid content
        Package pkg = new Package();
        pkg.setName("test-package");
        pkg.setVersion("1.0.0");
        pkg.setDescription("Test Package");

        String json = "{\"meta\":" + gson.toJson(pkg) + "}";
        File manifestFile = new File(packageDir, "manifest.json");
        try (FileWriter writer = new FileWriter(manifestFile)) {
            writer.write(json);
        }

        List<Package> packages = packageService.findAll();

        assertEquals(1, packages.size());
        Package result = packages.get(0);
        assertEquals("test-package", result.getName());
        assertEquals("1.0.0", result.getVersion());
        assertEquals("Test Package", result.getDescription());
        assertEquals(Package.Requirement.Required, result.getRequirement());
    }

    @Test
    public void testToPackageWithInvalidJson() throws Exception {
        File packageDir = new File(packagesDir, "invalid-package/1.0.0");
        packageDir.mkdirs();

        File manifestFile = new File(packageDir, "manifest.json");
        try (FileWriter writer = new FileWriter(manifestFile)) {
            writer.write("invalid json");
        }

        List<Package> packages = packageService.findAll();
        assertEquals(0, packages.size());
    }

    @Test
    public void testFindAllWithNoPackages() {
        List<Package> packages = packageService.findAll();
        assertTrue(packages.isEmpty());
    }

    @Test
    public void testFindAllWithCaching() throws Exception {
        // Create initial package
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        Package pkg = new Package();
        pkg.setName("test-package");
        pkg.setVersion("1.0.0");

        String json = "{\"meta\":" + gson.toJson(pkg) + "}";
        File manifestFile = new File(packageDir, "manifest.json");
        try (FileWriter writer = new FileWriter(manifestFile)) {
            writer.write(json);
        }

        // First call
        List<Package> firstResult = packageService.findAll();
        assertEquals(1, firstResult.size());

        // Second call should use cache
        List<Package> secondResult = packageService.findAll();
        assertSame(firstResult, secondResult);
    }

    @Test
    public void testReadPackageFileSuccess() throws Exception {
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        String content = "test content";
        File testFile = new File(packageDir, "test.txt");
        Files.write(testFile.toPath(), content.getBytes());

        String result = packageService.readPackageFile("test-package", "1.0.0", "test.txt");
        assertEquals(content, result);
    }

    @Test(expected = NotFoundException.class)
    public void testReadPackageFileNotFound() throws Exception {
        packageService.readPackageFile("non-existent", "1.0.0", "test.txt");
    }
}
