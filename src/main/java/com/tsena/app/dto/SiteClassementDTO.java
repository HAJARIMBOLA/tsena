package com.tsena.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiteClassementDTO {

    private Long siteId;
    private String siteNom;
    private BigDecimal chiffreAffaires;
}
