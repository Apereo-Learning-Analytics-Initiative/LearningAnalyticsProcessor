'use strict';

angular
.module('LAP')
.controller('MasterCtrl',

function MasterCtrl($scope, $state, $translate, $translatePartialLoader, runs) {
	
  $translatePartialLoader.addPart('overview');
  $translate.refresh();
    
  $scope.runs = runs;
});