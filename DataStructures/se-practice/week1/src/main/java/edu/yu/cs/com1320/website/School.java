package edu.yu.cs.com1320.website;

import edu.yu.cs.com1320.users.User;

import java.util.HashSet;
import java.util.Set;

public class School {
    private String name;
    private University university;
    private Set<Department> myDepartments = new HashSet<>();
    public School(String name, University university){
        this.name = name;
        this.university = university;
    }
    String getName(){
        return this.name;
    }
    University getUniversity(){
        return this.university;
    }
    Set<Department> getDepartments(){
        return this.myDepartments;
    }
    boolean addDepartment(User user, String departmentName){
        Department department = new Department(departmentName, this);
        if(user.canAddProfessorOrDepartment(this)){
            return this.myDepartments.add(department);
        }
        return false;
    }

}
