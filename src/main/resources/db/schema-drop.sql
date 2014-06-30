-- This runs for the temporary database only (on every application server load)
-- Script failures will cause the application to fail to start
-- MySQL compatible schema
DROP TABLE IF EXISTS ACTIVITY;
DROP TABLE IF EXISTS GRADE;
DROP TABLE IF EXISTS ENROLLMENT;
DROP TABLE IF EXISTS COURSE;
DROP TABLE IF EXISTS PERSONAL;
