package com.securevault.service;

import com.securevault.model.GameRound;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class GameService {

    public static Map<Integer, GameRound> activeGames = new HashMap<>();

    static Random rand = new Random();

    public static GameRound generateGame(int matchId){

        GameRound g = new GameRound();

        try(Connection conn = DatabaseService.connect()){

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT team1_id, team2_id FROM matches WHERE id=?"
            );

            ps.setInt(1,matchId);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                g.setTeam1Id(rs.getInt("team1_id"));
                g.setTeam2Id(rs.getInt("team2_id"));

            }

        }catch(Exception e){
            e.printStackTrace();
        }

        // ostatak tvoje logike

        int len = 10 + rand.nextInt(3);

        List<String> candidates = WordService.wordsByLength.get(len);

        String solution = candidates.get(rand.nextInt(candidates.size()));

        List<Character> letters = new ArrayList<>();

        for(char c : solution.toCharArray()){
            letters.add(c);
        }

        while(letters.size() < 12){
            letters.add((char)('A' + rand.nextInt(26)));
        }

        Collections.shuffle(letters);

        g.setMatchId(matchId);
        g.setSolution(solution);
        g.setLetters(letters);
        g.setStartTime(System.currentTimeMillis());
        g.setState("COUNTDOWN");
        g.setCountdownStart(System.currentTimeMillis());

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

        if(!canBuild(word,g.getLetters())) return 0;

        int points = word.length();

        if(word.equals(g.getSolution())){
            points += 3;
        }

        if(word.length() > g.getSolution().length()){
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