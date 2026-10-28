package com.example.gamebackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gamebackend.model.Score;
import com.example.gamebackend.service.ScoreService;

@RestController
@RequestMapping("/api")
public class LeaderboardController {
    private final ScoreService service;

    public LeaderboardController(ScoreService service) {
        this.service = service;
    }

    @GetMapping("/leaderboard")
    public List<Score> leaderboard() {
        return service.top10();
    }

    @GetMapping("/score/highest")
    public Map<String, Integer> highestScore() {
        return Map.of("highScore", service.highestScore());
    }

    @PostMapping("/score")
    public Score postScore(@RequestBody Score s) {
        return service.save(s);
    }

    @PostMapping("/score/save")
    public Score postScoreSave(@RequestBody Score s) {
        return service.save(s);
    }
}
