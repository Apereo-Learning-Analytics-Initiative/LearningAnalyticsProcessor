'use strict';

angular
.module('LAP')
.controller('LoginCtrl',

function LoginCtrl($rootScope, $scope, $state, $translate, $translatePartialLoader, AuthenticationService, isMultiTenant) {
  $translatePartialLoader.addPart('login');
  $translate.refresh();
    
  $scope.isMultiTenant = isMultiTenant;
  $scope.credentials = {};
  $scope.login = function() {
    AuthenticationService.authenticated($scope.credentials)
      .then(
        function (data) {
          $rootScope.authenticated = data;
          $state.go('index');
          return;
        },
        function (error) {
        	$rootScope.authenticated = false;
        	$scope.error = true;
        }
      );
  };
});