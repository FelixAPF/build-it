package com.build_it.buildit.dto;
import com.build_it.buildit.entity.PaymentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class JobRequirementRequest {
  @NotBlank(message = "Job type is required")
  private String jobType;

  @NotNull(message = "Payment type is required")
  private PaymentType paymentType;

  @NotNull(message = "Pay amount is required")
  @Min(value = 1, message = "Pay amount must be greater than 0")
  private Double payRate;

  @NotNull(message = "Quantity requested is required")
  @Min(value = 1, message = "Must request at least 1 worker")
  private Integer qtyRequested;

  private List<RequirementAnswerDto> answers;
}
