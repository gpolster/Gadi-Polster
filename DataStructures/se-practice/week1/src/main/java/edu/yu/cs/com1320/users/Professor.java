package edu.yu.cs.com1320.users;

import edu.yu.cs.com1320.website.Course;
import edu.yu.cs.com1320.website.CourseOffering;
import edu.yu.cs.com1320.website.Department;

import java.util.HashSet;
import java.util.Set;

public class Professor extends User{
    private Department departmentProfWorks;
    public Professor(String name, int idNum, int years, Department dept, Set<CourseOffering> myCourseOfferings){
        this.name = name;
        this.idNumber = idNum;
        this.yearsAtYU = years;
        this.departmentProfWorks = dept;
    }
    Set<CourseOffering> getCourseOfferings(){
        Set<CourseOffering> myCourseOfferings = new HashSet<>();
        for(CourseOffering cs : departmentProfWorks.getCourseOfferings()){
            if(cs.getProfessor().equals(this)) {
                myCourseOfferings.add(cs);
            }
        }
        return myCourseOfferings;
    }

    public Department getDepartmentProfWorks() {
        return departmentProfWorks;
    }
    public int getIDNumber(User user){
        if (user instanceof Registrar){
            return this.idNumber;
        } else {
            return -1;
        }
    }
}
