package testapp.model;

import act.data.annotation.Data;
import org.osgl.$;

@Data(callSuper = true)
public class Student extends Person {
    private String clazz;
    private String studentId;
    private double score;

    public Student(String fn, String ln, Address addr, int age, String clazz, String studentId, double score) {
        super(fn, ln, addr, age);
        this.clazz = clazz;
        this.studentId = studentId;
        this.score = score;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Student) {
            Student that = (Student) obj;
            return super.equals(that) && $.eq(that.clazz, this.clazz) && $.eq(that.studentId, this.studentId) && $.eq(that.score, this.score);
        }
        return false;
    }

    @Override
    public int hashCode() {
        String foo = "";
        int bar = 4;
        byte z = 2;
        return $.hc(clazz, studentId, score, foo, bar, z, super.hashCode());
    }

}
