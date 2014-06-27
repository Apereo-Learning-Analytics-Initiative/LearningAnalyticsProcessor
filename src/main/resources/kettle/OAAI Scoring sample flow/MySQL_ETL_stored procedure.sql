/******************************************************************************************************
* MySQL doesn't provide a if exists drop column feature. Coming up with a stored procedure that accepts 
* tablename and column name as inputs. Currently the reusable procedur has some issues. 
* Till then please use these two stored procs as a work around to run the Kettle ETL flows
* So came up with a last minute stored procedure 
* to achieve it. The following procedure check if column score exists in the intermediate GRADES table
* and deletes the column.
********************************************************************************************************/

DROP PROCEDURE IF EXISTS DropScoreColumn;
delimiter //
CREATE PROCEDURE DropScoreColumn() BEGIN
		IF EXISTS( SELECT * 
				   FROM information_schema.COLUMNS
				   WHERE table_name = 'GRADES' AND column_name = 'score'
		)THEN
			ALTER TABLE GRADES DROP COLUMN score;
		END IF;
END;
//
delimiter ';'

/* testing stored procedure */
call  DropScoreColumn();


/******************************************************************************
* Procedure to check and drop columns COURSE, COURSENUM and ONLINE_FLAG alter
* from the Course Enrollment table
*******************************************************************************/

DROP PROCEDURE IF EXISTS DropCourseTableColumns;
DELIMITER //
CREATE PROCEDURE DropCourseTableColumns() BEGIN 

	IF EXISTS (SELECT *
			   FROM information_schema.COLUMNS
			   WHERE TABLE_NAME = 'U11FCourse' AND COLUMN_NAME = 'COURSE') THEN
				BEGIN
					ALTER TABLE U11FCourse
					DROP COLUMN `COURSE`; 
				END; END IF;  
	IF EXISTS (SELECT *
				FROM information_schema.COLUMNS
				WHERE TABLE_NAME = 'U11FCourse' AND COLUMN_NAME = 'ONLINE_FLAG') THEN
				BEGIN
					ALTER TABLE U11FCourse
					DROP COLUMN `ONLINE_FLAG`;
				END; END IF; 
	IF EXISTS (SELECT *
				FROM information_schema.COLUMNS
				WHERE TABLE_NAME = 'U11FCourse' AND COLUMN_NAME = 'COURSENUM') THEN
				BEGIN
					ALTER TABLE U11FCourse
					DROP COLUMN `COURSENUM`; 
				END; END IF; 
END; 
// DELIMITER ';' 

/* testing the stored procedure */
 call DropCourseTableColumns();


/**********************************************************************************************
* RESUALBLE STORED PROC - STILL IN PROGRESS
* Creating a Reusable stored procedure which accepts table name and column name as parameters.
* The query is built using PREPARED STATEMENTS. 
* 
* Issue to be fixed - Using IF EXISTS in the prepared query is throwing an error. 
* Will re-visit and update it.Till then use the above two queries to run the Kettle Job
***********************************************************************************************/ 

DROP PROCEDURE IF EXISTS sp_DeleteCoulumnsIfExists;
DELIMITER //
CREATE PROCEDURE sp_DeleteCoulumnsIfExists (IN tableName VARCHAR(200) , IN columnName VARCHAR(200)) 
BEGIN 
	
	set @query1  = CONCAT('IF EXISTS (\nSELECT * FROM  information_schema.COLUMNS WHERE TABLE_NAME = \'', tableName,'\' AND COLUMN_NAME = \'', columnName, '\'\n) THEN \n ALTER TABLE ',tableName, '\n DROP COLUMN ', columnName,'; \nEND IF;');
	
	select @query1;
	PREPARE statement from  @query1;
	EXECUTE statement;
	DEALLOCATE PREPARE statement;

END; 
// DELIMITER ';' 

/* testing the stored procedure*/
call sp_DeleteCoulumnsIfExists('distbiotable' , 'PERCENTILE' );






