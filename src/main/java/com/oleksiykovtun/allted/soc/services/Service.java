package com.oleksiykovtun.allted.soc.services;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.oleksiykovtun.allted.soc.base.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Constructor;

/**
 * ALLTED base REST service.
 */
@Path("/")
public class Service {

    @Context
    protected UriInfo uriInfo;

    @Path(TaskPath.SUBMIT) @POST @Consumes(MediaType.JSON) @Produces(MediaType.JSON)
    public Task submit(String incomingTaskJsonString) throws Throwable {
        Task task = getTaskFromJson(incomingTaskJsonString, getTaskTypeFromUri());
        String taskId = task.setup();
        task.setLink(getLink(TaskPath.STATUS, taskId));
        new AlltedTask(taskId).writeMetadata(new Gson().toJson(task));
        new AlltedTask(taskId).startWithPreProcessing(task.generateAlltedTask());
        return task;
    }

    @Path(TaskPath.EXECUTE) @POST @Consumes(MediaType.JSON) @Produces(MediaType.JSON)
    public Result execute(String incomingTaskJsonString) throws Throwable {
        String taskId = submit(incomingTaskJsonString).getId();
        final long maxExecutionTimeMillis = 4000;
        final long executionPollIntervalMillis = 300;
        for (int i = 0; i < maxExecutionTimeMillis; i += executionPollIntervalMillis) {
            if (new AlltedTask(taskId).hasResult()) {
                return getResult(taskId);
            }
            Thread.sleep(executionPollIntervalMillis);
        }
        Task task = getTaskFromJson(new AlltedTask(taskId).getMetadata());
        task.setStatus(Task.EXPIRED);
        new AlltedTask(taskId).writeMetadata(new Gson().toJson(task));
        throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
    }

    @Path(TaskPath.RESULT) @GET @Produces(MediaType.JSON)
    public final Result getResult(@PathParam("id") String taskId) throws Throwable {
        AlltedTask alltedTask = new AlltedTask(taskId);
        Task task = getTaskFromJson(alltedTask.getMetadata());
        task = updateMetadataToFinished(task); // todo check
        Result result = createResultForTask(task);
        result.setRawLink(getLink(TaskPath.RESULT_RAW, taskId));
        result.parseAlltedResult(alltedTask.getResult(), alltedTask.getResultPlotVariableIndices(),
                alltedTask.getResultPlotCount(), alltedTask.getResultPlotDataSizes(),
                alltedTask.getResultPlotVariableNames(), alltedTask.getResultPlotData());
        alltedTask.writeMetadata(new Gson().toJson(task));
        return result;
    }

    @Path(TaskPath.STATUS) @GET @Produces(MediaType.JSON)
    public final Task getStatus(@PathParam("id") String taskId) throws Throwable {
        Task task = getTaskFromJson(new AlltedTask(taskId).getMetadata());
        if (new AlltedTask(taskId).hasResult()) {
            task = updateMetadataToFinished(task);
        }
        return task;
    }

    private Task updateMetadataToFinished(Task task) throws Throwable {
        if (!task.getStatus().equals(Task.FINISHED)) {
            task.setLink(getLink(TaskPath.RESULT, task.getId()));
            task.setStatus(Task.FINISHED);
            task.setExecutionTime(Time.getSeconds(new AlltedTask(task.getId()).getResultModificationTime()
                    - new AlltedTask(task.getId()).getModificationTime()));
            new AlltedTask(task.getId()).writeMetadata(new Gson().toJson(task));
        }
        return task;
    }

    @Path(TaskPath.RESULT_RAW) @GET @Produces(MediaType.TEXT)
     public final String getRawResult(@PathParam("id") String taskId) throws Throwable {
        return new AlltedTask(taskId).getResult();
    }

    @Path(TaskPath.RESULT_ATTACHMENT) @GET @Produces(MediaType.HTML)
    public final String getAttachmentResult(@PathParam("id") String taskId, @PathParam("attachment") String attachmentId,
                                            @PathParam("extension") String attachmentExtension) throws Throwable {
        AlltedTask alltedTask = new AlltedTask(taskId);
        Result result = createResultForTask(getTaskFromJson(alltedTask.getMetadata()));
        return result.generateAttachment(attachmentId, alltedTask.getResultPlotVariableIndices(),
                alltedTask.getResultPlotCount(), alltedTask.getResultPlotDataSizes(),
                alltedTask.getResultPlotVariableNames(), alltedTask.getResultPlotData());
    }

