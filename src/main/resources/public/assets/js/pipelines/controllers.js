(function(angular, JSON) {
	'use strict';
	angular
	.module('LAP')
	.controller('PipelinesController',

		function PipelinesController($scope, $state, $translate, $translatePartialLoader, pipelines) {
		    $translatePartialLoader.addPart('pipelines');
		    $translate.refresh();
		    
			$scope.pipelines = pipelines;
		}
	
	);
})(angular, JSON);