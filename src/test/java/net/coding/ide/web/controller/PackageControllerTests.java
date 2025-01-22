package net.coding.ide.web.controller;

import net.coding.ide.dto.Package;
import net.coding.ide.service.PackageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class PackageControllerTests {

    @Mock
    private PackageService packageService;

    @InjectMocks
    private PackageController packageController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(packageController)
                .setHandlerExceptionResolvers(new ExceptionHandlerExceptionResolver())
                .build();
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

        mockMvc.perform(get("/packages")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("package1"))
                .andExpect(jsonPath("$[0].version").value("1.0.0"))
                .andExpect(jsonPath("$[1].name").value("package2"))
                .andExpect(jsonPath("$[1].version").value("2.0.0"));

        verify(packageService).findAll();
    }

    @Test
    public void testGetAllPackagesEmpty() throws Exception {
        when(packageService.findAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/packages")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(packageService).findAll();
    }
}
