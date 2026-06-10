package com.build_it.buildit.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseConstraintFixer implements CommandLineRunner {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void run(String... args) {
    try {
      // Drop the outdated Enum check constraint for Job Postings
      jdbcTemplate.execute("ALTER TABLE job_postings DROP CONSTRAINT IF EXISTS job_postings_status_check");

      // Also drop the one for Applications since we recently added 'AUTO_CANCELLED' to that Enum too!
      jdbcTemplate.execute("ALTER TABLE job_applications DROP CONSTRAINT IF EXISTS job_applications_status_check");

      System.out.println("✅ Successfully cleared old Enum constraints from PostgreSQL!");
    } catch (Exception e) {
      System.out.println("ℹ️ Enum constraint check skipped or already removed.");
    }
  }
}
