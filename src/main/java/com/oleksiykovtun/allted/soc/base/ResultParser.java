package com.oleksiykovtun.allted.soc.base;

import java.util.*;

/**
 * ALLTED .ato and .out result files content parser
 */
public class ResultParser {

    private List<String> tokens;

    public ResultParser(String taskText) {
        tokens = new LinkedList<>(Arrays.asList(taskText.trim().split("\\s+")));
    }

    public ResultParser cropToRange(int startIndexIncluding, int finishIndexNonIncluding) {
        while (tokens.size() > finishIndexNonIncluding) {
            tokens.remove(finishIndexNonIncluding);
        }
        while (tokens.size() > finishIndexNonIncluding - startIndexIncluding) {
            tokens.remove(0);
        }
        return this;
    }

    public String getString() {
        return Arrays.toString(toStringArray()).replaceAll("[\\[, \\]]","");
    }

    public ResultParser cutBefore(String token) {
        while (!tokens.isEmpty() && !tokens.get(0).equals(token)) {
            tokens.remove(0);
        }
        return this;
    }

    public ResultParser cutBefore(String token1, String token2) {
        while (!tokens.isEmpty() && !(tokens.get(0).equals(token1) && tokens.get(1).equals(token2))) {
            tokens.remove(0);
        }
        return this;
    }

    public ResultParser cutBeforeLast(String token) {
        for (int i = 0; i < tokens.size(); ++i) {
            if (tokens.get(i).equals(token)) {
                cutFirst();
                cutBeforeLast(token);
            }
        }
        return this;
    }

    public ResultParser cutAfterAndIncluding(String token) {
        int tokenPosition = 0;
        while (tokenPosition < tokens.size()) {
            if (tokens.get(tokenPosition).equals(token)) {
                while (tokenPosition < tokens.size()) {
                    tokens.remove(tokenPosition);
                }
            }
            ++tokenPosition;
        }
        return this;
    }

    public ResultParser cutFirst() {
        if (!tokens.isEmpty()) {
            tokens.remove(0);
        }
        return this;
    }

    public ResultParser cutLast() {
        if (!tokens.isEmpty()) {
            tokens.remove(tokens.size() - 1);
        }
        return this;
    }

    public ResultParser cutBeforeAndIncluding(String token) {
        return cutBefore(token).cutFirst();
    }

    public ResultParser getFirst() {
        while(tokens.size() > 1) {
            tokens.remove(1);
        }
        return this;
    }

    public int getIntAbsolute(int i) {
        return Math.abs(getInt(i));
    }

    public int size() {
        return tokens.size();
    }

    public int getInt(int i) {
        return Integer.parseInt(tokens.get(i));
    }

    public double getDouble(int i) {
        return Double.parseDouble(tokens.get(i));
    }

    public double[] toDoubleArray() {
        double[] valueArray = new double[size()];
        for (int i = 0; i < size(); ++i) {
            valueArray[i] = getDouble(i);
        }
        return valueArray;
    }

    public double getDouble() {
        return getDouble(0);
    }

    public String[] toStringArray() {
        return tokens.toArray(new String[tokens.size()]);
    }

    public Map<String, Double> toStringDoubleMap() {
        Map<String, Double> map = new TreeMap<>();
        for (int i = 0; i < tokens.size(); i += 2) {
            map.put(tokens.get(i), Double.parseDouble(tokens.get(i + 1)));
        }
        return map;
    }

    public ResultParser getRegular(int width, Integer... offsets) {
        List<String> regularTokens = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i += width) {
            for (int currentOffset = 0; currentOffset < width; ++ currentOffset) {
                if (Arrays.asList(offsets).contains(currentOffset)) {
                    regularTokens.add(tokens.get(i + currentOffset));
                }
            }
        }
        tokens = regularTokens;
        return this;
    }

}
