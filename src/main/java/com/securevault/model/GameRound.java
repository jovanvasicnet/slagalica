package com.securevault.model;

import java.util.*;

public class GameRound {

    private int matchId;

    public int team1Id;
    public int team2Id;

    private List<Character> letters;

    private String solution;

    public long startTime;

    public Map<Integer,String> answers = new HashMap<>();


    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }


    public int getTeam1Id() {
        return team1Id;
    }

    public void setTeam1Id(int team1Id) {
        this.team1Id = team1Id;
    }


    public int getTeam2Id() {
        return team2Id;
    }

    public void setTeam2Id(int team2Id) {
        this.team2Id = team2Id;
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