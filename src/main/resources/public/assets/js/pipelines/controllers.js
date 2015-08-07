(function(angular, JSON) {
	'use strict';
	angular
	.module('LAP')
	.controller('PipelinesController',

		function PipelinesController($scope, $state, $translate, $translatePartialLoader, pipelines, PipelineDataService, Notification) {
		    $translatePartialLoader.addPart('pipelines');
		    $translate.refresh();
		    
			$scope.pipelines = pipelines;
			$scope.submitted = false;

			$scope.selectPipeline = function (pipeline) {$scope.selectedPipeline = pipeline;};
			
			$scope.startPipeline = function (type) {
				$scope.submitted = true;

				PipelineDataService
				.runPipeline(type)
				.then (
				    function (data) {
				    	if (data) {
				    		Notification.success('Pipeline complete');
				    	}
				    	else {
				    		Notification.error('Pipeline failed');
				    	}
				    	
				    	$scope.submitted = false;
				    }
				);
			};
		}
	
	);
})(angular, JSON);