LAP Data Formats
================
There are 5 data extracts formats used as inputs to the student data processing.
Typically, formats 1,2,3 are extracted from an SIS. Formats 4 and 5 are from an LMS.
All files are CSV files and the columns must be in the order shown.
See the sample files for examples.

FORMATS
=======

1) PERSONAL (Student Demographics Data) - personal.csv
------------------------------------------------------
The personal data includes all students and their demographic details.

COLUMN                  | FORMAT            | DESCRIPTION
----------------------- |:-----------------:|------------------------------------------
ALTERNATIVE_ID          | String(100)       | The CWID(College Wide ID) of the student replaced with some unique identifiers for security reasons.
PERCENTILE              | Float[0-100.0]    | The high school ranking of the students (e.g. 85 means 85th percentile).
SAT_VERBAL              | Integer[200-800]  | The numeric SAT verbal score (or 0/blank to indicate no score).
SAT_MATH                | Integer[200-800]  | The numeric SAT mathematics score (or 0/blank to indicate no score).
ACT_COMPOSITE           | Integer[1-36]     | The ACT composite score of the Student (or 0/blank to indicate no score)
AGE                     | Integer[1-150]    | The age of the student (in years)
RACE                    | [1-8] *See Notes* | The race of the student (self-reported)
GENDER                  | [M,F]             | The gender of the student (self-reported)
ENROLLMENT_STATUS       | [F,P]             | Code for full-time (F) or part-time (P) student (by credit hours currently enrolled).
EARNED_CREDIT_HOURS     | Integer[1-1000]   | The total number of credit hours earned by each of the student
GPA_CUMULATIVE          | Float[0-4.0]      | Cumulative university grade point average (float - four point scale - [0.00 - 4.00])
GPA_SEMESTER            | Float[0-4.0]      | Semester university grade point average (float - four point scale - [0.00 - 4.00])
STANDING                | [0-2] *See Notes* | Current university standing such as probation, regular standing or dean’s list/semester honors.
PELL_STATUS             | [Y,N]             | If a student is a Pell grant recipient: Y=Yes, N=No
CLASS                   | [SR,JR,SO,FR,GR]  | SR=Senior JR= Junior SO=Sophomore FR=Freshmen GR=Graduate

2) COURSE (Courses Data) - course.csv
-------------------------------------
The course data has all the relevant details of the courses.

COLUMN                  | FORMAT            | DESCRIPTION
----------------------- |:-----------------:|------------------------------------------
COURSE_ID               | String(100)       | The unique identifier standard across SIS and LMS for the course. Usually in the format Subject_CourseNumber_Section_Term.
SUBJECT                 | String(50)        | The subject of the course.
ENROLLMENT              | Integer[1-1000]   | The number of students in the course / section.
ONLINE                  | [Y,N]             | Y = Online, N = Classroom or other non-online


3) ENROLLMENT (Student enrollments) - enrollment.csv
----------------------------------------------------
The enrollments data has all the details of the courses that the students are enrolled in.

COLUMN                  | FORMAT            | DESCRIPTION
----------------------- |:-----------------:|------------------------------------------
ALTERNATIVE_ID          | String(100)       | The CWID of the student replaced with some unique identifiers for security reasons.
COURSE_ID               | String(100)       | The unique identifier standard across SIS and LMS for the course. Usually in the format Subject_CourseNumber_Section_Term.
FINAL_GRADE             | String *SPECIAL*  | The final course grade of the Student. Entries are A,A-,B+,B,B-,C+,C,C-,D,F,I, or W (or null). If the student drops the course within the official drop/add window, the course grade field will be null.
WITHDRAWL_DATE          | [ISO-8601,""]     | The date the student opted out from the course (null if they did not drop the course).


4) GRADE (LMS Gradebook Data) - grade.csv
-----------------------------------------
The gradebook data is extracted from the LMS and it provides the information about all graded items in the course.

COLUMN                  | FORMAT            | DESCRIPTION
----------------------- |:-----------------:|------------------------------------------
ALTERNATIVE_ID          | String(100)        | The CWID of the student replaced with some unique identifiers for security reasons.
COURSE_ID               | String(100)       | The unique identifier standard across SIS and LMS for the course. Usually in the format Subject_CourseNumber_Section_Term.
GRADABLE_OBJECT         | String(250)       | Different gradable objects and the course
CATEGORY                | String(250)       | The gradable objects are categorized here. For example grouping of a bunch of related assignments, forum posting, projects etc
MAX_POINTS              | Integer[0-1000]   | Maximum allocated points for each Gradable Object
EARNED_POINTS           | Integer[0-1000]   | Points earned by the students for a particular gradable object
WEIGHT                  | Float[0.0-1.0]    | Overall percent weight of that particular assignment towards final grading (e.g. 0.5 means 50% of the overall grade).
GRADE_DATE              | ISO-8601          | To facilitate chronological division of gradebook. Helpful in breaking down the gradebook like 4 weeks or 8 weeks into the course during testing phases.


5) ACTIVITY (LMS Usage Data) - activity.csv
-------------------------------------------
Includes information about student activity (typically events related to learning tools in the LMS).

COLUMN                  | FORMAT            | DESCRIPTION
----------------------- |:-----------------:|------------------------------------------
ALTERNATIVE_ID          | String(100)       | The CWID of the student replaced with some unique identifiers for security reasons.
COURSE_ID               | String(100)       | The unique identifier standard across SIS and LMS for the course. Usually in the format Subject_CourseNumber_Section_Term.
EVENT                   | String(250)       | The name of the event that was generated
EVENT_DATE              | ISO-8601          | The date when the event occurred


*******************************************************************************

NOTES
=====

1. Recoding Required to work with Weka  
   Certain Variables in each of these data sets have to be recoded to replace numeric values as Weka works only with numeric values.
    * Alternative ID - no recoding necessary
    * Course ID - no recoding necessary
    * Race {1= White, 2= American Indian or Alaska Native, 3=Asian, 4=Black or African American, 5=Hispanic, 6=Native Hawaiian or Other Pacific Islander, 7= Two or More Races}
    * Gender M (male), F (female), converted in DB: {1 = Female, 2 = Male}
    * Full-time or Part-time Status {1 = Full-time student, 2 = Part-time student}
    * Class Code: FR(Freshman), SO(Sophomore), JR(Junior), SR(Senior), GR(Graduate), converted in DB {1 = FR, 2 = SO, 3 = JR, 4 = SR, 5 = GR}
    * University Standing {0 = Probation, 1 = Regular standing, 2 = Semester honors / dean list}
    * Pell Status: 1=Yes and 2=No
    * Letter Grade

            4.0 = A 
            3.7 = A-
            3.3 = B+
            3.0 = B 
            2.7 = B-
            2.3 = C+
            2.0 = C 
            1.7 = C-
            1.0 = D 
            0.0 = F 
            null = I or W

2. Aptitude score  
   Defined as the SAT composite score or the converted ACT to SAT score.  In  the cases in which students have  both SAT and ACT scores, the  SAT score will remain

3. Age  
   Converted from the birth date, expressed in years.

4. Academic_Risk  
   Defined as students completing the course within the normal timeframe and receiving a grade of C or better.  
   1 = Grade Below C  
   2 = Grade of C or better  
