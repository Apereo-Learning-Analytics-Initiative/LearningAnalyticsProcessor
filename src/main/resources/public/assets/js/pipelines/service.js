(function(angular, JSON) {
	'use strict';
	angular.module('LAP').service('PipelineDataService',
			function($log, $http) {
				return {
					getPipelines : function() {

						var url = '/api/pipelines';
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
					}
				}
			});

})(angular, JSON);