package com.example.gamebackend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.gamebackend.model.Score;
import com.example.gamebackend.repository.ScoreRepository;

@Service
public class ScoreService {
    private final ScoreRepository repo;

    public ScoreService(ScoreRepository repo) {
        this.repo = repo;
    }

    public Score save(Score s) {
        return repo.save(s);
    }

    public List<Score> top10() {
        return repo.findTop10ByOrderByScoreDesc();
    }

    public int highestScore() {
        Score topScore = repo.findTopByOrderByScoreDesc();
        return topScore != null ? topScore.getScore() : 0;
    }
}
