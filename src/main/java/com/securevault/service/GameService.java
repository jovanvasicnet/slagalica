package com.securevault.service;

import com.securevault.model.GameRound;

import java.util.*;

public class GameService {

    public static Map<Integer, GameRound> activeGames = new HashMap<>();

    static Random rand = new Random();

    public static GameRound generateGame(int matchId){

        // RANDOM DUŽINA 10-12
        int len = 10 + rand.nextInt(3);

        List<String> candidates = WordService.wordsByLength.get(len);

        if(candidates == null || candidates.isEmpty()){
            throw new RuntimeException("Nema riječi dužine "+len);
        }

        // RANDOM RIJEČ TE DUŽINE
        String solution = candidates.get(
                rand.nextInt(candidates.size())
        );

        List<Character> letters = new ArrayList<>();

        for(char c : solution.toCharArray()){
            letters.add(c);
        }

        // DODAJ RANDOM SLOVA DO 12
        while(letters.size() < 12){

            char randomLetter = (char)('A' + rand.nextInt(26));
            letters.add(randomLetter);

        }

        // IZMIJEŠAJ
        Collections.shuffle(letters);

        GameRound g = new GameRound();

        g.matchId = matchId;
        g.solution = solution;
        g.letters = letters;
        g.startTime = System.currentTimeMillis();

        activeGames.put(matchId,g);

        return g;
    }

    public static boolean canBuild(String word, List<Character> letters){

        List<Character> temp = new ArrayList<>(letters);

        for(char c : word.toCharArray()){

            if(!temp.remove((Character)c)){
                return false;
            }

        }

        return true;
    }

    public static int score(GameRound g, String word){

        if(word == null) return 0;

        if(!WordService.exists(word)) return 0;

        if(!canBuild(word,g.letters)) return 0;

        int points = word.length();

        if(word.equals(g.solution)){
            points += 3;
        }

        if(word.length() > g.solution.length()){
            points += 6;
        }

        return points;
    }

    public static Map<String,Object> calculateResult(GameRound g){

        Map<String,Object> res = new HashMap<>();

        String w1 = g.getAnswers().getOrDefault(g.getTeam1Id(),"");
        String w2 = g.getAnswers().getOrDefault(g.getTeam2Id(),"");

        int p1 = score(g,w1);
        int p2 = score(g,w2);

        if(p1 > p2){
            p1 += 6;
        }else if(p2 > p1){
            p2 += 6;
        }

        res.put("team1Word",w1);
        res.put("team2Word",w2);

        res.put("team1Points",p1);
        res.put("team2Points",p2);

        res.put("solution",g.getSolution());

        return res;
    }

}