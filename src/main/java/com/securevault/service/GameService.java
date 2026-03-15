package com.securevault.service;

import com.securevault.model.GameRound;

import java.util.*;

public class GameService {

    public static Map<Integer, GameRound> activeGames = new HashMap<>();

    static Random rand = new Random();

    public static GameRound generateGame(int matchId){

        int len = 10 + rand.nextInt(3); // 10-12

        List<String> candidates = new ArrayList<>();

        for(String w : WordService.words){

            if(w.length() == len){
                candidates.add(w);
            }

        }

        String solution = candidates.get(rand.nextInt(candidates.size()));

        List<Character> letters = new ArrayList<>();

        for(char c : solution.toCharArray()){
            letters.add(c);
        }

        while(letters.size() < 12){

            char randomLetter = (char)('A' + rand.nextInt(26));
            letters.add(randomLetter);

        }

        Collections.shuffle(letters);

        GameRound g = new GameRound();

        g.matchId = matchId;
        g.solution = solution;
        g.letters = letters;
        g.startTime = System.currentTimeMillis();

        activeGames.put(matchId,g);

        return g;
    }

}
