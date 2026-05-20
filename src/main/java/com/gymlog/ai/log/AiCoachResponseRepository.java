package com.gymlog.ai.log;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiCoachResponseRepository extends JpaRepository<AiCoachResponse, Long> {

    List<AiCoachResponse> findByUserIdOrderByCreatedAtDesc(Long userId);
}