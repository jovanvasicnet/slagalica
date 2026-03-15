package com.securevault.controller;

import com.securevault.model.JoinTeamRequest;
import com.securevault.model.LoginRequest;
import com.securevault.model.TeamRequest;
import com.securevault.model.TournamentRequest;
import com.securevault.security.*;
import com.securevault.service.DatabaseService;
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


            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE tournaments SET status='STARTED' WHERE id=?"
            );

            ps.setInt(1, id);

            ps.executeUpdate();

            return "started";

        } catch(Exception e) {
            e.printStackTrace();
        }

        return "error";
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

}