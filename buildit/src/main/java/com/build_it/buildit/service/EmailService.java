package com.build_it.buildit.service;

import jakarta.mail.internet.MimeMessage; // <-- New Import
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper; // <-- New Import
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

  @Value("${spring.mail.username}")
  private String mailUsername;

  private final JavaMailSender mailSender;

  @Async
  public void sendWorkerHiredEmail(String toEmail, String workerName, String companyName, String address, String startTime) {
    System.out.println("==================================================================");
    System.out.println("--> EMAIL SERVICE: Attempting to send structured HTML email...");
    System.out.println("==================================================================");

    try {
      // Parse and format the date nicely
      LocalDateTime dateTime = LocalDateTime.parse(startTime);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
      String humanReadableTime = dateTime.format(formatter);

      // Create a sophisticated multi-part MIME email instead of plain text
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      helper.setFrom(mailUsername);
      helper.setTo(toEmail);
      helper.setSubject("🎉 You've been hired for a shift by " + companyName + "!");

      // High-converting CSS container layout that passes corporate spam parameters easily
      String htmlContent =
        "<div style='font-family: Arial, sans-serif; background-color: #f8fafc; padding: 40px; color: #334155;'>" +
          "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #e2e8f0;'>" +

          // Header
          "<div style='background-color: #2563eb; padding: 30px; text-align: center;'>" +
          "<h1 style='color: #ffffff; margin: 0; font-size: 24px; font-weight: 800; letter-tight: -0.05em;'>BuildIt Marketplace</h1>" +
          "</div>" +

          // Body
          "<div style='padding: 40px;'>" +
          "<h2 style='color: #1e293b; margin-top: 0; font-size: 20px;'>Hello " + workerName + ",</h2>" +
          "<p style='font-size: 16px; line-height: 1.6; color: #475569;'>Great news! Your application has been officially reviewed and approved by <strong>" + companyName + "</strong>. Your shift schedule is locked in.</p>" +

          // Shift Details Card Box
          "<div style='background-color: #f1f5f9; border-left: 4px solid #2563eb; padding: 20px; margin: 30px 0; border-radius: 0 8px 8px 0;'>" +
          "<h3 style='margin-top: 0; color: #1e293b; font-size: 16px;'>Shift Assignment Details</h3>" +
          "<p style='margin: 8px 0; font-size: 14px;'><strong>Company:</strong> " + companyName + "</p>" +
          "<p style='margin: 8px 0; font-size: 14px;'><strong>Site Address:</strong> " + address + "</p>" +
          "<p style='margin: 8px 0; font-size: 14px;'><strong>Report Time:</strong> " + humanReadableTime + "</p>" +
          "</div>" +

          "<p style='font-size: 14px; line-height: 1.6; color: #64748b; background-color: #fffbeb; border: 1px solid #fef3c7; padding: 12px; border-radius: 6px;'>" +
          "⚠️ <strong>Compliance Reminder:</strong> Please ensure you bring your valid CCQ competency cards, apprentice/journeyman logbooks, and appropriate high-visibility PPE to the job site entry gate." +
          "</p>" +
          "</div>" +

          // Footer
          "<div style='background-color: #f8fafc; padding: 20px; text-align: center; border-t: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8;'>" +
          "<p style='margin: 0;'>This is an automated operational notification matching your Act R-20 workforce preferences.</p>" +
          "<p style='margin: 5px 0 0 0;'>&copy; 2026 BuildIt Technologies Inc. Montreal, QC</p>" +
          "</div>" +

          "</div>" +
          "</div>";

      // Set 'true' parameter to interpret the body string as rich HTML
      helper.setText(htmlContent, true);

      mailSender.send(mimeMessage);
      System.out.println("--> EMAIL SERVICE SUCCESS: Rich HTML MimeMessage dispatched safely!");
      System.out.println("==================================================================");

    } catch (Exception e) {
      System.err.println("--> EMAIL SERVICE CRITICAL ERROR: HTML email compilation failed!");
      e.printStackTrace();
      System.err.println("==================================================================");
    }
  }

  @Async
  public void sendPasswordResetEmail(String toEmail, String token) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      helper.setFrom(mailUsername);
      helper.setTo(toEmail);
      helper.setSubject("Password Reset Request - BuildIt");

      // The Angular Route we will create in Step 2
      String resetUrl = "http://localhost:4200/reset-password?token=" + token;

      String htmlContent =
        "<div style='font-family: Arial, sans-serif; background-color: #f8fafc; padding: 40px; color: #334155;'>" +
          "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0;'>" +
          "<div style='background-color: #2563eb; padding: 30px; text-align: center;'>" +
          "<h1 style='color: #ffffff; margin: 0; font-size: 24px; font-weight: 800;'>BuildIt Password Recovery</h1>" +
          "</div>" +
          "<div style='padding: 40px; text-align: center;'>" +
          "<h2 style='color: #1e293b; margin-top: 0; font-size: 20px;'>Forgot your password?</h2>" +
          "<p style='font-size: 16px; line-height: 1.6; color: #475569;'>No problem. Click the button below to securely set a new password. This link will expire in 1 hour.</p>" +
          "<a href='" + resetUrl + "' style='display: inline-block; background-color: #2563eb; color: #ffffff; padding: 14px 28px; margin: 20px 0; text-decoration: none; border-radius: 8px; font-weight: bold;'>Reset My Password</a>" +
          "<p style='font-size: 14px; line-height: 1.6; color: #94a3b8;'>If you did not request this, you can safely ignore this email.</p>" +
          "</div></div></div>";

      helper.setText(htmlContent, true);
      mailSender.send(mimeMessage);

    } catch (Exception e) {
      System.err.println("Failed to send password reset email.");
    }
  }

  @Async
  public void sendVerificationEmail(String toEmail, String token) {
    try {
      jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
      org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "UTF-8");

      helper.setFrom(mailUsername);
      helper.setTo(toEmail);
      helper.setSubject("Verify Your BuildIt Account");

      String verifyUrl = "http://localhost:4200/verify-email?token=" + token;

      String htmlContent =
        "<div style='font-family: Arial, sans-serif; background-color: #f8fafc; padding: 40px; color: #334155;'>" +
          "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0;'>" +
          "<div style='background-color: #10b981; padding: 30px; text-align: center;'>" +
          "<h1 style='color: #ffffff; margin: 0; font-size: 24px; font-weight: 800;'>Welcome to BuildIt!</h1>" +
          "</div>" +
          "<div style='padding: 40px; text-align: center;'>" +
          "<h2 style='color: #1e293b; margin-top: 0; font-size: 20px;'>Almost there...</h2>" +
          "<p style='font-size: 16px; line-height: 1.6; color: #475569;'>Please verify your email address to activate your account and access the marketplace.</p>" +
          "<a href='" + verifyUrl + "' style='display: inline-block; background-color: #10b981; color: #ffffff; padding: 14px 28px; margin: 20px 0; text-decoration: none; border-radius: 8px; font-weight: bold;'>Verify My Email</a>" +
          "</div></div></div>";

      helper.setText(htmlContent, true);
      mailSender.send(mimeMessage);

    } catch (Exception e) {
      System.err.println("Failed to send verification email.");
    }
  }
}
