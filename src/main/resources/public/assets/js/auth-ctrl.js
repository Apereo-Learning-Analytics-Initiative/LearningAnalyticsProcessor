'use strict';

angular
.module('LAP')
.controller('AuthCtrl',

function AuthCtrl($rootScope, $scope, $state, AuthenticationService) {
  if (!$rootScope.authenticated) {
	  AuthenticationService
	  .authenticated()
	  .then(
		function (data) {
		  if (!data) {
		    $state.go('login');
		    return;
		  }
		  return;
		},
		function (error) {
			$state.go('login');
			return;
		}
	  );
  }
  
});