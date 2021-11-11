package edu.yu.cs.com1320.users;

import edu.yu.cs.com1320.website.Course;
import edu.yu.cs.com1320.website.CourseOffering;
import edu.yu.cs.com1320.website.School;
//I am going off the assumption that everyone in this database has logged in,
// and therefore is in some way or another an instance of user
public abstract class User {
    String name = "";
    int yearsAtYU = 0;
    int idNumber;

    String getName(){
        return name;
    }
    int getYearsAtYU(){
        return yearsAtYU;
    }
    public boolean canAddSchool(){
        return (this instanceof Registrar);
    }
    public boolean canAddProfessorOrDepartment(School school){
        if(this instanceof Dean){
            if (((Dean) this).getSchool().equals(school)){
                return true;
            }
        } else if (this instanceof Registrar){
            return true;
        }
        return false;
    }
    public boolean canAddCourse(School school){
        if (this instanceof Registrar){
            return true;
        } else if(this instanceof Dean){
            if (((Dean) this).getSchool().equals(school)){
                    return true;
            }
        }
        return false;
    }
    public boolean canAddStudentToCourse(School school, CourseOffering courseO){
        if (canAddCourse(school)){
            return true;
        } else if (this instanceof Professor){
            if(((Professor) this).getCourseOfferings().contains(courseO)){
                return true;
            }
        }
        return false;
    }
    public boolean canSetGrades(School school, CourseOffering courseO){
        if(canAddCourse(school)){
            return true;
        } else if (this instanceof Professor){
            if(((Professor) this).getCourseOfferings().contains(courseO)){
                return true;
            }
        }
        return false;
    }
}
