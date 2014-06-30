-- This schema create script runs on every application load
-- Script failures will cause the application to fail to start
-- MySQL compatible schema
CREATE TABLE IF NOT EXISTS USERS (
  USER_ID varchar(255) NOT NULL,
  EMAIL varchar(255) DEFAULT NULL,
  PRIMARY KEY (USER_ID)
);

CREATE TABLE IF NOT EXISTS COURSES (
  COURSE_ID varchar(255) NOT NULL,
  SUBJECT varchar(55) DEFAULT NULL,
  COURSE_NUMBER varchar(55) DEFAULT NULL,
  SECTION varchar(55) DEFAULT NULL,
  ENROLLMENT integer DEFAULT 0,
  COURSE_TYPE varchar(55) DEFAULT NULL,
  PRIMARY KEY (COURSE_ID)
);
