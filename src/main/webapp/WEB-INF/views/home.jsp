<%--

    Copyright 2013 Unicon (R) Licensed under the
    Educational Community License, Version 2.0 (the "License"); you may
    not use this file except in compliance with the License. You may
    obtain a copy of the License at

    http://www.osedu.org/licenses/ECL-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an "AS IS"
    BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
    or implied. See the License for the specific language governing
    permissions and limitations under the License.

--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Learning Analytics Processor Home">
    <meta name="author" content="Aaron Zeckoski">
    <link rel="shortcut icon" href="<c:url value="resources/favicon.ico"/>" type="image/x-icon">
    <link rel="icon" href="<c:url value="resources/favicon.ico"/>" type="image/x-icon">
    <title>LAP Home</title>

    <link href="resources/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css" media="screen" />
    <link href="resources/bootstrap/css/bootstrap-theme.min.css" rel="stylesheet" type="text/css" media="screen" />
    <link href="resources/css/screen.css" rel="stylesheet" type="text/css" media="screen" />

    <script src="resources/jquery/jquery-1.11.1.min.js"></script>
    <script src="resources/bootstrap/js/bootstrap.min.js"></script>
    <script src="resources/js/lap.js"></script>
</head>
<body>
  <h1>Learning Analytics Processor</h1>

  <div>This page is just informational for now.
  More information about this project is available at <a href="https://confluence.sakaiproject.org/display/LAI/Apereo+Learning+Analytics+Processor">Apereo Learning Analytics Processor wiki</a>.
  </div>

  <h2>Current <a href="pipeline/">pipelines</a></h2>
  <ol>
      <c:forEach items="${pipelines}" var="pipeline">
          <li>
              <strong><a href="pipeline/${pipeline.type}">${pipeline.type}:</a></strong> ${pipeline.name}
              <form action="/pipeline/${pipeline.type}" method="post" style="display: inline;">
                  <input type="submit" name="start-pipeline-${pipeline.type}" value="Run" />
              </form>
          </li>
      </c:forEach>
  </ol>

  <h2>Current pipeline processors</h2>
  <ol>
      <c:forEach items="${processors}" var="processor">
      <li>${processor.processorType}</li>
      </c:forEach>
  </ol>

  <h2>Processing directories</h2>
  <div>
      <div><strong>Input:</strong> ${inputDir}</div>
      <div><strong>Output:</strong> ${outputDir}</div>
      <div><strong>Pipelines:</strong> ${pipelinesDir}</div>
  </div>

  <h2>Databases</h2>
  <div>
      <div><strong>Temporary:</strong> ${temporaryDB}</div>
      <div><strong>Persistent:</strong> ${persistentDB}</div>
  </div>

</body>
</html>
