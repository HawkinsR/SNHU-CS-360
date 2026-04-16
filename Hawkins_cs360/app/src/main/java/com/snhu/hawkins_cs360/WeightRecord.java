package com.snhu.hawkins_cs360;

public class WeightRecord {
    private long id;
    private long userId;
    private String date;
    private float weight;

    public WeightRecord(long id, long userId, String date, float weight) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.weight = weight;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getDate() { return date; }
    public float getWeight() { return weight; }
}
