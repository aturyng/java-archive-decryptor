package com.underground.extractor;



import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    @Data
    @AllArgsConstructor
    public static class Pair<T,V>{
        T key;
        V value;
    }

    public static boolean isMultipartArchive(String fileName) {
        return Pattern.compile(".+.((part[0-9]+.rar)|((7z|7zip).[0-9]+))$").matcher(fileName).matches();
    }

    public static boolean isFirstMultipartArchive(String fileName) {
        return Pattern.compile(".+.((part[0-9]*1.rar)|((7z|7zip).[0-9]*1))$").matcher(fileName).matches();
    }

    public static boolean isSinglepartArchive(String filename) {
        if (isMultipartArchive(filename)) {
            return false;
        }
        return Pattern.compile(".+.(7z|7zip|zip|rar)$").matcher(filename).matches();
    }

    public static Pair<Integer, Integer> getIndexesForCounterOfMultipartFilename(String filename) {
        //remove the rest
        Pattern patternExtensionWithCounter = Pattern.compile("\\d+(?!.*\\d)");
        Matcher matcher = patternExtensionWithCounter.matcher(filename);
        if (matcher.find()) {
            return new Pair<>(matcher.start(), matcher.end());
        }
        return null;
    }

    public static String getNextMultipartByIncrementingCounter(String fileName) {
        var indexes = Utils.getIndexesForCounterOfMultipartFilename(fileName);
        if (indexes != null) {
            String counter = fileName.substring(indexes.getKey(), indexes.getValue());
            //string leading zeros
            var strippedCounter = counter.replaceAll("^0+", "");
            Integer incrementedCounter = Integer.parseInt(strippedCounter) + 1;

            int n = counter.length() - strippedCounter.length(); // Number of zeros to insert

            String incrementedCounterAsString = String.valueOf(incrementedCounter);
            String incrementedCounterWithLeadingZeros;
            if (n > 0) {
                // Append the desired number of zeros
                incrementedCounterWithLeadingZeros = "0".repeat(n) + incrementedCounterAsString;
            } else {
                incrementedCounterWithLeadingZeros = incrementedCounterAsString;
            }
            //replace with incremented counter & return
            return new StringBuilder(fileName)
                    .replace(indexes.getKey(), indexes.getValue(), incrementedCounterWithLeadingZeros)
                    .toString();
        }
        return null;
    }
}
