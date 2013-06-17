OAA Data Extracts
There are 4 data extracts used as inputs to the student data processing.
All files are CSV files and the columns must be in the order shown.
See the sample files for examples.

*******************************************************************************
** FORMATS

1) PERSONAL (Student Demographics Data) - students.csv
   The personal data had all the demographic details of the student namely,

ALTERNATIVE_ID          The CWID of the student replaced with some unique identifiers for security reasons.
PERCENTILE              The high school ranking of the students.
SAT_VERBAL              The numeric SAT verbal score.
SAT_MATH                The numeric SAT mathematics score.
ACL_COMPOSITE           The ACT score of the Student (Either of the scores will be reported by the students)
DOB                     The birth date of the student
RACE                    The race of the student (self-reported)
GENDER                  The gender of the student (self-reported)
STATUS                  Code for full-time or part-time student based on the number of credit hours currently enrolled.
SEMESTERS               The current academic standing of the student as expressed by the number of semesters of completed coursework
EARNED_CREDIT_HOURS     The total number of hours earned by each of the student
GPA_CUMULATIVE          Cumulative university grade point average (four point scale).
GPA_SEMESTER            Semester university grade point average (four point scale).
STANDING                Current university standing such as probation, dean’s list, or semester honors.


2) COURSES (Course Data) - courses.csv
   The course data has all the details of the course that the students are enrolled in

COURSE_ID               The course number of the course.
ALTERNATIVE_ID          The CWID of the student replaced with some unique identifiers for security reasons.
ENROLLMENT              The number of students in the course / section
FINAL_GRADE             The final course grade of the Student.
                        Entries are A,A-,B+,B,B-,C+,C,C-,D,F,I, or W (or null).
                        If the student drops the course within the official drop/add window, the course grade field will be null.


3) GRADES (LMS Gradebook Data) - grades.csv
   The CMS Gradebook data is extracted from the LMS and it provides the information involving 

ALTERNATIVE_ID          The CWID of the student replaced with some unique identifiers for security reasons.
COURSE_ID               The course number of the course.
GRADABLE_OBJECT         Different gradable objects and the course
CATEGORY                The gradable objects are categorized here. For example grouping of a bunch of related assignments, forum posting, projects etc
MAX_POINTS              Maximum allocated points for each Gradable Object
EARNED_POINTS           Points earned by the students for a particular gradable object
WEIGHT                  Overall weight of that particular assignment towards final grading.
GRADE_DATE              To facilitate chronological division of gradebook. Helpful in breaking down the gradebook like 4 weeks or 8 weeks into the course during testing phases.


4) EVENTS (LMS Usage Data) - usage.csv
   The LMS Events table has details of the events generated for each of the tool usage by the students

ALTERNATIVE_ID          The CWID of the student replaced with some unique identifiers for security reasons.
COURSE_ID               The course number of the course.
EVENT                   The name of the event that was generated
EVENT_DATE              The date when the event occurred


*******************************************************************************
** NOTES

A) Recoding Required to work with Weka
Certain Variables in each of these data sets have to be recoded to replace numeric values as Weka works only with numeric values.
Alternative ID - ???
Course ID - ???
Race {1=B, 2=H, 3=I, 4=N, 5=O, 6=P, 7=W, 8=X}
Gender {1 = Female, 2 = Male}
Full-time or Part-time Status {1 = Full-time student, 2 = Part-time student}
Class Code {1= FR (Freshman), 2= SO (Sophomore),3= JR (Junior),4=SR(Senior),5=GR(Graduate)}
University Standing {0 = Probation, 1 = Regular standing, 2 = Semester honors, 3 = Semester honors and dean list}
Enrollment {-1 and 0 changed to 1}
Letter Grade
4 = A 
3.7=A-
3.3=B+
3 = B 
2.7=B-
2.3=C+
2 = C 
1.7=C-
1 = D 
0 = F 
null = I or W

B) Aptitude score
Defined as the SAT composite score or the converted ACT to SAT score.  In  the cases in which students have  both SAT and ACT scores, the  SAT score will remain

C) Age
Converted from the birth date, expressed in years.

D) Tool_Usage
The count of number of times each of the tool are accessed in the course site
Missing values recoded to 0.

E) Academic Success
Defined as students completing the course within the normal timeframe and receiving a grade of C or better.
 1 = Grade Below C 
 2 = Grade of C or better
