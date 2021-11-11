package edu.yu.cs.com1320.website;

import edu.yu.cs.com1320.users.Professor;
import edu.yu.cs.com1320.users.User;

import java.util.HashSet;
import java.util.Set;

public class Course {
    private final String name;
    private Department department;
    private int crn;
    private Set<CourseOffering> myCourseOs = new HashSet<>();
    private Set<Integer> preReqs = new HashSet<>();
    public Course(String name, Department department, Set<Integer> preReqs, int crn){
        this.name = name;
        this.department = department;
        this.preReqs = preReqs;
        this.crn = crn;
    }
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
    boolean addCourseOffering(User user, Set<Integer> preReqs, Professor prof, int crn, String time, Set<String> days){
        if(user.canAddCourse(this.department.getSchool())){
            CourseOffering courseOffering = new CourseOffering(name, prof, this.department, preReqs, crn, time, days);
            return this.myCourseOs.add(courseOffering);
        }
        return false;
    }
    public Set<CourseOffering> getCourseOfferings(){
        return this.myCourseOs;
    }
}
