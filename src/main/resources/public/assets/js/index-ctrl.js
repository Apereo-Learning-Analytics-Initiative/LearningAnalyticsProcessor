'use strict';

angular
.module('LAP')
.controller('IndexCtrl',

function MasterCtrl($scope, $state, $translate, $translatePartialLoader, $http, runs, SessionService) {
    
  $translatePartialLoader.addPart('overview');
  $translate.refresh();
    
  $scope.runs = runs;

  $scope.logout = function() {
	  // TODO move to SessionService
          $http.post('logout', {}).success(function() {
            $SessionService.invalidate();
            $state.go('login');
          }).error(function(data) {
            $rootScope.authenticated = false;
          });
      };
      
  $scope.login = function() {
	  $SessionService.invalidate();
      $state.go("login");
  }
});