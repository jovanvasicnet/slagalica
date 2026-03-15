package com.securevault.model;

public class JoinTeamRequest {

    private String name;
    private String password;
    private int tournamentId;

    public JoinTeamRequest(String name, String password, int tournamentId) {
        this.name = name;
        this.password = password;
        this.tournamentId = tournamentId;
    }
    public JoinTeamRequest() {

    }
    public int getTournamentId() {
        return tournamentId;
    }
    public void setTournamentId(int tournamentId) {
        this.tournamentId = tournamentId;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
