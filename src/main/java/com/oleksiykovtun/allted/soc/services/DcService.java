package com.oleksiykovtun.allted.soc.services;

import com.oleksiykovtun.allted.soc.base.Result;
import com.oleksiykovtun.allted.soc.base.ResultParser;
import com.oleksiykovtun.allted.soc.base.Task;

import javax.ws.rs.Path;
import java.util.Map;

/**
 * ALLTED DC analysis REST service.
 */
@Path("/dc/")
public class DcService extends Service {

    private static String testTask1 = "{\n" +
            "    \"elements\":{\n" +
            "        \"R1\":{\"from\":1,\"to\":2,\"value\":5},\n" +
            "        \"R2\":{\"from\":2,\"to\":3,\"value\":5},\n" +
            "        \"R3\":{\"from\":2,\"to\":0,\"value\":5},\n" +
            "        \"R4\":{\"from\":3,\"to\":0,\"value\":5},\n" +
            "        \"E1\":{\"from\":1,\"to\":0,\"value\":5}\n" +
            "    },\n" +
            "    \"variableParameters\":{\n" +
            "        \"R1=R4\":{\"min\":0.1,\"max\":10},\n" +
            "        \"R3\":{\"min\":0.1,\"max\":10}\n" +
            "    },\n" +
            "    \"targetNode\":2,\n" +
            "    \"targetNodeVoltage\":2.7,\n" +
            "    \"optimizationMethod\":120,\n" +
            "    \"operationError\":0.001\n" +
            "}";

    public static String DESCRIPTION = "The service for DC electronic circuits analysis.\n\n"
            + "Example task JSON:\n\n" + testTask1 + "\n";


    class DcTask extends Task {

        public Map<String, Map<String, Double>> elements;
        public Map<String, Map<String, Double>> variableParameters;
        public int targetNode;
        public double targetNodeVoltage;
        public int optimizationMethod;
        public double operationError;

        @Override
        public String generateAlltedTask() {
            return "Object\n" + "search ALLTED;\n" + "circuit GENERATED;\n" +
                    getElements() +
                    "&&\n" + "task\n" + "dc;\n" + "optim;\n" +
                    getVariableParameters() +
                    "fix f=fixa(v" + targetNode + ",0);\n" +
                    "of tt1=f2(" + "" + targetNodeVoltage + "" + "/f);\n" +
                    "const method=" + optimizationMethod + ";\n" +
                    "option 48;\n" +
                    "const operr = " + operationError + ";\n" +
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

        private String getVariableParameters() {
            String variableParametersText = "";
            for (Map.Entry<String, Map<String, Double>> element : variableParameters.entrySet()) {
                variableParametersText += "varpar " + element.getKey()
                        + "(" + element.getValue().get("min")
                        + "," + element.getValue().get("max") + ");\n";
            }
            return variableParametersText;
        }
    }

    class DcResult extends Result {

        public double solutionError;
        public Map optimalPoint;

        @Override
        public void parseAlltedResult(String raw, String... plotRawData) {
            solutionError = new ResultParser(raw).cutBeforeLast("point").cutBeforeAndIncluding("=")
                    .getFirst().getDouble();
            optimalPoint = new ResultParser(raw).cutBeforeLast("point").cutBeforeAndIncluding("parameters")
                    .cutAfterAndIncluding("S").cutLast().getRegular(3, 0, 2).toStringDoubleMap();
            // todo regular with width 5 for method 40
        }

    }

}
