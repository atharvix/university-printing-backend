package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }

    @Test
    void calculatePrice_BlackWhite_A4_SingleSided_SinglePage() {
        // 1 page, 1 copy, B&W, A4, single-sided: ₹2.00
        BigDecimal price = pricingService.calculatePrice(ColorMode.BLACK_WHITE, PaperSize.A4, false, 1, 1);
        assertEquals(new BigDecimal("2.00"), price);
    }

    @Test
    void calculatePrice_BlackWhite_A4_SingleSided_MultiCopy() {
        // 10 pages, 2 copies, B&W, A4, single-sided: 2.00 * 10 * 2 = ₹40.00
        BigDecimal price = pricingService.calculatePrice(ColorMode.BLACK_WHITE, PaperSize.A4, false, 10, 2);
        assertEquals(new BigDecimal("40.00"), price);
    }

    @Test
    void calculatePrice_BlackWhite_A4_Duplex() {
        // 10 pages, 1 copy, B&W, A4, duplex: 2.00 * 10 * 1 * 1.00 * 0.90 = ₹18.00
        BigDecimal price = pricingService.calculatePrice(ColorMode.BLACK_WHITE, PaperSize.A4, true, 10, 1);
        assertEquals(new BigDecimal("18.00"), price);
    }

    @Test
    void calculatePrice_Color_A4_SingleSided() {
        // 5 pages, 1 copy, COLOR, A4, single-sided: 5.00 * 5 * 1 * 1.00 * 1.00 = ₹25.00
        BigDecimal price = pricingService.calculatePrice(ColorMode.COLOR, PaperSize.A4, false, 5, 1);
        assertEquals(new BigDecimal("25.00"), price);
    }

    @Test
    void calculatePrice_Color_A3_Duplex() {
        // 10 pages, 1 copy, COLOR, A3, duplex: 5.00 * 10 * 1 * 1.50 * 0.90 = ₹67.50
        BigDecimal price = pricingService.calculatePrice(ColorMode.COLOR, PaperSize.A3, true, 10, 1);
        assertEquals(new BigDecimal("67.50"), price);
    }

    @Test
    void calculatePrice_Color_A3_SingleSided_MultiCopy() {
        // 2 pages, 3 copies, COLOR, A3, single-sided: 5.00 * 2 * 3 * 1.50 * 1.00 = ₹45.00
        BigDecimal price = pricingService.calculatePrice(ColorMode.COLOR, PaperSize.A3, false, 2, 3);
        assertEquals(new BigDecimal("45.00"), price);
    }

    @Test
    void calculatePrice_InvalidInputs_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            pricingService.calculatePrice(ColorMode.BLACK_WHITE, PaperSize.A4, false, 0, 1)
        );
        assertThrows(IllegalArgumentException.class, () ->
            pricingService.calculatePrice(ColorMode.BLACK_WHITE, PaperSize.A4, false, 1, 0)
        );
        assertThrows(IllegalArgumentException.class, () ->
            pricingService.calculatePrice(null, PaperSize.A4, false, 1, 1)
        );
        assertThrows(IllegalArgumentException.class, () ->
            pricingService.calculatePrice(ColorMode.BLACK_WHITE, null, false, 1, 1)
        );
    }
}
