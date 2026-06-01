package com.build_it.buildit.repository;

import com.build_it.buildit.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  // Fetch all reviews given TO a specific user to calculate their average star rating
  List<Review> findByRevieweeId(Long revieweeId);
}
