'use strict';

angular
.module('LAP')
.controller('OverviewCtrl',

function OverviewCtrl($scope, $state, $translate, $translatePartialLoader, runs) {
    $translatePartialLoader.addPart('overview');
    $translate.refresh();
    
    $scope.runs = runs;
});