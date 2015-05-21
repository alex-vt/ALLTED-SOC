package com.oleksiykovtun.allted.soc.services;

import com.google.gson.internal.LinkedTreeMap;
import com.oleksiykovtun.allted.soc.base.*;

import javax.ws.rs.Path;
import java.util.*;

/**
 * ALLTED raw (.atd) task execution REST service with graph plotting support.
 */
@Path("/plot/")
public class PlotService extends Service {

    private static String testTask1 = "{\"raw\":\n" +
            "    \"OBJECT\\n\n" +
            "    CIRCUIT MCCLAB1;\\n\n" +
            "    Ein(0,1) = 1;\\n\n" +
            "    R1(1,2) = 10;\\n\n" +
            "    L1(2,3) = 0.2;\\n\n" +
            "    C1(3,4) = 0.001;\\n\n" +
            "    L2(4,0) = 0.5;\\n\n" +
            "    &\\n\n" +
            "    TASK\\n\n" +
            "    DC;\\n\n" +
            "    AC;\\n\n" +
            "    TF Ku = UL2 / UEin;\\n\n" +
            "    CONST Lfreq = 1, Ufreq = 20;\\n\n" +
            "    PLOT RE.Ku, MA.Ku, DB.Ku, PH.Ku;\\n\n" +
            "    &\\n\n" +
            "    END.\\n\"\n" +
            "}";

    public static String DESCRIPTION = "The service for ALLTED .atd tasks execution and output graph plotting.\n\n"
            + "Example task JSON:\n\n" + testTask1 + "\n";

    class PlotTask extends Task {

        public String raw;

        @Override
        public String generateAlltedTask() {
            return raw;
        }

    }

    class PlotResult extends Result {

        public Map plot;

        @Override
        public void parseAlltedResult(String raw, String... plotRawData) {
            plot = getPlots(plotRawData, getTask().getLink());
        }

        private Map getPlots(String[] plotRawData, String resultLink) {
            Map<String, Map> plot = new LinkedTreeMap<>();
            int graphCount = getGraphVariantPoints(plotRawData).length;
            for (int graph = 0; graph < graphCount; ++graph) {
                Map<String, Map> graphMap = new LinkedTreeMap<>();
                Map<String, String> linkMap = new LinkedTreeMap<>();
                int variantCount = getGraphVariantPoints(plotRawData)[0].length;
                for (int i = 0; i < variantCount; ++i) {
                    String graphVariantName = (variantCount > 1) ? ("variant" + i) : "graph";
                    linkMap.put(graphVariantName, resultLink + TaskPath.ATTACHMENT + (graph * variantCount + i) + ".svg");
                }
                graphMap.put("graphLinks", linkMap);
                graphMap.put("plotRange", getPlotRangeMap(plotRawData, graph));
                plot.put(getGraphCoordinateNames(plotRawData)[graph][1], graphMap);
            }
            return plot;
        }
    }

}
