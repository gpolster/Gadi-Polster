package edu.yu.registrar;
import edu.yu.registrar.model.*;
//even though the query package is not used in the API, it will be used in all of the public methods
import edu.yu.registrar.query.*;

import java.util.Set;

public class BetterMYYUInterface {
    int myUserID;

    public BetterMYYUInterface(){
    }


    /**
     * makes sure that userID is a valid and current userID of someone who can access the user interface
     * @param userID
     * @return
     */
    public boolean login(int userID){
        this.myUserID = userID;
        return false;
    }
    public void logout(){
        this.myUserID = -1;
    }
    /**
     * returns an instance of Student (either Student itself or an immuttable student),
     * the qualifier for which is who is trying to access the student's transcript.
     * this is strictly for past classes therefore there is no reason why the student would ever be able to access the actual Student class itself
     * @param userID used to identify the user that is trying to access the student's transcript
     * @param searchID the ID to search to find the Student the user wants
     * @return an instance of student if the user is qualified to access this students transcript, otherwise null
     *
     * @throws UnauthorizedActionException if this userID is not allowed to access the searchID
     */
    public Student accessStudentTranscript(int userID, int searchID){
        return null;
    }

    /**
     * returns an instance of department (either Department itself or an immutable Department)
     * the qualifier is who is trying to access the department
     * the user will be able to see current professors and courses with the possibilty of changing them depending on who is accessing it
     * @param userID used to identify the user that is trying to access the department
     * @param search the search to find the department the user wants
     * @return
     */
    public Department accessDepartmentCoursesAndProfessors(int userID, DepartmentsSearch search){
        return null;
    }

    /**
     * the method for a student to add/drop a class
     * checks to make sure there are no holds on the account
     * if its a registrar/dean they can add/drop at anytime they'd like
     * @param userId used to identify who is trying to add/drop classes
     * @param searchID the ID to search for the student the user is looking to add/drop classes for
     * @param semester the semester of which the user is add/dropping classes
     * @return the boolean of the Student.removeCourse/ if the user is qualified to add or drop a class at this time, otherwise an UnauthorizedActionException
     * there is no case where an immutableStudent would be returned
     @throws UnauthorizedActionException if this userID is not allowed to access the searchID
     */
    public boolean addOrDropClasses(int userId, int searchID, CourseOffering.Semester semester){
        return false;
    }
    /**
     * returns an instance of Major (either major itself or an immutable major)
     * the qualifier is who is trying to access the major
     * the user will be able to see what courses this major requires with the possibilty of changing them depending on who is accessing it
     * @param userID used to identify the user that is trying to access the major
     * @param search the search to find the major the user wants
     * @return
     */
    public Major accessMajors(int userID, MajorsSearch search){
        return null;
    }

    /**
     * returns an instance of School (either school itself or an immutable school)
     * Only the registrar and dean of this specific school can have access to an instance of School (as in not immutable)
     * a consise way of viewing or accessing the entire school at one time
     * @param userID used to identify the user that is trying to access the school
     * @param search the search to find the School the user wants
     * @return

     */
    public School getSchoolInfo(int userID, SchoolsSearch search){
        return null;
    }

    /**
     * returns an instance of Professor (either professor itself or an immutable professor)
     * the qualifier is who is trying to access this professor
     * ability to see all information about this professor without having to go through their department
     * @param userID used to identify the user that is trying to access the professor
     * @param searchID the ID to search to find the Professor the user wants
     * @return

     */
    public Professor accessProfessorInfo(int userID, int searchID){
        return null;
    }

    /**
     * the method for a someone to access a specific course offering
     * ability to change or just view the courseOffering depends on who is trying to access it
     * ability to view which students are in the class is only allowed to the dean of this school, the registrar, and the professor of this class
     * @param userID used to identify who is trying to acssess this course offering
     * @param courseNumber the # that identifies the course offering the user wants
     * @param semester the semester of which the specific course offering takes place
     * @return
     @throws UnauthorizedActionException if this course offering isnt offered this semester
     */
    public CourseOffering accessSpecificCourseOffering(int userID, int courseNumber, CourseOffering.Semester semester){
        return null;
    }

    /**
     *
     * @param userID id of person trying to access this, decides whether to return immutable or non immutable CourseOfferings
     * @param profSearchID id of professor
     * @param semester the semester of which the specific course offerings take place
     * @return set of course offerings the professor teaches
     return an empty set if this professor doesnt teach any courses this semester
     */
    public Set<CourseOffering> findCoursesAProfessorTeaches(int userID, int profSearchID, CourseOffering.Semester semester){
        return null;
    }

}
