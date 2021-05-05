import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class Logger {
    private String path;
    private PrintWriter logWriter;

    public Logger(String path) {
        this.path = path;
        try {
            logWriter = new PrintWriter(new FileWriter(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            logWriter.flush();
            logWriter.close();
        } finally {
            if (logWriter != null) {
                logWriter.close();
            }
        }
    }

    public void writeLogEntry(String entry) {
        String timestamp = LocalDateTime.now().toString();
        logWriter.print(timestamp + ": " + entry);
        logWriter.flush();
    }
}

