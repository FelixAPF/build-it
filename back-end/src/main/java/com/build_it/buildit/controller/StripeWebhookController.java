package com.build_it.buildit.controller;

import com.build_it.buildit.entity.JobPosting;
import com.build_it.buildit.entity.JobStatus;
import com.build_it.buildit.repository.JobPostingRepository;
import com.build_it.buildit.service.AuditLogService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

  private final JobPostingRepository jobPostingRepository;
  private final AuditLogService auditLogService;

  @Value("${stripe.webhook.secret}")
  private String endpointSecret;

  // Webhooks MUST consume the raw payload string, not a JSON mapped object!
  @PostMapping
  public ResponseEntity<String> handleStripeEvent(
    @RequestBody String payload,
    @RequestHeader("Stripe-Signature") String sigHeader) {

    Event event;

    try {
      // Verify the payload actually came from Stripe using the secret key
      event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
    } catch (SignatureVerificationException e) {
      System.err.println("⚠️ Webhook error: Invalid signature.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
    }

    // Handle the specific event when a user's credit card successfully charges
    if ("checkout.session.completed".equals(event.getType())) {

      EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
      if (dataObjectDeserializer.getObject().isPresent()) {

        Session session = (Session) dataObjectDeserializer.getObject().get();

        // Retrieve the jobId we injected into the metadata
        String jobIdStr = session.getMetadata().get("jobId");

        if (jobIdStr != null) {
          try {
            Long jobId = Long.parseLong(jobIdStr);
            JobPosting job = jobPostingRepository.findById(jobId).orElse(null);

            if (job != null && job.getStatus() == JobStatus.PENDING_PAYMENT) {
              // Activate the job natively in the background!
              job.setStatus(JobStatus.OPEN);
              jobPostingRepository.save(job);

              auditLogService.log("STRIPE_SYSTEM", "PAYMENT_WEBHOOK_SUCCESS",
                "Stripe confirmed payment for session " + session.getId() + ". Unlocked job ID: " + jobId);

              System.out.println("✅ Webhook triggered: Job " + jobId + " is now OPEN.");
            }
          } catch (Exception e) {
            System.err.println("⚠️ Webhook processing failed for Job ID: " + jobIdStr);
          }
        }
      }
    }

    // Always return 200 OK so Stripe knows we received it
    return ResponseEntity.ok("Received");
  }
}