    @Path(TaskPath.CANCEL) @GET @Produces(MediaType.TEXT)
    public final String cancel(@PathParam("id") String taskId) {
        return "todo implement cancel"; // todo
    }

    @Path(TaskPath.DELETE) @GET @Produces(MediaType.TEXT)
    public final String delete(@PathParam("id") String taskId) {
        return "todo implement delete"; // todo
    }

    private String getDefaultDescription() throws Throwable {
        String description = "ALLTED-SOC\n\n"
                + "ALLTED-based Service-Oriented Computing in Cloud with REST API\n\n"
                + "https://github.com/oleksiykovtun/ALLTED-SOC\n\n"
                + "Services:\n";
        for (String serviceLink : ServiceDiscoverer.getServiceLinkList(getUrlTextFromUri())) {
            description += serviceLink + "\n";
        }
        return description;
    }

    private String getCommonUsageNotes() throws Throwable { // todo improve
        return "\n\nUsage Notes:\n\n"
                + "Submit a task (POST; Content-Type: application/json):\n"
                + getUrlTextFromUri() + "/" + TaskPath.SUBMIT + "\n\n"
                + "Execute a task (POST; Content-Type: application/json):\n"
                + getUrlTextFromUri() + "/" + TaskPath.EXECUTE + "\n\n"
                + "Status of the task with {id} (GET):\n"
                + getUrlTextFromUri().replace(getTaskTypeFromUri().toLowerCase(), TaskPath.STATUS) + "\n\n"
                + "Result of execution of the task with {id} (GET):\n"
                + getUrlTextFromUri().replace(getTaskTypeFromUri().toLowerCase(), TaskPath.RESULT) + "\n\n\n"
                + "REST Client for Chrome:\n"
                + "https://chrome.google.com/webstore/detail/advanced-rest-client/hgmloofddffdnphfgcellkdfbfbjeloo" + "\n\n"
                + "ALLTED-SOC Home Page:\n"
                + getUrlTextFromUri().replace("/" + getTaskTypeFromUri().toLowerCase(), "") + "\n";
    }

    @Path(TaskPath.DESCRIPTION) @GET @Produces(MediaType.TEXT)
    public String getDescription() throws Throwable {
        String taskType = getTaskTypeFromUri();
        if (taskType.isEmpty()) {
            return getDefaultDescription();
        } else {
            // todo invoke method instead
            return Class.forName(getServiceFullClassName(taskType)).getField("DESCRIPTION").get(null)
                    + getCommonUsageNotes();
        }
    }


    protected Task getTaskFromJson(String taskJsonString, String taskType) throws Throwable {
        taskType = toSentenceCase(taskType);
        Task task = (Task) new Gson().fromJson(taskJsonString,
                Class.forName(getServiceFullClassName(taskType) + "$" + taskType + "Task"));
        task.setType(taskType.toUpperCase());
        return task;
    }

    protected Task getTaskFromJson(String taskJsonString) throws Throwable {
        String taskType = (String) ((LinkedTreeMap) new Gson().fromJson(taskJsonString, Object.class)).get("type");
        return getTaskFromJson(taskJsonString, taskType);
    }

    private String getLink(String taskPath, String taskId) {
        return uriInfo.getAbsolutePath().getScheme() + "://" + uriInfo.getAbsolutePath().getHost()
                + "/" + taskPath.replace(TaskPath.TASK_ID, taskId);
    }

    private String getTaskTypeFromUri() {
        String[] pathParts = uriInfo.getAbsolutePath().getPath().split("/");
        return toSentenceCase(pathParts.length > 0 ? pathParts[1] : "");
    }

    private String getUrlTextFromUri() {
        return uriInfo.getAbsolutePath().toString();
    }

    private String toSentenceCase(String text) {
        return text.isEmpty() ? "" : (text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase());
    }

    private Result createResultForTask(Task task) throws Throwable {
        String taskType = toSentenceCase(task.getType());
        Class<?> serviceClass = Class.forName(getServiceFullClassName(taskType));
        Object serviceInstance = serviceClass.newInstance();
        Class<?> resultClass = Class.forName(getServiceFullClassName(taskType) + "$" + taskType + "Result");
        Constructor<?> resultConstructor = resultClass.getDeclaredConstructor(serviceClass);

        Result result = (Result) resultConstructor.newInstance(serviceInstance);
        result.setTask(task);
        return result;
    }

    private String getServiceFullClassName(String taskType) {
        return "com.oleksiykovtun.allted.soc.services." + toSentenceCase(taskType) + "Service";
    }

}
