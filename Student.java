public class Student {
    private String studentId;
    private String fullName;
    private String courseSection;
    private String yearLevel;

    public Student(String studentId, String fullName,
                   String courseSection, String yearLevel) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.courseSection = courseSection;
        this.yearLevel = yearLevel;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCourseSection() {
        return courseSection;
    }

    public String getYearLevel() {
        return yearLevel;
    }

}
