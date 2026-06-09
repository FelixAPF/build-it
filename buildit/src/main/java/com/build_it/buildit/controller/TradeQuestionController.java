package com.build_it.buildit.controller;

import com.build_it.buildit.entity.TradeQuestion;
import com.build_it.buildit.repository.TradeQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/trade-questions")
@RequiredArgsConstructor
public class TradeQuestionController {
  private final TradeQuestionRepository tradeQuestionRepository;

  @GetMapping
  public List<TradeQuestion> getAllQuestions() {
    return tradeQuestionRepository.findAll();
  }
}
