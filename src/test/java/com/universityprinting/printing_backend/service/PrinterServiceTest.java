package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.universityprinting.printing_backend.dto.CreatePrinterRequest;
import com.universityprinting.printing_backend.dto.PrinterResponse;
import com.universityprinting.printing_backend.dto.UpdatePrinterRequest;
import com.universityprinting.printing_backend.exception.IncompatiblePrinterException;
import com.universityprinting.printing_backend.exception.PrinterNotFoundException;
import com.universityprinting.printing_backend.exception.PrinterUnavailableException;
import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.model.Printer;
import com.universityprinting.printing_backend.model.PrinterStatus;
import com.universityprinting.printing_backend.repository.PrinterRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrinterServiceTest {

    @Mock
    private PrinterRepository printerRepository;

    private PrinterService printerService;

    @BeforeEach
    void setUp() {
        printerService = new PrinterService(printerRepository);
    }

    @Test
    void createPrinter_Success() {
        CreatePrinterRequest request = new CreatePrinterRequest(
            "HP LaserJet Pro",
            "Library 1st Floor",
            Set.of(ColorMode.BLACK_WHITE),
            Set.of(PaperSize.A4),
            false,
            null
        );

        when(printerRepository.save(any(Printer.class))).thenAnswer(invocation -> {
            Printer p = invocation.getArgument(0);
            p.setId("ptr-101");
            return p;
        });

        PrinterResponse response = printerService.createPrinter(request);

        assertNotNull(response);
        assertEquals("ptr-101", response.getId());
        assertEquals("HP LaserJet Pro", response.getName());
        assertEquals(PrinterStatus.OFFLINE, response.getStatus());
        assertTrue(response.getEnabled());
        verify(printerRepository).save(any(Printer.class));
    }

    @Test
    void updatePrinter_Success() {
        Printer printer = new Printer("ptr-101", "HP", "Lib", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null, true, null, Instant.now(), Instant.now());
        when(printerRepository.findById("ptr-101")).thenReturn(Optional.of(printer));
        when(printerRepository.save(any(Printer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePrinterRequest updateRequest = new UpdatePrinterRequest();
        updateRequest.setName("HP Updated");
        updateRequest.setDuplexSupported(true);

        PrinterResponse response = printerService.updatePrinter("ptr-101", updateRequest);

        assertEquals("HP Updated", response.getName());
        assertTrue(response.getDuplexSupported());
    }

    @Test
    void getPrinterById_NotFound_ThrowsPrinterNotFoundException() {
        when(printerRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(
            PrinterNotFoundException.class,
            () -> printerService.getPrinterById("unknown")
        );
    }

    @Test
    void setPrinterEnabled_False_SetsDisabledStatus() {
        Printer printer = new Printer("ptr-101", "HP", "Lib", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null, true, null, Instant.now(), Instant.now());
        when(printerRepository.findById("ptr-101")).thenReturn(Optional.of(printer));
        when(printerRepository.save(any(Printer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PrinterResponse response = printerService.setPrinterEnabled("ptr-101", false);

        assertFalse(response.getEnabled());
        assertEquals(PrinterStatus.DISABLED, response.getStatus());
    }

    @Test
    void validateCompatibility_Compatible_Success() {
        Printer printer = new Printer("ptr-1", "HP", "Lib", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE, ColorMode.COLOR), Set.of(PaperSize.A4, PaperSize.A3), true, null, true, null, Instant.now(), Instant.now());
        PrintJob job = new PrintJob("job-1", "s-1", "d-1", 1, ColorMode.COLOR, PaperSize.A4, true, 2, new BigDecimal("10.00"), PrintJobStatus.QUEUED, true, null, null, Instant.now(), Instant.now());

        printerService.validateCompatibility(printer, job);
    }

    @Test
    void validateCompatibility_IncompatibleColor_ThrowsException() {
        Printer printer = new Printer("ptr-1", "HP", "Lib", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), true, null, true, null, Instant.now(), Instant.now());
        PrintJob job = new PrintJob("job-1", "s-1", "d-1", 1, ColorMode.COLOR, PaperSize.A4, false, 2, new BigDecimal("10.00"), PrintJobStatus.QUEUED, true, null, null, Instant.now(), Instant.now());

        assertThrows(
            IncompatiblePrinterException.class,
            () -> printerService.validateCompatibility(printer, job)
        );
    }

    @Test
    void validateCompatibility_IncompatiblePaper_ThrowsException() {
        Printer printer = new Printer("ptr-1", "HP", "Lib", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), true, null, true, null, Instant.now(), Instant.now());
        PrintJob job = new PrintJob("job-1", "s-1", "d-1", 1, ColorMode.BLACK_WHITE, PaperSize.A3, false, 2, new BigDecimal("10.00"), PrintJobStatus.QUEUED, true, null, null, Instant.now(), Instant.now());

        assertThrows(
            IncompatiblePrinterException.class,
            () -> printerService.validateCompatibility(printer, job)
        );
    }

    @Test
    void validateCompatibility_IncompatibleDuplex_ThrowsException() {
        Printer printer = new Printer("ptr-1", "HP", "Lib", PrinterStatus.ONLINE, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), false, null, true, null, Instant.now(), Instant.now());
        PrintJob job = new PrintJob("job-1", "s-1", "d-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, true, 2, new BigDecimal("10.00"), PrintJobStatus.QUEUED, true, null, null, Instant.now(), Instant.now());

        assertThrows(
            IncompatiblePrinterException.class,
            () -> printerService.validateCompatibility(printer, job)
        );
    }

    @Test
    void validateCompatibility_DisabledPrinter_ThrowsPrinterUnavailableException() {
        Printer printer = new Printer("ptr-1", "HP", "Lib", PrinterStatus.DISABLED, Set.of(ColorMode.BLACK_WHITE), Set.of(PaperSize.A4), true, null, false, null, Instant.now(), Instant.now());
        PrintJob job = new PrintJob("job-1", "s-1", "d-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 2, new BigDecimal("10.00"), PrintJobStatus.QUEUED, true, null, null, Instant.now(), Instant.now());

        assertThrows(
            PrinterUnavailableException.class,
            () -> printerService.validateCompatibility(printer, job)
        );
    }
}
