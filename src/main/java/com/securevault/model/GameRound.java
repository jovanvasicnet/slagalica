package com.securevault.model;

import java.util.*;

public class GameRound {

    public int matchId;

    public List<Character> letters;

    public String solution;

    public long startTime;

    public Map<Integer,String> answers = new HashMap<>();


    public int getMatchId() {
            return matchId;
    }
    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }
    public List<Character> getLetters() {
        return letters;
    }
    public void setLetters(List<Character> letters) {
        this.letters = letters;
    }
    public String getSolution() {
        return solution;
    }
    public void setSolution(String solution) {
        this.solution = solution;
    }
    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public Map<Integer,String> getAnswers() {
        return answers;
    }
    public void setAnswers(Map<Integer,String> answers) {
        this.answers = answers;
    }

}