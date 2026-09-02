package com.universityprinting.printing_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.universityprinting.printing_backend.dto.CreatePrinterRequest;
import com.universityprinting.printing_backend.dto.PrinterResponse;
import com.universityprinting.printing_backend.dto.UpdatePrinterRequest;
import com.universityprinting.printing_backend.exception.GlobalExceptionHandler;
import com.universityprinting.printing_backend.exception.PrinterNotFoundException;
import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrinterStatus;
import com.universityprinting.printing_backend.service.PrinterService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PrinterControllerTest {

    @Mock
    private PrinterService printerService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PrinterController printerController = new PrinterController(printerService);
        mockMvc = MockMvcBuilders.standaloneSetup(printerController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void createPrinter_Success_Returns201Created() throws Exception {
        CreatePrinterRequest request = new CreatePrinterRequest("HP Laser", "Library", Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null);
        PrinterResponse response = new PrinterResponse("ptr-1", "HP Laser", "Library", PrinterStatus.OFFLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null, true, null, Instant.now(), Instant.now());

        when(printerService.createPrinter(any(CreatePrinterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/printers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("ptr-1"))
            .andExpect(jsonPath("$.name").value("HP Laser"));
    }

    @Test
    void getAllPrinters_Returns200Ok() throws Exception {
        PrinterResponse response = new PrinterResponse("ptr-1", "HP Laser", "Library", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null, true, null, Instant.now(), Instant.now());
        when(printerService.getAllPrinters()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/printers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("ptr-1"));
    }

    @Test
    void getPrinterById_Success_Returns200Ok() throws Exception {
        PrinterResponse response = new PrinterResponse("ptr-1", "HP Laser", "Library", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null, true, null, Instant.now(), Instant.now());
        when(printerService.getPrinterById("ptr-1")).thenReturn(response);

        mockMvc.perform(get("/api/printers/ptr-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("ptr-1"));
    }

    @Test
    void getPrinterById_NotFound_Returns404() throws Exception {
        when(printerService.getPrinterById("ptr-unknown"))
            .thenThrow(new PrinterNotFoundException("Printer not found with ID: ptr-unknown"));

        mockMvc.perform(get("/api/printers/ptr-unknown"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"));
    }

    @Test
    void updatePrinter_Success_Returns200Ok() throws Exception {
        UpdatePrinterRequest request = new UpdatePrinterRequest();
        request.setName("HP Laser Updated");
        PrinterResponse response = new PrinterResponse("ptr-1", "HP Laser Updated", "Library", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null, true, null, Instant.now(), Instant.now());

        when(printerService.updatePrinter(eq("ptr-1"), any(UpdatePrinterRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/printers/ptr-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("HP Laser Updated"));
    }

    @Test
    void updatePrinterStatus_Success_Returns200Ok() throws Exception {
        PrinterResponse response = new PrinterResponse("ptr-1", "HP Laser", "Library", PrinterStatus.BUSY, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null, true, null, Instant.now(), Instant.now());
        when(printerService.updatePrinterStatus("ptr-1", PrinterStatus.BUSY)).thenReturn(response);

        mockMvc.perform(patch("/api/printers/ptr-1/status")
                .param("status", "BUSY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("BUSY"));
    }

    @Test
    void enablePrinter_Success_Returns200Ok() throws Exception {
        PrinterResponse response = new PrinterResponse("ptr-1", "HP Laser", "Library", PrinterStatus.OFFLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null, true, null, Instant.now(), Instant.now());
        when(printerService.setPrinterEnabled("ptr-1", true)).thenReturn(response);

        mockMvc.perform(post("/api/printers/ptr-1/enable"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(true));
    }
}
