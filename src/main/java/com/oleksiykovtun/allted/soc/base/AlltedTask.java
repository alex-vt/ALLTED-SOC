package com.oleksiykovtun.allted.soc.base;

import org.apache.commons.io.FileUtils;

import java.io.File;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

/**
 * Representation of ALLTED task in the filesystem and its lifecycle
 */
public class AlltedTask {

    private static final String ALLTED_HOME = "/usr/local/share/allted/";
    private static final String BIN_PATH = ALLTED_HOME + "bin/";
    private static final String TASKS_PATH = ALLTED_HOME + "tasks/";

    private static final String TASK_METADATA = "task.json";
    private static final String TASK_FILE = "task.atd";
    private static final String RESULT_FILE = "result.ato";
    private static final String PRE_PROCESSING_FILE_NAME = "preprocessing.txt";
    private static final String POST_PROCESSING_FILE_NAME = "funcrpn.rpn";

    private static final String INPUT_KEY = "-if";
    private static final String RPN_KEY = "-rpn";
    private static final String ATD_KEY = "-atd";
    private static final String VECTORS_KEY = "-p";

    private static final String ALLTED_EXECUTABLE = "./run-task.sh";
    private static final String PRE_PROCESSOR_EXECUTABLE = "./createrpn";

    private static final String ALLTED_TASK_KEYWORD = "object";

    private String id;
    private String taskFolderPath;

    public AlltedTask(String id) {
        this.id = id;
        this.taskFolderPath = TASKS_PATH + id;
    }

    public void start(String taskFileContent) throws Throwable {
        provideDirectory(taskFolderPath);
        FileUtils.writeStringToFile(new File(taskFolderPath, TASK_FILE), taskFileContent);
        new ProcessBuilder(ALLTED_EXECUTABLE, id).directory(new File(BIN_PATH)).start();
    }

    public void startWithPreProcessing(String preProcessingFileContent) throws Throwable {
        String taskFileContent;
        if (preProcessingFileContent.toLowerCase().contains(ALLTED_TASK_KEYWORD)) {
            taskFileContent = preProcessingFileContent;
        } else {
            provideDirectory(taskFolderPath);
            FileUtils.writeStringToFile(new File(taskFolderPath, PRE_PROCESSING_FILE_NAME), preProcessingFileContent);
            new ProcessBuilder(PRE_PROCESSOR_EXECUTABLE,
                    INPUT_KEY, taskFolderPath + "/" + PRE_PROCESSING_FILE_NAME,
                    RPN_KEY, ALLTED_HOME + POST_PROCESSING_FILE_NAME,
                    ATD_KEY, taskFolderPath + "/" + TASK_FILE,
                    VECTORS_KEY).directory(new File(ALLTED_HOME)).start(); // todo replace with shell script
            Thread.sleep(500);
            taskFileContent = getFileContent(taskFolderPath + "/" + TASK_FILE);
        }
        start(taskFileContent);
    }

    public void writeMetadata(String metadata) throws Throwable {
        provideDirectory(taskFolderPath);
        FileUtils.writeStringToFile(new File(taskFolderPath, TASK_METADATA), metadata);
    }

    public boolean hasResult() {
        return new File(taskFolderPath, RESULT_FILE).exists()
                && new File(taskFolderPath, "tab.out").exists();
    }

    public String getResult() throws Throwable {
        return getFileContent(taskFolderPath + "/" + RESULT_FILE);
    }

    public String getResultPlotVariableIndices() throws Throwable {
        return getFileContent(taskFolderPath + "/" + "inprn.out");
    }

    public String getResultPlotCount() throws Throwable {
        return getFileContent(taskFolderPath + "/" + "iplot.out");
    }

    public String getResultPlotDataSizes() throws Throwable {
        return getFileContent(taskFolderPath + "/" + "iptab1.out");
    }

    public String getResultPlotVariableNames() throws Throwable {
        return getFileContent(taskFolderPath + "/" + "nprn.out");
    }

    public String getResultPlotData() throws Throwable {
        return getFileContent(taskFolderPath + "/" + "tab.out");
    }

    public long getModificationTime() throws Throwable {
        return new File(taskFolderPath, TASK_FILE).lastModified();
    }

    public long getResultModificationTime() throws Throwable {
        return new File(taskFolderPath, RESULT_FILE).lastModified();
    }

    public String getMetadata() throws Throwable {
        return getFileContent(taskFolderPath + "/" + TASK_METADATA);
    }

    private String getFileContent(String filePath) throws Throwable {
        return new String(readAllBytes(get(filePath)));
    }

    private void provideDirectory(String directoryPath) throws Throwable {
        if (! new File(directoryPath).exists()) {
            if (!new File(directoryPath).mkdirs()) {
                throw new Exception("Cannot create directory " + directoryPath);
            }
        }
    }
}
