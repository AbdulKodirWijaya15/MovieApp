package com.KamilIsmail.MovieApp.service;

import com.KamilIsmail.MovieApp.DTO.BooleanDTO;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ParseService {
    @PreAuthorize("hasAnyAuthority('admin')")
    BooleanDTO parseTVGuide();
}
