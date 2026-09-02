package com.universityprinting.printing_backend.service;

import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private static final BigDecimal PRICE_BW = new BigDecimal("2.00");
    private static final BigDecimal PRICE_COLOR = new BigDecimal("5.00");

    private static final BigDecimal MULTIPLIER_A4 = new BigDecimal("1.00");
    private static final BigDecimal MULTIPLIER_A3 = new BigDecimal("1.50");

    private static final BigDecimal MULTIPLIER_SIMPLEX = new BigDecimal("1.00");
    private static final BigDecimal MULTIPLIER_DUPLEX = new BigDecimal("0.90");

    public BigDecimal calculatePrice(ColorMode colorMode, PaperSize paperSize, boolean duplex, int pageCount, int copies) {
        if (pageCount < 1) {
            throw new IllegalArgumentException("Page count must be at least 1");
        }
        if (copies < 1) {
            throw new IllegalArgumentException("Copies must be at least 1");
        }
        if (colorMode == null) {
            throw new IllegalArgumentException("Color mode cannot be null");
        }
        if (paperSize == null) {
            throw new IllegalArgumentException("Paper size cannot be null");
        }

        BigDecimal basePerPage = (colorMode == ColorMode.COLOR) ? PRICE_COLOR : PRICE_BW;
        BigDecimal paperMultiplier = (paperSize == PaperSize.A3) ? MULTIPLIER_A3 : MULTIPLIER_A4;
        BigDecimal duplexMultiplier = duplex ? MULTIPLIER_DUPLEX : MULTIPLIER_SIMPLEX;

        BigDecimal totalPages = BigDecimal.valueOf(pageCount);
        BigDecimal totalCopies = BigDecimal.valueOf(copies);

        BigDecimal price = basePerPage
            .multiply(totalPages)
            .multiply(totalCopies)
            .multiply(paperMultiplier)
            .multiply(duplexMultiplier);

        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
