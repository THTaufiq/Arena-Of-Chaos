package com.example.gamebackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gamebackend.model.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findTop10ByOrderByScoreDesc();
    Score findTopByOrderByScoreDesc();
}
