package edu.yu.cs.com1320.website;

import edu.yu.cs.com1320.users.User;

import java.util.HashSet;
import java.util.Set;

public class University {
    private String name;
    private Set<School> mySchools = new HashSet<>();
    public University(String name){
        this.name = name;
    }
    String getName(){
        return this.name;
    }
    Set<School> getSchools(){
        return Set.copyOf(this.mySchools);
    }
    boolean addSchool(User user, String name){
        School school = new School(name, this);
        if(user.canAddSchool()){
            return this.mySchools.add(school);
        }
        return false;
    }
}
