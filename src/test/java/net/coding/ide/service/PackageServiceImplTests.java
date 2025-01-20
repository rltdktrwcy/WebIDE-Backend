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
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class PackageServiceImplTests {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private PackageServiceImpl packageService;
    private File packagesDir;
    private Gson gson = new Gson();

    @Before
    public void setUp() throws Exception {
        packagesDir = tempFolder.newFolder("packages");
        packageService = new PackageServiceImpl(packagesDir);
    }

    @Test
    public void testToPackage_ValidJson() throws Exception {
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        Package pkg = new Package();
        pkg.setName("test-package");
        pkg.setVersion("1.0.0");
        pkg.setDescription("Test package");

        JsonObject json = new JsonObject();
        json.add("meta", gson.toJsonTree(pkg));

        File manifestFile = new File(packageDir, "manifest.json");
        try (FileWriter writer = new FileWriter(manifestFile)) {
            gson.toJson(json, writer);
        }

        List<Package> packages = packageService.findAll();
        assertFalse(packages.isEmpty());
        assertEquals("test-package", packages.get(0).getName());
        assertEquals(Package.Requirement.Required, packages.get(0).getRequirement());
    }

    @Test
    public void testToPackage_InvalidJson() throws Exception {
        File packageDir = new File(packagesDir, "invalid-package/1.0.0");
        packageDir.mkdirs();

        File manifestFile = new File(packageDir, "manifest.json");
        try (FileWriter writer = new FileWriter(manifestFile)) {
            writer.write("invalid json");
        }

        List<Package> packages = packageService.findAll();
        assertTrue(packages.isEmpty());
    }

    @Test
    public void testFindAll_EmptyDirectory() {
        List<Package> packages = packageService.findAll();
        assertTrue(packages.isEmpty());
    }

    @Test
    public void testFindAll_CacheLastModified() throws Exception {
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        Package pkg = new Package();
        pkg.setName("test-package");
        JsonObject json = new JsonObject();
        json.add("meta", gson.toJsonTree(pkg));

        File manifestFile = new File(packageDir, "manifest.json");
        try (FileWriter writer = new FileWriter(manifestFile)) {
            gson.toJson(json, writer);
        }

        List<Package> packages1 = packageService.findAll();
        assertFalse(packages1.isEmpty());

        List<Package> packages2 = packageService.findAll();
        assertSame(packages1, packages2);
    }

    @Test
    public void testReadPackageFile_ExistingFile() throws Exception {
        File packageDir = new File(packagesDir, "test-package/1.0.0");
        packageDir.mkdirs();

        String content = "test content";
        File testFile = new File(packageDir, "test.txt");
        Files.write(testFile.toPath(), content.getBytes());

        String result = packageService.readPackageFile("test-package", "1.0.0", "test.txt");
        assertEquals(content, result);
    }

    @Test(expected = NotFoundException.class)
    public void testReadPackageFile_NonExistentFile() throws Exception {
        packageService.readPackageFile("non-existent", "1.0.0", "test.txt");
    }
}
