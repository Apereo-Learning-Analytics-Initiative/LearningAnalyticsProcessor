(function(angular, JSON) {
  'use strict';
angular
.module('LAP')
.service('RunDataService', function($log, $http) {
    return {
      getRuns : function(page, size) {
			
        var p = page || 0;
        var s = size || 10;
	
		var url = '/api/runs?page='+p+'&size='+s;
		var promise = $http({
		  method : 'GET',
		  url : url,
		  headers : {
		    'Content-Type' : 'application/json'
		  }
		}).then(function(response) {
		  if (response && response.data) {
		    return response.data.content;
		  }
		  $log.debug(response);
		  return null;
		}, function(error) {
		  $log.error(error);
		  return null;
		});
		return promise;
      }
	}
});

})(angular, JSON);