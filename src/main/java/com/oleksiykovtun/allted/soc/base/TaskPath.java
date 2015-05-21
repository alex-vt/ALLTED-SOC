package com.oleksiykovtun.allted.soc.base;

/**
 * Service method URL paths for managing structured and raw ALLTED tasks and results
 */
public class TaskPath {

    public static final String DESCRIPTION = "";
    public static final String TASK_ID = "{id}";
    public static final String ATTACHMENT_ID = "{attachment}";
    public static final String ATTACHMENT_EXTENSION = "{extension}";

    public static final String SUBMIT = "submit";
    public static final String EXECUTE = "execute";
    public static final String STATUS = "status/" + TASK_ID;
    public static final String RESULT = "result/" + TASK_ID;
    public static final String RESULT_RAW = RESULT + "/raw";
    public static final String ATTACHMENT = "/attachment/";
    public static final String RESULT_ATTACHMENT = RESULT + ATTACHMENT + ATTACHMENT_ID + ATTACHMENT_EXTENSION;
    public static final String CANCEL = "cancel/" + TASK_ID;
    public static final String DELETE = "delete/" + TASK_ID;

}
