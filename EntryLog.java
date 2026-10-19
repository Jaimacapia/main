import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EntryLog {
    private Student student;
    private LocalDateTime entryTime;

    public EntryLog(Student student) {
        this.student = student;
        this.entryTime = LocalDateTime.now();
    }

    public Student getStudent() {
        return student;
    }

    public String getEntryDate() {
        return entryTime.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );
    }

    public String getEntryTime() {
        return entryTime.format(
                DateTimeFormatter.ofPattern("hh:mm:ss a")
        );
    }
}