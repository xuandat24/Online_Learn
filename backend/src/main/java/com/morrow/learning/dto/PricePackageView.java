package com.morrow.learning.dto;

import com.morrow.learning.domain.PricePackage;
import java.math.BigDecimal;

public record PricePackageView(Long id, String name, BigDecimal price, String currency,
                               int accessDays, boolean published) {
    public static PricePackageView from(PricePackage pricePackage) {
        return new PricePackageView(pricePackage.getId(), pricePackage.getName(), pricePackage.getPrice(),
                pricePackage.getCurrency(), pricePackage.getAccessDays(), pricePackage.isPublished());
    }
}