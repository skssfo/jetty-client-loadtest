package loadtest.analyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JmeterResultProcessor {

    private static final String rootFolder =
            "/Users/sathish.santhanam/salesforce/cyan/performance/vpod/results-1-6-2017/jmeter/";

    private static final String[] subFolders = {"1-6-1kb-try2", "1-6-10kb-try1", "1-6-100kb"};

    public static void main(String args[]) throws Exception {
        System.out.println("Jmeter result processor, starting...");
        new JmeterResultProcessor().run();
    }

    public void run() throws Exception {
        List<File> fileList = loadFilesToAnalyze();
        fileList.stream().forEach(f -> {
            try {
                analyzeFile(f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    private List<File> loadFilesToAnalyze() {
        List<File> result = new ArrayList<>();
        for (String s : subFolders) {
            File f = new File(rootFolder, s);
            if (f.exists() && f.isDirectory()) {
                File[] files = f.listFiles();
                for (File data : files) {
                    if (data.isFile() && data.getName().contains(".csv") && data.getName().contains("test")) {
                        result.add(data);
                    }
                }
            }
        }
        return result;
    }

    private void analyzeFile(File f) throws Exception {
        System.out.println(f.getName());
        BufferedReader reader = new BufferedReader(new FileReader(f));
        List<Integer> latencies = new ArrayList<>();
        String line = reader.readLine();
        //ignore the first line
        line = reader.readLine();
        while(line != null) {
            int latency = readLatency(line);
            latencies.add(latency);
            line = reader.readLine();
        }

        printLatencyInfo(latencies);

    }

    private int readLatency(String line) {
        //comma separate items. The last field is the latency
        String[] s = line.split(",");
        return Integer.parseInt(s[s.length-1].trim());
    }

    private void printLatencyInfo(List<Integer> latencies) {
        Integer[] data = latencies.toArray(new Integer[]{});
        Arrays.sort(data);
        System.out.print("p50 -> " + data[data.length/2]);
        System.out.print("  p75 -> " + data[data.length * 3/4]);
        System.out.print("  p90 -> " + data[data.length * 9/10]);
        long sum = Arrays.stream(data).mapToInt(i -> i.intValue()).sum();
        System.out.println("  average -> " + sum/data.length);
        System.out.println();
    }
}
