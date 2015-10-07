/*******************************************************************************
 * Copyright (c) 2015 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *******************************************************************************/
(function(angular, JSON) {
	'use strict';
	angular.module('LAP').service('PipelineDataService',
			function($log, $http) {
				return {
					getPipelines : function() {

						var url = '/pipelines';
						var promise = $http({
							method : 'GET',
							url : url,
							headers : {
								'Content-Type' : 'application/json'
							}
						}).then(function(response) {
							if (response && response.data) {
								return response.data;
							}
							$log.debug(response);
							return null;
						}, function(error) {
							$log.error(error);
							return null;
						});
						return promise;
					},
					
					runPipeline : function (type) {
					  var url = '/pipelines/start/' + type;
					  var promise = $http({
						  method: 'POST',
						  url : url,
						  headers : {
						    'Content-Type' : 'application/json'
					      }
					  })
					  .then(
					      function (response) {
							if (response && response.data) {
								return response.data;
							}
							$log.debug(response);
							return false;
					      },
					      function (error) {
					    	$log.error(error);
					    	return false;
					      }
					  );
					  return promise;
					}

				}
			});

})(angular, JSON);