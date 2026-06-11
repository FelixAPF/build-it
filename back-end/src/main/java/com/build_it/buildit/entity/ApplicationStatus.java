package com.build_it.buildit.entity;

public enum ApplicationStatus {
  PENDING,
  SELECTED,
  REJECTED,
  WITHDRAWN,
  AUTO_CANCELLED // Triggered when a worker is selected for a different job at the same time
}
