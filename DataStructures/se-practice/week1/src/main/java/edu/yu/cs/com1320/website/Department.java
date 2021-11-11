package edu.yu.cs.com1320.website;

import edu.yu.cs.com1320.users.Professor;
import edu.yu.cs.com1320.users.User;

import java.util.HashSet;
import java.util.Set;

public class Department {
    private final String name;
    private School school;
    private Set<Course> myCourses = new HashSet<>();
    private Set<Professor> myProfessors = new HashSet<>();
    public Department(String name, School school){
        this.name = name;
        this.school = school;
    }
    public String getName(){
        return this.name;
    }
    public School getSchool(){
        return this.school;
    }
    public Set<Course> getCourses(){
        return Set.copyOf(this.myCourses);
    }
    public Set<CourseOffering> getCourseOfferings(){
        Set<CourseOffering> coSet = new HashSet<>();
        for (Course c : myCourses){
            coSet.addAll(c.getCourseOfferings());
        }
        return coSet;
    }
    boolean addCourse(User user, String name,Set<Integer> preReqs, int crn){
        Course course = new Course(name, this, preReqs, crn);
        if(user.canAddCourse(this.school)){
            return this.myCourses.add(course);
        }
        return false;
    }
    public Set<Professor> getProfessors(){
        return Set.copyOf(this.myProfessors);
    }
    boolean addProfessor(User user, Professor professor){
        if(user.canAddProfessorOrDepartment(this.school)){
            return this.myProfessors.add(professor);
        }
        return false;
    }

}
