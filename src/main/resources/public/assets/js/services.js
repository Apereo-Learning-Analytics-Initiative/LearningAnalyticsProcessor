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
})
.service('AuthenticationService', function($log, $http) {
    return {
      authenticated : function(credentials) {
		
        var headers = {'Content-Type' : 'application/json'};
        
        if (credentials) {
        	headers['Authorization'] = "Basic " + btoa(credentials.username + ":" + credentials.password);
        	if (credentials.tenant) {
        	  headers['X-LAP-TENANT'] = credentials.tenant;
        	}        	
        }
        
		var promise = $http({
			  method : 'GET',
			  url : '/user',
			  headers : headers
			}).then(function(response) {
			  if (response && response.data
					  && response.data.authenticated && response.data.name) {
			    return true;
			  }
			  $log.debug(response);
			  return false;
			}, function(error) {
			  $log.error(error);
			  return false;
			});
			return promise;
      }
	}
})
.service('FeatureFlagService', function($log, $http) {
    return {
      isFeatureActive : function(featureKey) {
        var headers = {'Content-Type' : 'application/json'};
        
		var promise = $http({
			  method : 'GET',
			  url : '/features/' + featureKey,
			  headers : headers
			})
			.then(function(response) {
			  if (response && response.data) {
				var val = response.data[featureKey];				
				$log.log(val)
				return (val && val.toLowerCase() === 'true');
			  }
			  $log.debug(response);
			  return false;
			}, 
			function(error) {
			  $log.error(error);
			  return false;
			});
			return promise;
      }
	}
});


})(angular, JSON);