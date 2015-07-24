(function(angular, JSON) {
	'use strict';
	angular
	.module('LAP')
	.controller('PipelinesController',

		function PipelinesController($scope, $state, $translate, $translatePartialLoader, pipelines, PipelineDataService) {
		    $translatePartialLoader.addPart('pipelines');
		    $translate.refresh();
		    
			$scope.pipelines = pipelines;
			
			$scope.selectPipeline = function (pipeline) {$scope.selectedPipeline = pipeline;};
			
			$scope.startPipeline = function (type) {
				PipelineDataService
				.runPipeline(type)
				.then (
				    function (data) {
				    	console.log(data);
				    }
				);
			};
		}
	
	);
})(angular, JSON);