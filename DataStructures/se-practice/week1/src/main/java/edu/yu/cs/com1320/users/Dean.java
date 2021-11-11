package edu.yu.cs.com1320.users;

import edu.yu.cs.com1320.website.Department;
import edu.yu.cs.com1320.website.School;

public class Dean extends User{
    private School school;
    public Dean(String name, int idNum, int years, School school){
        this.name = name;
        this.yearsAtYU = years;
        this.idNumber = idNum;
        this.school = school;
    }
    School getSchool(){
        return this.school;
    }
    public int getIDNumber(User user){
        if (user instanceof Registrar){
            return this.idNumber;
        } else {
            return -1;
        }
    }

}
