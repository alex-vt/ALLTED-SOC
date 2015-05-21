package com.oleksiykovtun.allted.soc.services;

import com.oleksiykovtun.allted.soc.base.*;

import javax.ws.rs.*;

/**
 * ALLTED raw (.atd) task execution REST service.
 */
@Path("/raw/")
public class RawService extends Service {

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

    public static String DESCRIPTION = "The service for arbitrary ALLTED .atd tasks execution.\n\n"
            + "Example task JSON:\n\n" + testTask1 + "\n";


    class RawTask extends Task {

        public String raw;

        @Override
        public String generateAlltedTask() {
            return raw;
        }

    }

    class RawResult extends Result {

        @Override
        public void parseAlltedResult(String raw, String... plotRawData) {
            // raw result is already a base class field
        }
    }

}
