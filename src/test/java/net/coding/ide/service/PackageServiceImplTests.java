package net.coding.ide.service;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.coding.ide.dto.Package;
import net.coding.ide.model.exception.NotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

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
    public void testToPackageWithValidJson() throws IOException {
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        File manifestFile = new File(packageDir, "manifest.json");
        String manifestContent = "{\"meta\": {\"name\": \"test-package\", \"version\": \"1.0.0\", \"description\": \"Test package\", \"author\": \"test\", \"displayName\": \"Test Package\"}}";

        try (FileWriter writer = new FileWriter(manifestFile)) {
            writer.write(manifestContent);
        }

        List<Package> packages = packageService.findAll();

        assertFalse(packages.isEmpty());
        Package pkg = packages.get(0);
        assertEquals("test-package", pkg.getName());
        assertEquals("1.0.0", pkg.getVersion());
        assertEquals(Package.Requirement.Required, pkg.getRequirement());
    }

    @Test
    public void testToPackageWithInvalidJson() throws IOException {
        File packageDir = new File(packagesDir, "invalid-package/1.0.0");
        packageDir.mkdirs();

        File manifestFile = new File(packageDir, "manifest.json");
        String invalidContent = "invalid json content";

        try (FileWriter writer = new FileWriter(manifestFile)) {
            writer.write(invalidContent);
        }

        List<Package> packages = packageService.findAll();
        assertTrue(packages.isEmpty());
    }

    @Test
    public void testFindAllWithNoPackages() {
        List<Package> packages = packageService.findAll();
        assertTrue(packages.isEmpty());
    }

    @Test
    public void testFindAllWithCaching() throws IOException {
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        File manifestFile = new File(packageDir, "manifest.json");
        String manifestContent = "{\"meta\": {\"name\": \"test-package\", \"version\": \"1.0.0\"}}";

        try (FileWriter writer = new FileWriter(manifestFile)) {
            writer.write(manifestContent);
        }

        List<Package> firstCall = packageService.findAll();
        assertEquals(1, firstCall.size());

        List<Package> secondCall = packageService.findAll();
        assertSame(firstCall, secondCall);
    }

    @Test
    public void testReadPackageFileSuccess() throws IOException {
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        File testFile = new File(packageDir, "test.txt");
        String content = "test content";

        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(content);
        }

        String result = packageService.readPackageFile("test-package", "1.0.0", "test.txt");
        assertEquals(content, result);
    }

    @Test(expected = NotFoundException.class)
    public void testReadPackageFileNotFound() throws IOException {
        packageService.readPackageFile("non-existent", "1.0.0", "test.txt");
    }
}
