package com.securevault.controller;

import com.securevault.model.*;
import com.securevault.security.*;
import com.securevault.service.DatabaseService;
import com.securevault.service.GameService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import java.sql.Timestamp;
import java.time.Instant;

import java.sql.*;
import java.util.*;

@RestController
public class MainController {

    @GetMapping("/")
    public String home() {
        return "Server radi";
    }

    @GetMapping("/ping")
    public String ping() {
        return "ok";
    }

    @PostMapping("/admin/login")
    public Map<String, Object> adminLogin(@RequestBody LoginRequest request) {

        Map<String,Object> res = new HashMap<>();

        try(Connection conn = DatabaseService.connect()) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT password FROM admin WHERE username=?"
            );

            ps.setString(1, request.getUsername());

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                String hash = rs.getString("password");

                if(BCrypt.checkpw(request.getPassword(), hash)) {

                    String token = JwtUtil.generateToken(request.getUsername());

                    res.put("success", true);
                    res.put("token", token);

                    return res;
                }
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        res.put("success", false);
        return res;
    }

    @GetMapping("/admin/tournaments")
    public List<Map<String,Object>> tournaments() {

        List<Map<String,Object>> list = new ArrayList<>();


        try (Connection conn = DatabaseService.connect()){


            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM tournaments ORDER BY start_time DESC"
            );

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                Map<String,Object> t = new HashMap<>();

                t.put("id", rs.getInt("id"));
                t.put("name", rs.getString("name"));
                t.put("location", rs.getString("location"));
                t.put("bar", rs.getString("bar_name"));
                t.put("status", rs.getString("status"));

                list.add(t);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @PostMapping("/admin/tournament/create")
    public String createTournament(@RequestBody TournamentRequest req) {

        try(Connection conn = DatabaseService.connect()) {

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO tournaments(name,quiz_type,location,bar_name,start_time,planned_start,status) VALUES(?,?,?,?,?,?,?)"
            );

            ps.setString(1, req.getName());
            ps.setString(2, req.getQuizType());
            ps.setString(3, req.getLocation());
            ps.setString(4, req.getBarName());

            ps.setTimestamp(5, Timestamp.from(Instant.parse(req.getStartTime())));
            ps.setTimestamp(6, Timestamp.from(Instant.parse(req.getPlannedStart())));

            ps.setString(7, "CREATED");

            ps.executeUpdate();

            return "tournament_created";

        } catch(Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/admin/tournament/{id}/teams")
    public List<Map<String,Object>> teams(@PathVariable int id) {

        List<Map<String,Object>> list = new ArrayList<>();

        try(Connection conn = DatabaseService.connect()) {


            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id,name,members FROM tournament_teams WHERE tournament_id=?"
            );

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                Map<String,Object> t = new HashMap<>();

                t.put("id", rs.getInt("id"));
                t.put("name", rs.getString("name"));
                t.put("members", rs.getInt("members"));

                list.add(t);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @PostMapping("/admin/team/add")
    public String addTeam(@RequestBody TeamRequest req) {

        try(Connection conn = DatabaseService.connect()) {


            int baseTeamId = -1;

            // 1️⃣ provjeri da li base team postoji
            PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM base_teams WHERE name=? AND location=?"
            );

            check.setString(1, req.getName());
            check.setString(2, req.getLocation());

            ResultSet rs = check.executeQuery();

            if(rs.next()) {

                baseTeamId = rs.getInt("id");

            } else {

                // 2️⃣ kreiraj base team
                PreparedStatement insertBase = conn.prepareStatement(
                        "INSERT INTO base_teams(name,location,image_url) VALUES(?,?,?)",
                        Statement.RETURN_GENERATED_KEYS
                );

                insertBase.setString(1, req.getName());
                insertBase.setString(2, req.getLocation());
                insertBase.setString(3, req.getImageUrl());

                insertBase.executeUpdate();

                ResultSet generated = insertBase.getGeneratedKeys();

                if(generated.next()) {
                    baseTeamId = generated.getInt(1);
                }
            }

            // 3️⃣ hash password
            String hash = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt());

            // 4️⃣ insert u tournament teams
            PreparedStatement insertTeam = conn.prepareStatement(
                    "INSERT INTO tournament_teams(tournament_id,base_team_id,name,password,image_url,members) VALUES(?,?,?,?,?,?)"
            );

            insertTeam.setInt(1, req.getTournamentId());
            insertTeam.setInt(2, baseTeamId);
            insertTeam.setString(3, req.getName());
            insertTeam.setString(4, hash);
            insertTeam.setString(5, req.getImageUrl());
            insertTeam.setInt(6, req.getMembers());

            insertTeam.executeUpdate();

            return "team_added";

        } catch(Exception e) {
            e.printStackTrace();
        }

        return "error";
    }


    @PostMapping("/admin/tournament/start/{id}")
    public String startTournament(@PathVariable int id) {

        try(Connection conn = DatabaseService.connect()) {

            List<Integer> teams = new ArrayList<>();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM tournament_teams WHERE tournament_id=?"
            );

            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                teams.add(rs.getInt("id"));
            }

            if(teams.size() < 2){
                return "not_enough_teams";
            }

            Collections.shuffle(teams);

            for(int i=0;i<teams.size();i+=2){

                int team1 = teams.get(i);
                Integer team2 = null;

                if(i+1 < teams.size()){
                    team2 = teams.get(i+1);
                }

                PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO matches(tournament_id,round,bracket,team1_id,team2_id,status) VALUES(?,?,?,?,?,?)"
                );

                insert.setInt(1,id);
                insert.setInt(2,1);
                insert.setString(3,"WINNERS");
                insert.setInt(4,team1);

                if(team2 == null){
                    insert.setNull(5,Types.INTEGER);
                }else{
                    insert.setInt(5,team2);
                }

                insert.setString(6,"WAITING");

                insert.executeUpdate();
            }

            PreparedStatement update = conn.prepareStatement(
                    "UPDATE tournaments SET status='STARTED' WHERE id=?"
            );

            update.setInt(1,id);
            update.executeUpdate();

            return "tournament_started";

        } catch(Exception e){
            e.printStackTrace();
        }

        return "error";
    }
    @GetMapping("/admin/tournament/{id}/matches")
    public List<Map<String,Object>> matches(@PathVariable int id){

        List<Map<String,Object>> list = new ArrayList<>();

        try(Connection conn = DatabaseService.connect()){

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT m.id, t1.name as team1, t2.name as team2 " +
                            "FROM matches m " +
                            "LEFT JOIN tournament_teams t1 ON m.team1_id=t1.id " +
                            "LEFT JOIN tournament_teams t2 ON m.team2_id=t2.id " +
                            "WHERE m.tournament_id=? AND round=1"
            );

            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){

                Map<String,Object> m = new HashMap<>();

                m.put("id",rs.getInt("id"));
                m.put("team1",rs.getString("team1"));
                m.put("team2",rs.getString("team2"));

                list.add(m);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }
    @PostMapping("/admin/tournament/finish/{id}")
    public String finishTournament(@PathVariable int id) {

        try(Connection conn = DatabaseService.connect()) {



            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE tournaments SET status='FINISHED' WHERE id=?"
            );

            ps.setInt(1, id);

            ps.executeUpdate();

            return "finished";

        } catch(Exception e) {
            e.printStackTrace();
        }

        return "error";
    }

    @GetMapping("/admin/base-teams/{location}")
    public List<Map<String,Object>> getBaseTeams(@PathVariable String location) {

        List<Map<String,Object>> list = new ArrayList<>();

        try (Connection conn = DatabaseService.connect()){



            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM base_teams WHERE location=?"
            );

            ps.setString(1, location);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                Map<String,Object> team = new HashMap<>();

                team.put("id", rs.getInt("id"));
                team.put("name", rs.getString("name"));
                team.put("image", rs.getString("image_url"));

                list.add(team);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @PostMapping("/team/join")
    public Map<String,Object> joinTeam(@RequestBody JoinTeamRequest req) {

        Map<String,Object> res = new HashMap<>();

        try(Connection conn = DatabaseService.connect()) {


            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id,password,tournament_id FROM tournament_teams WHERE name=? AND tournament_id=?"
            );

            ps.setString(1, req.getName());
            ps.setInt(2, req.getTournamentId());


            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                String hash = rs.getString("password");

                if(BCrypt.checkpw(req.getPassword(), hash)) {

                    res.put("success", true);
                    res.put("teamId", rs.getInt("id"));
                    res.put("tournamentId", rs.getInt("tournament_id"));

                    return res;
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        res.put("success", false);
        return res;
    }
    @GetMapping("/tournament/active")
    public Map<String,Object> getActiveTournament() {

        Map<String,Object> res = new HashMap<>();

        try(Connection conn = DatabaseService.connect()) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM tournaments WHERE status='STARTED' LIMIT 1"
            );

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                res.put("id", rs.getInt("id"));
                res.put("name", rs.getString("name"));
                res.put("location", rs.getString("location"));
                res.put("bar", rs.getString("bar_name"));
                res.put("quizType", rs.getString("quiz_type"));
                res.put("startTime", rs.getString("start_time"));

                return res;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        res.put("active", false);
        return res;
    }
    @GetMapping("/tournament/{id}/teams")
    public List<Map<String,Object>> publicTeams(@PathVariable int id) {

        List<Map<String,Object>> list = new ArrayList<>();

        try(Connection conn = DatabaseService.connect()) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id,name,image_url,members FROM tournament_teams WHERE tournament_id=?"
            );

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                Map<String,Object> t = new HashMap<>();

                t.put("id", rs.getInt("id"));
                t.put("name", rs.getString("name"));
                t.put("image", rs.getString("image_url"));
                t.put("members", rs.getInt("members"));

                list.add(t);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    @PostMapping("/team/session")
    public Map<String,Object> joinSession(@RequestBody Map<String,Object> req){

        Map<String,Object> res = new HashMap<>();

        try(Connection conn = DatabaseService.connect()){

            int teamId = (Integer)req.get("teamId");
            String sessionId = (String)req.get("sessionId");

            // provjeri ima li leader
            PreparedStatement check = conn.prepareStatement(
                    "SELECT COUNT(*) as total FROM team_sessions WHERE team_id=? AND leader=true"
            );

            check.setInt(1,teamId);

            ResultSet rs = check.executeQuery();

            boolean leader = false;

            if(rs.next()){
                if(rs.getInt("total") == 0){
                    leader = true;
                }
            }

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO team_sessions(team_id,session_id,leader) VALUES(?,?,?)"
            );

            ps.setInt(1,teamId);
            ps.setString(2,sessionId);
            ps.setBoolean(3,leader);

            ps.executeUpdate();

            res.put("success",true);
            res.put("leader",leader);

        }catch(Exception e){
            e.printStackTrace();
            res.put("success",false);
        }

        return res;
    }
    @GetMapping("/admin/team/{teamId}/players")
    public Map<String,Object> players(@PathVariable int teamId){

        Map<String,Object> res = new HashMap<>();

        try(Connection conn = DatabaseService.connect()){

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) as total FROM team_sessions WHERE team_id=?"
            );

            ps.setInt(1,teamId);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                res.put("players",rs.getInt("total"));
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return res;
    }

    @GetMapping("/team/match/{teamId}")
    public Map<String,Object> teamMatch(@PathVariable int teamId){

        Map<String,Object> res = new HashMap<>();

        try(Connection conn = DatabaseService.connect()){

            PreparedStatement ps = conn.prepareStatement(

                    "SELECT m.id,t1.name as team1,t2.name as team2 " +
                            "FROM matches m " +
                            "LEFT JOIN tournament_teams t1 ON m.team1_id=t1.id " +
                            "LEFT JOIN tournament_teams t2 ON m.team2_id=t2.id " +
                            "WHERE m.team1_id=? OR m.team2_id=? LIMIT 1"

            );

            ps.setInt(1,teamId);
            ps.setInt(2,teamId);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                String team1 = rs.getString("team1");
                String team2 = rs.getString("team2");

                if(team1.equals(team1)){

                    res.put("team",team1);
                    res.put("opponent",team2);

                }else{

                    res.put("team",team2);
                    res.put("opponent",team1);

                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return res;
    }

    @PostMapping("/admin/match/start/{matchId}")
    public String startMatch(@PathVariable int matchId){

        try(Connection conn = DatabaseService.connect()){

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE matches SET status='COUNTDOWN', start_time=? WHERE id=?"
            );

            Timestamp start = new Timestamp(System.currentTimeMillis() + 15000);

            ps.setTimestamp(1,start);
            ps.setInt(2,matchId);

            ps.executeUpdate();

            return "started";

        }catch(Exception e){
            e.printStackTrace();
        }

        return "error";
    }

    @GetMapping("/team/match-status/{teamId}")
    public Map<String,Object> matchStatus(@PathVariable int teamId){

        Map<String,Object> res = new HashMap<>();

        try(Connection conn = DatabaseService.connect()){

            PreparedStatement ps = conn.prepareStatement(

                    "SELECT m.id,m.start_time,m.status," +
                            "t1.name as team1,t2.name as team2," +
                            "t1.id as t1id,t2.id as t2id " +
                            "FROM matches m " +
                            "LEFT JOIN tournament_teams t1 ON m.team1_id=t1.id " +
                            "LEFT JOIN tournament_teams t2 ON m.team2_id=t2.id " +
                            "WHERE m.team1_id=? OR m.team2_id=? LIMIT 1"

            );

            ps.setInt(1,teamId);
            ps.setInt(2,teamId);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                res.put("matchId",rs.getInt("id"));
                res.put("status",rs.getString("status"));
                res.put("startTime",rs.getString("start_time"));

                int t1 = rs.getInt("t1id");
                int t2 = rs.getInt("t2id");

                // 🔹 OVO DODAJ
                res.put("t1id", t1);
                res.put("t2id", t2);

                if(teamId == t1){

                    res.put("team",rs.getString("team1"));
                    res.put("opponent",rs.getString("team2"));

                }else{

                    res.put("team",rs.getString("team2"));
                    res.put("opponent",rs.getString("team1"));

                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return res;
    }

    @PostMapping("/admin/start-game1/{matchId}")
    public GameRound startGame(@PathVariable int matchId){

        GameRound g = GameService.generateGame(matchId);

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

        return g;
    }
    
    @GetMapping("/team/game1/state/{matchId}")
    public Map<String,Object> gameState(@PathVariable int matchId){

        Map<String,Object> res = new HashMap<>();

        GameRound g = GameService.activeGames.get(matchId);

        if(g == null){
            return res;
        }

        long now = System.currentTimeMillis();

        // COUNTDOWN -> PLAYING
        if(g.getState().equals("COUNTDOWN")){

            long diff = now - g.getCountdownStart();

            if(diff >= 15000){

                g.setState("PLAYING");
                g.setPlayStart(now);

            }else{

                res.put("state","COUNTDOWN");
                res.put("time",15 - (diff/1000));
                return res;

            }

        }

        // PLAYING -> RESULT
        if(g.getState().equals("PLAYING")){

            long diff = now - g.getPlayStart();

            boolean team1Answered = g.getAnswers().containsKey(g.getTeam1Id());
            boolean team2Answered = g.getAnswers().containsKey(g.getTeam2Id());

            if(diff >= 60000 || (team1Answered && team2Answered)){

                g.setState("RESULT");
                g.setResultStart(now);

                Map<String,Object> result = GameService.calculateResult(g);

                g.setResult(result);

            }else{

                res.put("state","PLAYING");
                res.put("time",60 - (diff/1000));
                res.put("letters",g.getLetters());

                return res;

            }

        }

        // RESULT -> FINISHED
        if(g.getState().equals("RESULT")){

            long diff = now - g.getResultStart();

            if(diff >= 6000){

                g.setState("FINISHED");

            }else{

                res.put("state","RESULT");
                res.put("time",6 - (diff/1000));
                res.put("result",g.getResult());

                return res;

            }

        }

        if(g.getState().equals("FINISHED")){

            res.put("state","FINISHED");

        }

        return res;
    }

    @GetMapping("/team/game1/{matchId}")
    public GameRound getGame(@PathVariable int matchId){

        return GameService.activeGames.computeIfAbsent(
                matchId,
                id -> GameService.generateGame(id)
        );

    }
    @PostMapping("/team/game1/answer")
    public String answer(@RequestBody Map<String,Object> req){

        int matchId = (Integer)req.get("matchId");
        int teamId = (Integer)req.get("teamId");
        String word = ((String)req.get("word")).toUpperCase();

        GameRound g = GameService.activeGames.get(matchId);
        System.out.println("MATCH ID: " + matchId);
        System.out.println("GAME EXISTS: " + (g != null));
        System.out.println("TEAM ID FROM FRONT: " + teamId);
        System.out.println("TEAM1 ID: " + g.getTeam1Id());
        System.out.println("TEAM2 ID: " + g.getTeam2Id());
        if(g != null){
            g.getAnswers().put(teamId,word);
        }

        return "ok";
    }

    @GetMapping("/team/game1/result/{matchId}")
    public Map<String,Object> result(@PathVariable int matchId) throws InterruptedException {

        GameRound g = GameService.activeGames.get(matchId);

        if(g == null){
            return new HashMap<>();
        }

        long now = System.currentTimeMillis();

        long diff = now - g.startTime;

        if(diff < 60000){

            long wait = 60000 - diff;

            Thread.sleep(wait);

        }

        return GameService.calculateResult(g);
    }
}