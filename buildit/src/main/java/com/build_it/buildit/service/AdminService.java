package com.build_it.buildit.service;

import com.build_it.buildit.entity.AccountStatus;
import com.build_it.buildit.entity.User;
import com.build_it.buildit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;

  @Transactional
  public String verifyUserAccount(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

    if (user.getStatus() == AccountStatus.ACTIVE) {
      return "User is already ACTIVE.";
    }

    user.setStatus(AccountStatus.ACTIVE);
    userRepository.save(user);

    // Down the road, we will trigger an email notification here: "Your account is approved!"
    return "User account for " + user.getEmail() + " has been successfully verified and activated.";
  }
}
