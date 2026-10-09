package com.onlinelearn.dto.expert;

import com.onlinelearn.entity.enums.PackageStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricePackageFormDTO {

    private Long id;
    private Long subjectId;
    private String packageName;
    private Integer accessDuration;
    private BigDecimal listPrice;
    private BigDecimal salePrice;
    private String description;
    private PackageStatus status;
}
