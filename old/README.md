Learning Analytics Processor
============================
An Open source Java based Learning Analytics Processor (LAP) which initially automates the Marist OAAI Student Early Alerts and Risk Assessment model

[Marist OAAI Student Early Alerts Homepage](https://confluence.sakaiproject.org/x/8aWCB)

Important links
---------------

[Project Wiki](https://confluence.sakaiproject.org/display/LAI/Apereo+Learning+Analytics+Processor)

[Issue Tracker (JIRA)](https://jira.sakaiproject.org/browse/LAI)

Build
-----
Build and execute for testing purposes:

    mvn clean install jetty:run

Debug
-----
If you want to run the app in debugging mode you can run it like this and attach a remote debugger to port 8000:

    mvnDebug clean install jetty:run

Deploy
------
Deploy the application war file into your tomcat or other servlet container 
after building it using this command (the war will be in the target directory):

    mvn clean install

Contacts
--------
- Sandeep Jayaprakash (sandeep.jayaprakash1 @ marist.edu)
- Aaron Zeckoski (http://tinyurl.com/azprofile)

Part of the [Apereo Learning Analytics Initiative (LAI)](https://confluence.sakaiproject.org/display/LAI)
