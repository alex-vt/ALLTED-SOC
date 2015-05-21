package com.oleksiykovtun.allted.soc.services;

import com.google.gson.internal.LinkedTreeMap;
import com.oleksiykovtun.allted.soc.base.Result;
import com.oleksiykovtun.allted.soc.base.Task;
import com.oleksiykovtun.allted.soc.base.TaskPath;

import javax.ws.rs.Path;
import java.util.Map;

/**
 * ALLTED time analysis REST service.
 */
@Path("/time/")
public class TimeService extends Service {

    private static String testTask1 = "{\n" +
            "    \"elements\":{\n" +
            "        \"C1\":{\"from\":1,\"to\":2,\"value\":0.01},\n" +
            "        \"L2\":{\"from\":2,\"to\":3,\"value\":0.8},\n" +
            "        \"R3\":{\"from\":2,\"to\":0,\"value\":5},\n" +
            "        \"R4\":{\"from\":3,\"to\":0,\"value\":5},\n" +
            "        \"E1\":{\"from\":1,\"to\":0,\"value\":{\n" +
            "            \"pulse\":{\"lowValue\":0.1,\"highValue\":5,\"delay\":1,\"riseTime\":1,\"fallTime\":1,\"peakTime\":2,\"period\":7}}\n" +
            "        }\n" +
            "    },\n" +
            "    \"maxTime\":10,\n" +
            "    \"plotVoltageAtNodes\":[1,2],\n" +
            "    \"plotCurrentAtElements\":[\"C1\",\"L2\"]\n" +
            "}";

    private static String testTask2 = "{\n" +
            "    \"elements\":{\n" +
            "        \"C1\":{\"from\":1,\"to\":2,\"value\":0.01},\n" +
            "        \"L2\":{\"from\":2,\"to\":3,\"value\":0.8},\n" +
            "        \"R3\":{\"from\":2,\"to\":0,\"value\":5},\n" +
            "        \"R4\":{\"from\":3,\"to\":0,\"value\":5},\n" +
            "        \"E1\":{\"from\":1,\"to\":0,\"value\":{\n" +
            "            \"exponential\":{\"lowValue\":0.1,\"highValue\":5,\"riseDelay\":1,\"riseTimeConstant\":1,\"fallDelay\":3,\"fallTimeConstant\":2}}\n" +
            "        }\n" +
            "    },\n" +
            "    \"maxTime\":10,\n" +
            "    \"plotVoltageAtNodes\":[1,2],\n" +
            "    \"plotCurrentAtElements\":[\"C1\",\"L2\"]\n" +
            "}";

    public static String DESCRIPTION = "The service for electronic circuits time analysis with output graph plotting.\n\n"
            + "Example tasks JSON:\n\n" + testTask1 + "\n\n" + testTask2 + "\n";


    class TimeTask extends Task {

        public Map<String, Map<String, Object>> elements;
        public double maxTime;
        public int[] plotVoltageAtNodes;
        public String[] plotCurrentAtElements;

        @Override
        public String generateAlltedTask() {
            return "Object\n" + "search ALLTED;\n" + "circuit GENERATED;\n" +
                    getElements() +
                    "&&\n" + "task\n" + "tr;\n" + "dc;\n" +
                    "const tmax=" + maxTime + ";\n" +
                    getNodesToPlotVoltageAt() + getElementsToPlotCurrentAt() +
                    "&&\n" + "END\n";
        }

        private String getElements() {
            String elementsText = "";
            for (Map.Entry<String, Map<String, Object>> element : elements.entrySet()) {
                elementsText += element.getKey()
                        + "(" + ((Double)(element.getValue().get("from"))).intValue()
                        + "," + ((Double)(element.getValue().get("to"))).intValue() + ")="
                        + getValue(element.getValue().get("value")) + ";\n";
            }
            return elementsText;
        }

        private String getValue(Object value) {
            if (value instanceof Double) {
                return "" + value;
            } else {
                String functionName = ((Map) value).keySet().toArray()[0].toString();
                switch (functionName) {
                    case "pulse":
                        functionName = "FPULSE";
                        break;
                    case "exponential":
                        functionName = "FEXP";
                        break;
                }
                String functionParameters = "" + ((Map)((Map) value).values().toArray()[0]).values();

                return functionName + functionParameters.replace("[", "(").replace("]", ")");
            }
        }

        private String getNodesToPlotVoltageAt() {
            String text = "";
            for (int nodeNumber : plotVoltageAtNodes) {
                text += "plot V" + nodeNumber + ";\n";
            }
            return text;
        }

        private String getElementsToPlotCurrentAt() {
            String text = "";
            for (String element : plotCurrentAtElements) {
                text += "plot i" + element + ";\n";
            }
            return text;
        }

    }

    class TimeResult extends Result {

        public Map plot;

        @Override
        public void parseAlltedResult(String raw, String... plotRawData) {
            plot = getPlots(plotRawData, getTask().getLink());
        }

        private Map getPlots(String[] plotRawData, String resultLink) {
            Map<String, Map> plot = new LinkedTreeMap<>();
            int graphCount = getGraphVariantPoints(plotRawData).length;
            for (int graph = 0; graph < graphCount; ++graph) {
                Map<String, Object> graphMap = new LinkedTreeMap<>();
                graphMap.put("graphLink", resultLink + TaskPath.ATTACHMENT + graph + ".svg");
                plot.put(getGraphCoordinateNames(plotRawData)[graph][1], graphMap);
                graphMap.put("plotRange", getPlotRangeMap(plotRawData, graph));
            }
            return plot;
        }

    }

}
