package com.oleksiykovtun.allted.soc.base;

import com.google.gson.internal.LinkedTreeMap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Base structured ALLTED task execution result based on .ato and .out result files content
 */
public abstract class Result implements Serializable {

    protected Task task;
    protected String rawLink;

    public abstract void parseAlltedResult(String raw, String... plotRawData);

    public String generateAttachment(String attachmentId, String... plotRawData) {
        double[][][][] plotPoints = getGraphVariantPoints(plotRawData);
        int canvasWidth = 800;
        int canvasHeight = 600;
        int variantCount = plotPoints[0].length;
        int graphNumber = Integer.parseInt(attachmentId) / variantCount;
        int variantNumber = Integer.parseInt(attachmentId) % variantCount;
        double[][] rawPoints = plotPoints[graphNumber][variantNumber];
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                "<svg height=" + quote(canvasHeight) + " width=" + quote(canvasWidth) + ">\n" +
                addPolyLine(getScaledPoints(rawPoints, getGraphRanges(plotPoints[graphNumber]), canvasWidth, canvasHeight)) +
                "</svg>\n" +
                "</body>\n" +
                "</html>";
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getRawLink() {
        return rawLink;
    }

    public void setRawLink(String rawLink) {
        this.rawLink = rawLink;
    }


    protected double[][] getScaledPoints(double[][] rawPoints, Double[][] ranges, double canvasWidth, double canvasHeight) {
        double[][] points = new double[rawPoints.length][rawPoints[0].length];
        int low = 0;
        int high = 1;
        int x = 0;
        int y = 1;
        for (int i = 0; i < points.length; ++i) {
            points[i][x] = canvasWidth * (rawPoints[i][x] - ranges[x][low]) / (ranges[x][high] - ranges[x][low]);
            points[i][y] = canvasHeight * (1 - (rawPoints[i][y] - ranges[y][low]) / (ranges[y][high] - ranges[y][low]));
        }
        return points;
    }

    protected String quote(double number) {
        return "\"" + number + "\"";
    }

    protected String addLine(double[] point1, double[] point2) {
        return "<line x1=" + quote(point1[0]) + " y1=" + quote(point1[1]) +
                " x2=" + quote(point2[0]) + " y2=" + quote(point2[1]) +
                " style=\"stroke:rgb(0,0,0);stroke-width:2\" />\n";
    }

    protected String addPolyLine(double[][] points) {
        String returnString = "";
        for (int i = 0; i < points.length - 1; ++i) {
            returnString += addLine(points[i], points[i + 1]);
        }
        return returnString;
    }

    protected Map<String, List<Double>> getPlotRangeMap(String[] plotData, int graphNumber) {
        Map<String, List<Double>> plotRangeMap = new LinkedTreeMap<>();
        Double[][] graphRanges = getGraphRanges(getGraphVariantPoints(plotData)[graphNumber]);
        int x = 0;
        int y = 1;
        plotRangeMap.put(getGraphCoordinateNames(plotData)[graphNumber][x], Arrays.asList(graphRanges[x]));
        plotRangeMap.put(getGraphCoordinateNames(plotData)[graphNumber][y], Arrays.asList(graphRanges[y]));
        return plotRangeMap;
    }

    protected Double[][] getGraphRanges(double[][][] points) {
        int low = 0;
        int high = 1;
        Double[][] graphRanges = new Double[][] {{Double.MAX_VALUE, -Double.MAX_VALUE},
                {Double.MAX_VALUE, -Double.MAX_VALUE}};
        int variantCount = points.length;
        int pointCount = points[0].length;
        for (int variant = 0; variant < variantCount; ++variant) {
            for (int i = 0; i < pointCount; ++i) {
                for (int coordinate = 0; coordinate < points[variant][0].length; ++coordinate) {
                    if (points[variant][i][coordinate] <= graphRanges[coordinate][low]) {
                        graphRanges[coordinate][low] = points[variant][i][coordinate];
                    }
                    if (points[variant][i][coordinate] >= graphRanges[coordinate][high]) {
                        graphRanges[coordinate][high] = points[variant][i][coordinate];
                    }
                }
            }
        }
        return graphRanges;
    }

    protected int getGraphCount(String[] plotData) {
        return getGraphGroupCount(plotData) * getGraphCurveCount(plotData);
    }

    protected int getGraphGroupCount(String[] plotData) {
        String plotCount = plotData[1];
        return new ResultParser(plotCount).getIntAbsolute(0);
    }

    protected int getGraphCurveCount(String[] plotData) {
        String plotCount = plotData[1];
        // todo check graph curve count
        return new ResultParser(plotCount).getIntAbsolute(1);
    }

    protected double[][][][] getGraphVariantPoints(String[] plotData) {
        double[][] graphDataByVariant = getGraphDataByVariant(plotData);
        int graphCount = getGraphCount(plotData);
        int variantCount = graphDataByVariant.length;
        int variableCount = getVariableNames(plotData).length;
        int variantPointCount = graphDataByVariant[0].length;
        int singleGraphPointCount = variantPointCount / variableCount;
        double[][][][] points = new double[graphCount][variantCount][singleGraphPointCount][2];
        for (int graph = 0; graph < graphCount; ++graph) {
            for (int variant = 0; variant < variantCount; ++variant) {
                for (int point = 0; point < singleGraphPointCount; ++point) {
                    points[graph][variant][point][0] = graphDataByVariant[variant][variableCount * point];
                    points[graph][variant][point][1] = graphDataByVariant[variant][variableCount * point + graph + 1];
                    // todo check variable order
                }
            }
        }
        return points;
    }

    protected double[][] getGraphDataByVariant(String[] plotData) {
        String plotDataSizes = plotData[2];
        String plotDataValues = plotData[4];
        int plotVariantCount = new ResultParser(plotDataSizes).size() - 1;
        int plotVariantDataSetSize = new ResultParser(plotDataSizes).getInt(1) - new ResultParser(plotDataSizes).getInt(0);
        double[][] graphDataByVariant = new double[plotVariantCount][plotVariantDataSetSize]; // todo check equal set size
        for (int i = 0; i < plotVariantCount; ++i) {
            int dataStartIndex = new ResultParser(plotDataSizes).getInt(i) - 1;
            int dataFinishIndex = new ResultParser(plotDataSizes).getInt(i + 1) - 1;
            graphDataByVariant[i] = new ResultParser(plotDataValues).cropToRange(dataStartIndex, dataFinishIndex).toDoubleArray();
        }
        return graphDataByVariant;
    }

    protected String[][] getGraphCoordinateNames(String[] plotData) {
        String plotCount = plotData[1];
        String[][] coordinateNames = new String[getGraphCount(plotData)][2];
        int graphGroupCount = getGraphGroupCount(plotData);
        int graphCurveCount = getGraphCurveCount(plotData);
        for (int i = 0; i < graphGroupCount; ++i) {
            for (int j = 0; j < graphCurveCount; ++j) {
                int graphNumber = graphCurveCount * i + j;
                int x = 0;
                int y = 1;
                int xNamePosition = (3 + graphCurveCount) * i + 3;
                int yNamePosition = (3 + graphCurveCount) * i + 4 + j;
                coordinateNames[graphNumber][x] = getVariableNames(plotData)[new ResultParser(plotCount).getIntAbsolute(xNamePosition) - 1];
                coordinateNames[graphNumber][y] = getVariableNames(plotData)[new ResultParser(plotCount).getIntAbsolute(yNamePosition) - 1];
            }
        }
        return coordinateNames;
    }

    protected String[] getVariableNames(String[] plotData) {
        String plotVariableIndices = plotData[0];
        String plotVariableNames = plotData[3];
        int variableCount = new ResultParser(plotVariableIndices).getInt(0);
        String variableNames[] = new String[variableCount];
        for (int i = 0; i < variableCount; ++i) {
            int nameStartIndex = new ResultParser(plotVariableIndices).getInt(i + 1) - 1;
            int nameFinishIndex = new ResultParser(plotVariableIndices).getInt(i + 2) - 1;
            variableNames[i] = new ResultParser(plotVariableNames).cropToRange(nameStartIndex, nameFinishIndex).getString();
        }
        return variableNames;
    }

}
