package com.build_it.buildit.controller;

import com.build_it.buildit.entity.Trade;
import com.build_it.buildit.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {
  private final TradeRepository tradeRepository;

  @GetMapping
  public List<Trade> getAllTrades() {
    return tradeRepository.findAll();
  }
}
