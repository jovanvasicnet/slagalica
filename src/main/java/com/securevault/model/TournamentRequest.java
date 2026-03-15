package com.securevault.model;

public class TournamentRequest {

    private String name;
    private String quizType;
    private String location;
    private String barName;
    private String startTime;
    private String plannedStart;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQuizType() { return quizType; }
    public void setQuizType(String quizType) { this.quizType = quizType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBarName() { return barName; }
    public void setBarName(String barName) { this.barName = barName; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getPlannedStart() { return plannedStart; }
    public void setPlannedStart(String plannedStart) { this.plannedStart = plannedStart; }
}
