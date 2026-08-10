package com.tsena.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteDTO {

    private Long id;

    @NotBlank
    private String nom;

    @NotBlank
    private String localisation;

    private Boolean actif;
}
