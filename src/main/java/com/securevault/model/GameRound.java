package com.securevault.model;

import java.util.*;

public class GameRound {

    private int matchId;
    private String state;
    private long countdownStart;
    private long playStart;
    private long resultStart;
    private int team1Id;
    private int team2Id;

    private List<Character> letters;

    private String solution;

    public long startTime;

    private Map<Integer,String> answers = new HashMap<>();

    private Map<String,Object> result;

    public Map<String, Object> getResult() {
        return result;
    }
    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

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

    public long getCountdownStart() {
        return countdownStart;
    }
    public void setCountdownStart(long countdownStart) {
        this.countdownStart = countdownStart;

    }

    public long getPlayStart() {
        return playStart;
    }
    public void setPlayStart(long playStart) {
        this.playStart = playStart;
    }
    public long getResultStart() {
        return resultStart;
    }
    public void setResultStart(long resultStart) {
        this.resultStart = resultStart;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;

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