package com.dumbphone.forcestop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class RootShell {
    public static final class Result {
        public final int exitCode;
        public final String stdout;
        public final String stderr;

        Result(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public boolean succeeded() {
            return exitCode == 0;
        }
    }

    private RootShell() {
    }

    public static Result run(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
        String stdout = readFully(process.getInputStream());
        String stderr = readFully(process.getErrorStream());
        int exitCode = process.waitFor();
        return new Result(exitCode, stdout, stderr);
    }

    private static String readFully(InputStream input) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append('\n');
        }
        return result.toString();
    }
}
