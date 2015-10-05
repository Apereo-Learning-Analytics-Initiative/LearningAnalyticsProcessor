'use strict';

angular
.module('LAP')
.controller('IndexCtrl',

function MasterCtrl($scope, $state, $translate, $translatePartialLoader, $http, runs, SessionService) {
  
  if(!SessionService.isAuthenticated()){
    $state.go("login");
  }

  $translatePartialLoader.addPart('overview');
  $translate.refresh();
    
  $scope.runs = runs;
  $scope.isAuthenticated = SessionService.isAuthenticated();
  $scope.logout = function() {
	  SessionService.logout()
	    .then( function(data) {
	        $state.go('login', {loggedOutMessage:'USER_INITIATED'});
	        return;
	    },
	    function (error) {
	        $state.go('login', {loggedOutMessage:'USER_INITIATED'});
	        return;
	    }
	  );
  }

});
