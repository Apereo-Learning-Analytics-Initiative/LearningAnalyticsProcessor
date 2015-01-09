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
<link rel="shortcut icon" href="<c:url value="resources/favicon.ico"/>"
	type="image/x-icon">
<link rel="icon" href="<c:url value="resources/favicon.ico"/>"
	type="image/x-icon">
<title>LAP Configuration</title>

<link href="resources/bootstrap/css/bootstrap.min.css" rel="stylesheet"
	type="text/css" media="screen" />
<link href="resources/bootstrap/css/bootstrap-theme.min.css"
	rel="stylesheet" type="text/css" media="screen" />
<link href="resources/css/screen.css" rel="stylesheet" type="text/css"
	media="screen" />

<script src="resources/jquery/jquery-1.11.1.min.js"></script>
<script src="resources/bootstrap/js/bootstrap.min.js"></script>
<script src="resources/js/lap.js"></script>
<script src="resources/js/lap.configuration.js"></script>
</head>
<body>
	<div class="container">
		<div class="row">
			<div class="span12">
				<h1>Configuration</h1>

				<form action="/config/save" method="POST"
					enctype="application/x-www-form-urlencoded">

					<div class="form-group">
						<label>SSP Base URL</label> 
						<input type="text" name="sspBaseUrl" class="form-control"
							id="sspBaseUrl" value="${configuration.sspBaseUrl}" />
					</div>
					<div class="form-group">
						<label>Risk Confidence Threshold</label> 
						<input type="text" class="form-control"
							name="riskConfidenceThreshold" id="riskConfidenceThreshold"
							value="${configuration.riskConfidenceThreshold}" />
					</div>
					<div class="form-group">
						<div class="checkbox">
						<label>
							<input type="checkbox" name="active" 
								id="active" checked="${configuration.active ? 'checked' : ''}" /> Active</label> 
						</div>
					</div>

					<button type="button" class="btn"
						onclick="lap.configuration.save(this); return false;">Save</button>

				</form>
			</div>
		</div>
	</div>

</body>
</html>
