-- This schema create script runs on every application load
-- Script failures will cause the application to fail to start
-- MySQL compatible schema
CREATE TABLE IF NOT EXISTS PERSONAL (
  ALTERNATIVE_ID varchar(100) NOT NULL,
  PERCENTILE float NOT NULL DEFAULT 0.0,
  SAT_VERBAL integer NOT NULL DEFAULT 0,
  SAT_MATH integer NOT NULL DEFAULT 0,
  ACL_COMPOSITE integer NOT NULL DEFAULT 0,
  AGE integer NOT NULL DEFAULT 1,
  RACE integer,
  GENDER integer,
  STATUS integer NOT NULL DEFAULT 1,
  SEMESTERS integer NOT NULL DEFAULT 0,
  EARNED_CREDIT_HOURS integer NOT NULL DEFAULT 0,
  GPA_CUMULATIVE float,
  GPA_SEMESTER float,
  STANDING integer,
  PELL_STATUS boolean DEFAULT FALSE,
  PRIMARY KEY (ALTERNATIVE_ID)
);

CREATE TABLE IF NOT EXISTS COURSE (
  COURSE_ID varchar(100) NOT NULL,
  SUBJECT varchar(55),
  COURSE_NUMBER varchar(55),
  SECTION varchar(55),
  TERM varchar(25),
  ENROLLMENT integer DEFAULT 0,
  COURSE_TYPE integer DEFAULT 0,
  PRIMARY KEY (COURSE_ID)
);

CREATE TABLE IF NOT EXISTS ENROLLMENT (
  ALTERNATIVE_ID varchar(100) NOT NULL,
  COURSE_ID varchar(100) NOT NULL,
  FINAL_GRADE varchar(10),
  WITHDRAWL_DATE TIMESTAMP,
  PRIMARY KEY (ALTERNATIVE_ID,COURSE_ID),
  FOREIGN KEY (ALTERNATIVE_ID) REFERENCES PERSONAL(ALTERNATIVE_ID),
  FOREIGN KEY (COURSE_ID) REFERENCES COURSE(COURSE_ID)
);

CREATE TABLE IF NOT EXISTS GRADE (
  ALTERNATIVE_ID varchar(100) NOT NULL,
  COURSE_ID varchar(100) NOT NULL,
  GRADABLE_OBJECT varchar(255),
  CATEGORY varchar(255),
  MAX_POINTS integer DEFAULT 0,
  EARNED_POINTS integer DEFAULT 0,
  WEIGHT float,
  GRADE_DATE TIMESTAMP,
  PRIMARY KEY (ALTERNATIVE_ID,COURSE_ID,GRADABLE_OBJECT),
  FOREIGN KEY (ALTERNATIVE_ID) REFERENCES PERSONAL(ALTERNATIVE_ID),
  FOREIGN KEY (COURSE_ID) REFERENCES COURSE(COURSE_ID)
);

CREATE TABLE IF NOT EXISTS ACTIVITY (
  ALTERNATIVE_ID varchar(100) NOT NULL,
  COURSE_ID varchar(100) NOT NULL,
  EVENT varchar(255),
  EVENT_DATE TIMESTAMP,
  PRIMARY KEY (ALTERNATIVE_ID,COURSE_ID,EVENT),
  FOREIGN KEY (ALTERNATIVE_ID) REFERENCES PERSONAL(ALTERNATIVE_ID),
  FOREIGN KEY (COURSE_ID) REFERENCES COURSE(COURSE_ID)
);
