package com.oleksiykovtun.allted.soc.base;

import java.util.Random;

/**
 * Base structured ALLTED task based on service consumer-provided data and used to create ALLTED task files content
 */
public abstract class Task {

    public static transient final String RUNNING = "Running";
    public static transient final String FINISHED = "Finished";
    public static transient final String ABORTED = "Aborted";
    public static transient final String EXPIRED = "Expired";
    public static transient final String CANCELLED = "Cancelled";

    protected String type;
    protected String submitTime;
    protected String executionTime;
    protected String id;
    protected String status;
    protected String link;

    public Task() {}

    public String setup() { // todo set final
        long currentTime = System.currentTimeMillis();
        setSubmitTime(Time.getFormatted(currentTime));
        setId(currentTime + "_" + Math.abs(new Random().nextInt()));
        setStatus(RUNNING);
        generateAlltedTask();
        return getId();
    }

    public abstract String generateAlltedTask();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(String submitTime) {
        this.submitTime = submitTime;
    }

    public String getExecutionTime() {
        return (executionTime == null) ? "-" : executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
