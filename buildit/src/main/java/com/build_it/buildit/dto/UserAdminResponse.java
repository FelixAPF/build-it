package com.build_it.buildit.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserAdminResponse {
  private Long userId;
  private String email;
  private String role;
  private String name; // Company name or Worker Name
  private String identificationNumber; // RBQ or CCQ
  private LocalDateTime createdAt;
  private String documentUrl;
  private String status;
}
