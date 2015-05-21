package com.oleksiykovtun.allted.soc.services;

import com.oleksiykovtun.allted.soc.base.*;
import com.oleksiykovtun.allted.soc.base.ResultParser;

import javax.ws.rs.*;
import java.util.Arrays;
import java.util.Map;

/**
 * ALLTED optimization REST service.
 */
@Path("/optimization/")
public class OptimizationService extends Service {

    private static String testTask1 = "{\n" +
            "    \"function\":\"f(x1,x2) = (x1 - 3)^2 + (x2 + 4)^2\", \n" +
            "    \"lowPoint\": [-30, -30], \n" +
            "    \"highPoint\": [30, 30], \n" +
            "    \"startPoint\": [-30, -30]\n" +
            "}";
    private static String testTask2 = "{\n" +
            "    \"function\":\"f(x1,x2) = x1^2 + x2^2 - 10 * cos(2 * pi * x1) - 10 * cos(2 * pi * x2) + 20\", \n" +
            "    \"lowPoint\": [-10, -10], \n" +
            "    \"highPoint\": [10, 10], \n" +
            "    \"startPoint\": [9, 10]\n" +
            "}";

    public static String DESCRIPTION = "The service for analytic functions minimization.\n\n"
            + "Example tasks JSON:\n\n" + testTask1 + "\n\n" + testTask2 + "\n";

    class OptimizationTask extends Task {

        public String function;
        public double[] lowPoint;
        public double[] highPoint;
        public double[] startPoint;

        @Override
        public String generateAlltedTask() {
            String alltedTask = function.replaceAll(" ", "").replace("=", ";") + ";";
            alltedTask += "#" + Arrays.toString(lowPoint).replaceAll("[ \\[\\]]", "");
            alltedTask += "#" + Arrays.toString(highPoint).replaceAll("[ \\[\\]]", "");
            alltedTask += "#" + Arrays.toString(startPoint).replaceAll("[ \\[\\]]", "");
            return alltedTask;
        }
    }

    class OptimizationResult extends Result {

        public double optimalValue;
        public Map optimalPoint;

        @Override
        public void parseAlltedResult(String raw, String... plotRawData) {
            optimalValue = new ResultParser(raw).cutBefore("optimal", "point").cutBeforeAndIncluding("=").getDouble();
            optimalPoint = new ResultParser(raw).cutBefore("optimal", "point").cutBeforeAndIncluding("parameters")
                    .cutAfterAndIncluding("S").cutLast().getRegular(3, 0, 2).toStringDoubleMap();
        }

    }

}
