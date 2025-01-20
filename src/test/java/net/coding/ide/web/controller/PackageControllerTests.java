package net.coding.ide.web.controller;

import net.coding.ide.dto.Package;
import net.coding.ide.service.PackageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class PackageControllerTests {

    @Mock
    private PackageService packageService;

    @InjectMocks
    private PackageController packageController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(packageController)
                .setControllerAdvice(new ExceptionHandlerAdvice())
                .build();
    }

    @Test
    public void testReadPackageFile() throws Exception {
        String name = "test-package";
        String version = "1.0.0";
        String fileName = "readme.txt";
        String fileContent = "Test content";

        when(packageService.readPackageFile(name, version, fileName)).thenReturn(fileContent);

        mockMvc.perform(get("/packages/{name}/{version}/{fileName}", name, version, fileName)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().string(fileContent))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/plain;charset=ISO-8859-1"));
    }

    @Test
    public void testGetAllPackages() throws Exception {
        Package pkg1 = new Package();
        pkg1.setName("package1");
        pkg1.setVersion("1.0.0");

        Package pkg2 = new Package();
        pkg2.setName("package2");
        pkg2.setVersion("2.0.0");

        List<Package> packages = Arrays.asList(pkg1, pkg2);

        when(packageService.findAll()).thenReturn(packages);

        mockMvc.perform(get("/packages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void testReadPackageFileThrowsIOException() throws Exception {
        String name = "test-package";
        String version = "1.0.0";
        String fileName = "missing.txt";

        when(packageService.readPackageFile(name, version, fileName))
                .thenThrow(new IOException("File not found"));

        mockMvc.perform(get("/packages/{name}/{version}/{fileName}", name, version, fileName))
                .andExpect(status().isInternalServerError());
    }

    @ControllerAdvice
    private static class ExceptionHandlerAdvice {
        @ExceptionHandler(IOException.class)
        public ResponseEntity<String> handleIOException(IOException ex) {
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }
}
