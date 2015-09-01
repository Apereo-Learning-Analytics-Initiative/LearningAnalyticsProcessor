'use strict';

angular
.module('LAP')
.controller('MasterCtrl',

function MasterCtrl($scope, $state, $translate, $translatePartialLoader, $http, $rootScope, runs) {
    
  $translatePartialLoader.addPart('overview');
  $translate.refresh();
    
  $scope.runs = runs;

  $scope.logout = function() {
          $http.post('logout', {}).success(function() {
            $rootScope.authenticated = false;
            $state.go('login');
          }).error(function(data) {
            $rootScope.authenticated = false;
          });
      };
      
  $scope.login = function() {
      $rootScope.authenticated = false;
      $state.go("login");
  }
});