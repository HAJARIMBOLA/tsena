package com.tsena.app.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("siteAccessService")
public class SiteAccessService {

    public boolean aAcces(Long siteId, Authentication authentication) {
        if (siteId == null || authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            return false;
        }

        boolean estAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (estAdmin) {
            return true;
        }

        return principal.getSitesAutorises().stream()
                .anyMatch(site -> siteId.equals(site.getId()));
    }
}
