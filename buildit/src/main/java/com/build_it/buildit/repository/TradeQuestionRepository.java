package com.build_it.buildit.repository;
import com.build_it.buildit.entity.TradeQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeQuestionRepository extends JpaRepository<TradeQuestion, Long> {
  List<TradeQuestion> findByJobType(String jobType);
}
