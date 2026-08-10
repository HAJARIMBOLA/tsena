package com.tsena.app.dto;

import com.tsena.app.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;
    private Long id;
    private String nom;
    private String email;
    private Role role;

    @Builder.Default
    private Set<Long> siteIds = new HashSet<>();
}
