package edu.yu.regisrar;

import edu.yu.registrar.model.*;
import edu.yu.registrar.BetterMYYUInterface;
import edu.yu.registrar.query.DepartmentsSearch;
import edu.yu.registrar.query.MajorsSearch;
import edu.yu.registrar.query.SchoolsSearch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class BetterMYYUInterfaceTests {
    private School sySims;
    private School yc;
    private Department dep1;
    private Department dep2;
    private Department dep3;
    private Major major1;
    private Major major2;
    private Course course1;
    private Course course2;
    private Course course3;
    private Employee prof1;
    private Employee prof2;
    private Employee prof3;
    private CourseOffering co1;
    private CourseOffering co2;
    private CourseOffering co3;
    private CourseOffering co4;
    private User student1;
    private User student2;
    private User student3;
    private User student4;
    private Employee dean1;
    private Employee dean2;
    private Employee registrar;
    //still need to make deans and a registrar

//populates the university
    @BeforeEach
    public void init() throws Exception {
        //assumes that all of these are in the system:
//        "Pretend that the database has already been filled up with all the data about all the classes, students, professors, etc."
//        (Copied from the Slides)
        this.sySims = new School("SySims");
        this.yc = new School("Yeshiva College");
        this.dep1 = new Department("dep1-sims", this.sySims);
        this.dep2 = new Department("dep2-YC", this.yc);
        this.dep3 = new Department("dep3-YC", this.yc);
        this.prof1 = new Professor("Vladamir", "Kovtun", 1, this.dep1);
        this.prof2 = new Professor("Brandon", "Sanderson", 2, this.dep2);
        this.prof3 = new Professor("Hiram Habbid", "lopez Valdez", 3, this.dep3);
        this.course1 = new Course("Business Statistics", this.dep1, 11);
        this.course2 = new Course("Books on Books", this.dep2, 22);
        Set<Course> preReq = new HashSet<>();
        preReq.add(this.course1);
        this.course3 = new Course("Calculus 1", this.dep3, 33, preReq);
        Set<Course> mathCourses = new HashSet<>();
        mathCourses.add(this.course1);
        mathCourses.add(this.course3);
        this.major1 = new Major("math", this.yc, mathCourses);
        this.major2 = new Major("business", this.sySims);
        this.student1 = new Student("Gadi", "Polster", 2814123);
        this.student2 = new Student("Yair", "Polster", 3);
        this.student3 = new Student("Noam", "Polster", 2);
        this.student4 = new Student("Eitan", "Polster", 1);
        this.co1 = new CourseOffering(this.course1, 1, CourseOffering.Semester.FALL, (Professor) this.prof1);
        this.co2 = new CourseOffering(this.course2, 2, CourseOffering.Semester.SUMMER, (Professor) this.prof2);
        this.co3 = new CourseOffering(this.course3, 1, CourseOffering.Semester.SPRING, (Professor) this.prof3);
        this.co4 = new CourseOffering(this.course3, 1, CourseOffering.Semester.SPRING, (Professor) this.prof3);
        this.dean1 = new Employee("Dean", "Pelton", 123, Employee.Role.DEAN, this.sySims);
        this.dean2 = new Employee("Harry", "Potter",7, Employee.Role.DEAN, this.yc);
        this.registrar = new Employee("firstName", "lastName",0, Employee.Role.REGISTRAR,null);
    }
    @Test
    public void loginTest(){
        BetterMYYUInterface newYU = new BetterMYYUInterface();
        assertTrue(newYU.login(this.student1.getId()));
        assertFalse(newYU.login(999999999));
        System.out.println(("you passed: loginTest, yay!"));
    }
    @Test
    public void accessStudentTranscript(){
        BetterMYYUInterface yui = new BetterMYYUInterface();
        Student s1 = yui.accessStudentTranscript(this.student1.getId(), this.student1.getId());
        if (!(s1 instanceof ImmutableStudent)){
            fail();
        }
        assertThrows(UnauthorizedActionException.class, () -> {
            yui.accessStudentTranscript(this.student1.getId(),this.student1.getId());
        });
        Student s2 = yui.accessStudentTranscript(this.registrar.getId(),this.student1.getId());
        if ((s2 instanceof ImmutableStudent)){
            fail();
        }
        this.co3.addStudent((Student) this.student1);
        Student s3 = yui.accessStudentTranscript(this.prof3.getId(), this.student1.getId());
        if ((s3 instanceof ImmutableStudent)){
            fail();
        }
        assertThrows(UnauthorizedActionException.class, () -> {
            yui.accessStudentTranscript(this.prof2.getId(), this.student1.getId());
        });
        assertThrows(UnauthorizedActionException.class, () -> {
            yui.accessStudentTranscript(this.dean1.getId(), this.student1.getId());
        });
        s3 = yui.accessStudentTranscript(this.dean2.getId(), this.student1.getId());
        if ((s3 instanceof ImmutableStudent)){
            fail();
        }
        System.out.println(("you passed: accessStudentTranscript, yay!"));
    }
    @Test
    public void testAccessDeptCourseAndProfs(){
        BetterMYYUInterface yui = new BetterMYYUInterface();
        Department d1 = yui.accessDepartmentCoursesAndProfessors(this.registrar.getId(), new DepartmentsSearch(this.registrar.getId()));
        if ((d1 instanceof ImmutableDepartment)){
            fail();
        }
        Department d2 = yui.accessDepartmentCoursesAndProfessors(this.student2.getId(), new DepartmentsSearch(this.student2.getId()));
        if (!(d2 instanceof ImmutableDepartment)){
            fail();
        }
        //as per piazza @525_f1 we can pass in a query object
        //the one i am passing in is a department that dean1 is the dean of (this.dep1 to be exact)
        d1 = yui.accessDepartmentCoursesAndProfessors(this.dean1.getId(), new DepartmentsSearch(this.dean1.getId()));
        if ((d1 instanceof ImmutableDepartment)){
            fail();
        }
        //the dept am passing in is a department that dean1 is NOT the dean of (this.dep2 to be exact)
        d1 = yui.accessDepartmentCoursesAndProfessors(this.dean1.getId(), new DepartmentsSearch(this.dean1.getId()));
        if (!(d1 instanceof ImmutableDepartment)){
            fail();
        }
        System.out.println("you passed: testAccessDeptCourseAndProfs, yay!");
    }
    @Test
    public void testAddDropClass(){
        BetterMYYUInterface yui = new BetterMYYUInterface();
        //a student adding/dropping a course at the correct time
        boolean s1 = yui.addOrDropClasses(this.student1.getId(), this.student1.getId(), CourseOffering.Semester.FALL);
        assertTrue(s1);
        // a student adding/dropping a course at the correct time for a DIFFERENT student
        assertThrows(UnauthorizedActionException.class, () -> {
            yui.addOrDropClasses(this.student2.getId(), this.student1.getId(), CourseOffering.Semester.FALL);
        });
        //a student adding/dropping a course at the WRONG time
        assertThrows(UnauthorizedActionException.class, () -> {
            yui.addOrDropClasses(this.student1.getId(), this.student1.getId(), CourseOffering.Semester.SPRING);
        });
        //a registrar adding/dropping a course at the WRONG time, but still should work bc he's a registrar ;)
        boolean s2 = yui.addOrDropClasses(this.registrar.getId(), this.student1.getId(), CourseOffering.Semester.SPRING);
        assertTrue(s2);
        System.out.println("you passed: testAddDropClass, yay!");
    }
    @Test
    public void testAccessMajors(){
        BetterMYYUInterface yui = new BetterMYYUInterface();
        Major m1 = yui.accessMajors(this.registrar.getId(), new MajorsSearch(this.registrar.getId()));
        if ((m1 instanceof ImmutableMajor)){
            fail();
        }
        Major m2 = yui.accessMajors(this.student2.getId(), new MajorsSearch(this.student2.getId()));
        if (!(m2 instanceof ImmutableMajor)){
            fail();
        }
        //as per piazza @525_f1 we can pass in a query object
        //the one i am passing in is a major of a school that dean1 is the dean of (this.major2 to be exact)
        m1 = yui.accessMajors(this.dean1.getId(), new MajorsSearch(this.dean1.getId()));
        if ((m1 instanceof ImmutableMajor)){
            fail();
        }
        //the dept am passing in is a major of a school that dean1 is NOT the dean of (this.major1 to be exact)
        m1 = yui.accessMajors(this.dean1.getId(), new MajorsSearch(this.dean1.getId()));
        if (!(m1 instanceof ImmutableMajor)){
            fail();
        }
        System.out.println("you passed: testAccessMajors, yay!");
    }

    @Test
    public void testGetSchoolInfo(){
        BetterMYYUInterface yui = new BetterMYYUInterface();
        School s1 = yui.getSchoolInfo(this.registrar.getId(), new SchoolsSearch(this.registrar.getId()));
        if ((s1 instanceof ImmutableSchool)){
            fail();
        }
        School s2 = yui.getSchoolInfo(this.student2.getId(), new SchoolsSearch(this.student2.getId()));
        if (!(s2 instanceof ImmutableSchool)){
            fail();
        }
        //as per piazza @525_f1 we can pass in a query object
        //the one i am passing in is a major of a school that dean1 is the dean of (this.major2 to be exact)
        s1 = yui.getSchoolInfo(this.dean1.getId(), new SchoolsSearch(this.dean1.getId()));
        if ((s1 instanceof ImmutableSchool)){
            fail();
        }
        //the dept am passing in is a major of a school that dean1 is NOT the dean of (this.major1 to be exact)
        s2 = yui.getSchoolInfo(this.dean1.getId(), new SchoolsSearch(this.dean1.getId()));
        if (!(s2 instanceof ImmutableSchool)){
            fail();
        }
        System.out.println("you passed: testGetSchoolInfo, yay!");

    }
    @Test
    public void testAccessProfInfo(){
        BetterMYYUInterface yui = new BetterMYYUInterface();
        Professor p1 = yui.accessProfessorInfo(this.registrar.getId(), this.prof1.getId());
        if ((p1 instanceof ImmutableProfessor)){
            fail();
        }
        Professor p2 = yui.accessProfessorInfo(this.prof2.getId(), this.prof2.getId());
        if (!(p2 instanceof ImmutableProfessor)){
            fail();
        }

        p1 = yui.accessProfessorInfo(this.dean1.getId(), prof2.getId());
        if ((p1 instanceof ImmutableProfessor)){
            fail();
        }

        p2 = yui.accessProfessorInfo(this.dean1.getId(), this.prof1.getId());
        if (!(p2 instanceof ImmutableProfessor)){
            fail();
        }
        System.out.println("you passed: testAccessProfInfo, yay!");
    }
    @Test
    public void testAccessSpecificCourseOffering(){
        BetterMYYUInterface yui = new BetterMYYUInterface();
        CourseOffering c1 = yui.accessSpecificCourseOffering(this.registrar.getId(), this.prof1.getId(), CourseOffering.Semester.FALL);
        if ((c1 instanceof ImmutableCourseOffering)){
            fail();
        }
        CourseOffering c2 = yui.accessSpecificCourseOffering(this.prof2.getId(), this.prof2.getId(), CourseOffering.Semester.FALL);
        if ((c2 instanceof ImmutableCourseOffering)){
            fail();
        }
        assertThrows(UnauthorizedActionException.class, () -> {
            yui.accessSpecificCourseOffering(this.prof2.getId(), this.prof2.getId(), CourseOffering.Semester.SPRING);
        });
        CourseOffering c3 = yui.accessSpecificCourseOffering(this.prof1.getId(), this.prof2.getId(), CourseOffering.Semester.FALL);
        if (!(c3 instanceof ImmutableCourseOffering)){
            fail();
        }

        c1 = yui.accessSpecificCourseOffering(this.dean1.getId(), prof2.getId(), CourseOffering.Semester.SUMMER);
        if ((c1 instanceof ImmutableCourseOffering)){
            fail();
        }

        c2 = yui.accessSpecificCourseOffering(this.dean1.getId(), this.prof1.getId(), CourseOffering.Semester.SPRING);
        if (!(c2 instanceof ImmutableCourseOffering)){
            fail();
        }
        System.out.println("you passed: testAccessSpecificCourseOffering, yay!");
    }

//        this.co2 = new CourseOffering(this.course2, 2, CourseOffering.Semester.SUMMER, (Professor) this.prof2);
//        this.co3 = new CourseOffering(this.course3, 1, CourseOffering.Semester.SPRING, (Professor) this.prof3);
//        this.co4 = new CourseOffering(this.course3, 1, CourseOffering.Semester.SPRING, (Professor) this.prof3);

    @Test
    public void testFindCoursesAProfessorTeaches(){
        BetterMYYUInterface yui = new BetterMYYUInterface();
        this.co4 = new CourseOffering(this.course2, 1, CourseOffering.Semester.SPRING, (Professor) this.prof3);
        Set<CourseOffering> cos1 = yui.findCoursesAProfessorTeaches(this.student1.getId(), this.prof3.getId(), CourseOffering.Semester.SPRING );
        assertEquals(2, cos1.size());
        for(CourseOffering co : cos1) {
            if (!(co instanceof ImmutableCourseOffering)) {
                fail();
            }
        }
        Set<CourseOffering> cos2 = yui.findCoursesAProfessorTeaches(this.prof3.getId(), this.prof3.getId(), CourseOffering.Semester.SPRING );
        assertEquals(2, cos2.size());
        for(CourseOffering co : cos2) {
            if ((co instanceof ImmutableCourseOffering)) {
                fail();
            }
        }
        Set<CourseOffering> cos3 = yui.findCoursesAProfessorTeaches(this.prof2.getId(), this.prof2.getId(), CourseOffering.Semester.SUMMER);
        assertEquals(1, cos3.size());
        Set<CourseOffering> cos4 = yui.findCoursesAProfessorTeaches(this.prof1.getId(), this.prof2.getId(), CourseOffering.Semester.SPRING);
        assertEquals(0, cos4.size());
        System.out.println("you passed: testFindCoursesAProfessorTeaches, yay!");
    }

}
