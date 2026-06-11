package com.build_it.buildit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
  private String token;
  private String email;
  private String role;
  private String status;
}
