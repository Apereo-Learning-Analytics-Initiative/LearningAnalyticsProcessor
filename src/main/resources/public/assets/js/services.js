(function(angular, JSON) {
  'use strict';
angular
.module('LAP')
.service('RunDataService', function($log, $http) {
    return {
      getRuns : function(page, size) {
			
        var p = page || 0;
        var s = size || 10;
	
		var url = '/history?page='+p+'&size='+s;
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
	.service('SessionService', function($log, $http) {
		
		var ROLE_ADMIN = 'ROLE_ADMIN';
		
		var authenticated = false;
		var currentUser = null;
		var authorities = null;
		
		var checkRole = function(role) {
		  var hasRole = false;
		  if (authorities) {
			$log.debug(authorities);
			var values = _.map(authorities, function(authority){ return authority['authority'];});
			$log.debug(values);
            var indexOf = _.indexOf(values,role);
            $log.debug(indexOf);
            if (indexOf >= 0) {
            	hasRole = true;
            }
		  }
		  return hasRole;
		}
		
		return {
		  isAuthenticated : function () {
		    return authenticated;
		  },
		  invalidate : function () {
			authenticated = false;
		  },
		  hasAdminRole : function () {
			  return checkRole(ROLE_ADMIN);
		  },
		  authenticate : function(credentials) {
						  
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
			  headers : headers})
			  .then(function(response) {
			    if (response && response.data
			      && response.data.authenticated 
			      && response.data.name) {
			    	$log.debug(response);
			    	authenticated = response.data.authenticated;
			    	authorities = response.data.authorities;
			    	
					return authenticated;
			    }
			    return false;
			  }, 
			  function(error) {
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