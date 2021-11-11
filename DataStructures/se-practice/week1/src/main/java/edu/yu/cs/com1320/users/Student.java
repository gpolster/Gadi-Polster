package edu.yu.cs.com1320.users;

import edu.yu.cs.com1320.website.CourseOffering;
import edu.yu.cs.com1320.website.Department;
import edu.yu.cs.com1320.website.School;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Student extends User{
    private String major;
    private School school;
    private Map<CourseOffering, Integer> completedClasses = new HashMap<>();
    private Map<CourseOffering, Integer> gradeMap = new HashMap<>();
    public Student(String name, int idNum, int years, String major, School school,Map<CourseOffering, Integer> completedClasses){
        this.name = name;
        this.idNumber = idNum;
        this.yearsAtYU = years;
        this.school=school;
        this.major = major;
        this.completedClasses = completedClasses;
    }
    public int getIDNumber(User user){
        if (user instanceof Registrar){
            return this.idNumber;
        } else {
            return -1;
        }
    }
    public boolean addCompletedClass(CourseOffering co, int grade, User user){
        if(user.canSetGrades(school,co)){
            this.completedClasses.put(co,grade);
            return true;
        } else {
            return false;
        }
    }
    public boolean addClass(CourseOffering co, User user){
        if (user.canSetGrades(school, co) || user.equals(this) && !this.gradeMap.containsKey(co)){
            this.gradeMap.put(co, 100);
            return true;
        }
        return false;
    }
    public boolean hasPreReqs(CourseOffering co, User user){
        if (user.canSetGrades(school,co) || user.equals(this) ){
            for (int crn : co.getPreReqs()) {
                if (!this.completedClasses.containsKey(crn)){
                    return false;
                }
            }
            return true;
        }
        return  false;
    }
    public boolean setGradeInClass(CourseOffering courseOffering, User user, int grade) {
        if (user.canSetGrades(school,courseOffering)){
            gradeMap.put(courseOffering,grade);
            return true;
        } else {
            return false;
        }
    }

    public boolean inClass(CourseOffering courseOffering) {
        return gradeMap.containsKey(courseOffering);
    }

    public int getGradeInClass(CourseOffering courseOffering, User user) {
        if (user.canSetGrades(school,courseOffering) || user.equals(this) ){
            return gradeMap.get(courseOffering);
        } else {
            return -1;
        }
    }
}
