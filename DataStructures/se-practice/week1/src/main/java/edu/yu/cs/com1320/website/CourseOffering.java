package edu.yu.cs.com1320.website;

import edu.yu.cs.com1320.users.Professor;
import edu.yu.cs.com1320.users.Student;
import edu.yu.cs.com1320.users.User;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CourseOffering {
    private final String name;
    private Department department;
    private int crn;
    private Set<Integer> preReqs = new HashSet<>();
    private Professor professor;
    private String time;
    private Set<String> days;
    private Set<Student> studentsInClass = new HashSet<>();
    public CourseOffering(String name, Professor prof, Department department, Set<Integer> preReqs, int crn, String time, Set<String> days) {
        this.name = name;
        this.department = department;
        this.preReqs = preReqs;
        this.crn = crn;
        this.professor = prof;
        this.time = time;
        this.days = days;
    }

    //    private Set<CourseOffering> myCourseOs = new HashSet<>();
    public String getName(){
        return this.name;
    }
    public Department getDepartment(){
        return this.department;
    }

    public Set<Integer> getPreReqs() {
        return this.preReqs;
    }

    public int getCrn() {
        return this.crn;
    }

    public Professor getProfessor() {
        return professor;
    }

    public Set<String> getDays() {
        return days;
    }

    public String getTime() {
        return time;
    }
    public boolean setGradeInClass(User user, Student student, int grade){
        if (user.canSetGrades(this.department.getSchool(), this)){
            return student.setGradeInClass(this, user, grade);
        } else {
            return  false;
        }
    }
    public int getGradeInClass(User user, Student student){
        if (!(user instanceof Student) || student.inClass(this)){
            return student.getGradeInClass(this, user);
        } else {
            return -1;
        }
    }
    public Set<Student> getStudentsInClass(User user){
        if (!(user instanceof Student)){
            return Set.copyOf(studentsInClass);
        } else {
            return new HashSet<>();
        }
    }
    public boolean addStudentToClass(User user, String name, int idNum, int years, String major, School school, Map<CourseOffering, Integer> completedClasses ){
        if(user.canAddStudentToCourse(this.department.getSchool(), this)){
            Student student = new Student(name, idNum, years, major, school,completedClasses);
            if (student.hasPreReqs(this, user)) {
                student.addClass(this, user);
                return this.studentsInClass.add(student);
            }
        }
        return false;
    }

}
