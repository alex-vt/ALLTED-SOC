package com.oleksiykovtun.allted.soc.services;

import com.google.gson.internal.LinkedTreeMap;
import com.oleksiykovtun.allted.soc.base.Result;
import com.oleksiykovtun.allted.soc.base.Task;
import com.oleksiykovtun.allted.soc.base.TaskPath;

import javax.ws.rs.Path;
import java.util.Map;

/**
 * ALLTED frequency-domain analysis REST service.
 */
@Path("/ac/")
public class AcService extends Service {

    private static String testTask1 = "{\n" +
            "    \"elements\":{\n" +
            "        \"C1\":{\"from\":1,\"to\":2,\"value\":0.001},\n" +
            "        \"L2\":{\"from\":2,\"to\":3,\"value\":0.05},\n" +
            "        \"R3\":{\"from\":2,\"to\":0,\"value\":5},\n" +
            "        \"L4\":{\"from\":3,\"to\":0,\"value\":0.5},\n" +
            "        \"E1\":{\"from\":1,\"to\":0,\"value\":1}\n" +
            "    },\n" +
            "    \"transferFunction\":{\n" +
            "        \"numerator\":{\"variableType\":\"current\",\"variableName\":\"C1\"},\n" +
            "        \"denominator\":{\"variableType\":\"voltage\",\"variableName\":\"L2\"}\n" +
            "    },\n" +
            "    \"minFrequency\":0.1,\n" +
            "    \"maxFrequency\":30,\n" +
            "    \"plotResponses\":[\"amplitude\",\"phase\"]\n" +
            "}";

    public static String DESCRIPTION = "The service for electronic circuits frequency-domain analysis.\n\n"
            + "Example task JSON:\n\n" + testTask1 + "\n";


    class AcTask extends Task {

        public Map<String, Map<String, Double>> elements;
        public Map<String, Map<String, String>> transferFunction;
        public double minFrequency;
        public double maxFrequency;
        public String[] plotResponses;

        @Override
        public String generateAlltedTask() {
            return "Object\n" + "search ALLTED;\n" + "circuit GENERATED;\n" +
                    getElements() +
                    "&&\n" + "task\n" + "dc;\n" + "ac;\n" +
                    getTransferFunction() +
                    "const Lfreq = " + minFrequency + ", Ufreq = " + maxFrequency + ";\n" +
                    getResponsesToPlot() +
                    "&&\n" + "END\n";
        }

        private String getElements() {
            String elementsText = "";
            for (Map.Entry<String, Map<String, Double>> element : elements.entrySet()) {
                elementsText += element.getKey()
                        + "(" + element.getValue().get("from").intValue()
                        + "," + element.getValue().get("to").intValue() + ")="
                        + element.getValue().get("value") + ";\n";
            }
            return elementsText;
        }

        private String getTransferFunction() {
            String numeratorType = getVariableType(transferFunction.get("numerator").get("variableType"));
            String numeratorVariable = transferFunction.get("numerator").get("variableName");
            String denominatorType = getVariableType(transferFunction.get("denominator").get("variableType"));
            String denominatorVariable = transferFunction.get("denominator").get("variableName");
            return "TF Ku = " + numeratorType + numeratorVariable + " / " + denominatorType + denominatorVariable + ";\n";
        }

        private String getVariableType(String type) {
            switch (type) {
                case "current":
                    return "I";
                case "voltage":
                    return "U";
                case "nodeVoltage":
                    return "V";
            }
            return type;
        }

        private String getResponsesToPlot() {
            String text = "";
            for (String element : plotResponses) {
                switch (element) {
                    case "real":
                        element = "RE";
                        break;
                    case "imaginary":
                        element = "IM";
                        break;
                    case "amplitude":
                        element = "DB";
                        break;
                    case "phase":
                        element = "PH";
                        break;
                }
                text += "plot " + element + ".Ku;\n";
            }
            return text;
        }
    }

    class AcResult extends Result {

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
