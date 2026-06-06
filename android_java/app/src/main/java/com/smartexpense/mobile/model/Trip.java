package com.smartexpense.mobile.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Trip {
    @SerializedName("_id")
    private String mongoId;

    private String id;
    private String name;
    private String createdBy;
    private Map<String, Double> members;

    public Trip() {}

    public Trip(String id, String name, String createdBy, Map<String, Double> members) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.members = members;
    }

    public String getId() {
        if (id != null) return id;
        return mongoId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Map<String, Double> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Double> members) {
        this.members = members;
    }

    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }
}
