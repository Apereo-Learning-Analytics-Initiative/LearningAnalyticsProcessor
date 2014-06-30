-- This runs for the temporary database only (on every application server load)
-- Script failures will cause the application to fail to start
-- MySQL compatible schema
DROP TABLE IF EXISTS USERS;
DROP TABLE IF EXISTS COURSES;
