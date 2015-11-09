/*******************************************************************************
 * Copyright (c) 2015 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *******************************************************************************/
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

  .service('LocaleService', function ($translate, LOCALES, $rootScope) {
    'use strict';
    // VARS
    var localesObj = LOCALES.locales;

    // locales and locales display names
    var _LOCALES = Object.keys(localesObj);
    if (!_LOCALES || _LOCALES.length === 0) {
      //console.error('There are no _LOCALES provided');
    }
    var _LOCALES_DISPLAY_NAMES = [];
    _LOCALES.forEach(function (locale) {
      _LOCALES_DISPLAY_NAMES.push(localesObj[locale]);
    });

    var currentLocale = $translate.proposedLanguage();// because of async loading
    // METHODS
    var checkLocaleIsValid = function (locale) {
      return _LOCALES.indexOf(locale) !== -1;
    };

    var setLocale = function (locale) {
      if (!checkLocaleIsValid(locale)) {
        //console.error('Locale name "' + locale + '" is invalid');
        return;
      }
      startLoadingAnimation();
      currentLocale = locale;
      $translate.use(locale);
    };

    /**
     * Stop application loading animation when translations are loaded
     */
    var $html = angular.element('html');
    var LOADING_CLASS = 'app-loading';

    function startLoadingAnimation() {
      $html.addClass(LOADING_CLASS);
    }

    function stopLoadingAnimation() {
      $html.removeClass(LOADING_CLASS);
    }

    // EVENTS
    $rootScope.$on('$translateChangeSuccess', function (event, data) {
      document.documentElement.setAttribute('lang', data.language);// sets "lang" attribute to html
    });

    $rootScope.$on('$localeChangeSuccess', function () {
      stopLoadingAnimation();
    });

    return {
      getLocaleDisplayName: function () {
        return localesObj[currentLocale];
      },
      setLocaleByDisplayName: function (localeDisplayName) {
        setLocale(
          _LOCALES[
            _LOCALES_DISPLAY_NAMES.indexOf(localeDisplayName)// get locale index
            ]
        );
      },
      getLocalesDisplayNames: function () {
        return _LOCALES_DISPLAY_NAMES;
      },
      getLocaleForDisplayName: function (localeDisplayName) {
        return _LOCALES[_LOCALES_DISPLAY_NAMES.indexOf(localeDisplayName)];
      }
    };
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
		  },
		  logout : function () {
			  var promise = $http({
				  method : 'POST',
				  url : '/logout',
				  headers : { 'Content-Type': 'application/json' }
				})
				.then(function(response) {
				  authenticated = false;
				  authorities = null;
				  return response.data;
				}, 
				function(error) {
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