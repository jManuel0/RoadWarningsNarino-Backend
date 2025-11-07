package com.roadwarnings.narino.repository;

import com.roadwarnings.narino.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAlertId(Long alertId);
    List<Comment> findByUserId(Long userId);
}