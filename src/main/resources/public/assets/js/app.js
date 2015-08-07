'use strict';

angular
.module('LAP', ['ui.bootstrap', 'ui.router', 'ngCookies', 'pascalprecht.translate', 'ui-notification']);

angular
.module('LAP')
.config(function(NotificationProvider) {
	NotificationProvider.setOptions({
	    delay: 10000,
	    startTop: 20,
	    startRight: 10,
	    verticalSpacing: 20,
	    horizontalSpacing: 20,
	    positionX: 'left',
	    positionY: 'bottom'
	});
 })
.config(function($translateProvider, $translatePartialLoaderProvider) {
    $translateProvider.useLoader('$translatePartialLoader', {
        urlTemplate: '/assets/translations/{lang}/{part}.json'
      });

    $translateProvider.preferredLanguage('en_us');
})
.provider('requestNotification', function() {
	// This is where we keep subscribed listeners
	var onRequestStartedListeners = [];
	var onRequestEndedListeners = [];

	// This is a utility to easily increment the request count
	var count = 0;
	var requestCounter = {
		increment : function() {
			count++;
		},
		decrement : function() {
			if (count > 0)
				count--;
		},
		getCount : function() {
			return count;
		}
	};
	// Subscribe to be notified when request starts
	this.subscribeOnRequestStarted = function(listener) {
		onRequestStartedListeners.push(listener);
	};

	// Tell the provider, that the request has started.
	this.fireRequestStarted = function(request) {
		// Increment the request count
		requestCounter.increment();
		// run each subscribed listener
		angular.forEach(onRequestStartedListeners, function(listener) {
			// call the listener with request argument
			listener(request);
		});
		return request;
	};

	// this is a complete analogy to the Request START
	this.subscribeOnRequestEnded = function(listener) {
		onRequestEndedListeners.push(listener);
	};

	this.fireRequestEnded = function() {
		requestCounter.decrement();
		var passedArgs = arguments;
		angular.forEach(onRequestEndedListeners, function(listener) {
			listener.apply(this, passedArgs);
		});
		return arguments[0];
	};

	this.getRequestCount = requestCounter.getCount;

	// This will be returned as a service
	this.$get = function() {
		var that = this;
		// just pass all the
		return {
			subscribeOnRequestStarted : that.subscribeOnRequestStarted,
			subscribeOnRequestEnded : that.subscribeOnRequestEnded,
			fireRequestEnded : that.fireRequestEnded,
			fireRequestStarted : that.fireRequestStarted,
			getRequestCount : that.getRequestCount
		};
	};
})
.config(function($httpProvider, requestNotificationProvider) {
	$httpProvider.defaults.transformRequest.push(function(data) {
		requestNotificationProvider.fireRequestStarted(data);
		return data;
	});

	$httpProvider.defaults.transformResponse.push(function(data) {
		requestNotificationProvider.fireRequestEnded(data);
		return data;
	});
})
.config(function($httpProvider){
	
	var logsOutUserOn401 = ['$q','$location','$log','$cookieStore',
		function($q, $location, $log, $cookieStore) {
			var success = function(response) {
				return response;
			};
	
			var error = function(response) {
				$log.log('error response');
				$log.log(response)
				if (response.status === 401) {
	
					var requestedurl = $location.path();
	
					if (requestedurl != '/') {
						$cookieStore.put('origurl',requestedurl);
						$log.info("Set origurl cookie to: "	+ $cookieStore.get('origurl'));
					}
	
					$log.info('401 http status code - user should have been routed to /, requested url is: '+ requestedurl);
					// redirect them back to login page
					$location.path('/login');
	
					return $q.reject(response);
				} else {
					return $q.reject(response);
				}
			};
	
			return function(promise) {
				return promise.then(success, error);
			};
		}];
	
	$httpProvider.interceptors.push(logsOutUserOn401);

	if (!$httpProvider.defaults.headers.get) {
		$httpProvider.defaults.headers.get = {};
	}
	$httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
	$httpProvider.defaults.headers.get['Pragma'] = 'no-cache';
})
.config(function($stateProvider, $urlRouterProvider, $locationProvider) {

	// For unmatched routes
	$urlRouterProvider.otherwise('/');
	
	// Application routes
	$stateProvider
	    .state('overview', {
	        url: '/',
	        controller: 'OverviewCtrl',
	        templateUrl: '/assets/templates/overview.html'
	    })
	    .state('tenants', {
	        url: '/tenants',
	        templateUrl: '/assets/templates/tenants.html'
	    })
	    .state('jobs', {
	        url: '/jobs',
	        templateUrl: '/assets/templates/jobs.html'
	    })
	    .state('pipelines', {
	        url: '/pipelines',
	        templateUrl: '/assets/templates/pipelines.html',
		    resolve:{
	    		pipelines : function ($stateParams, PipelineDataService) {
	    			return PipelineDataService.getPipelines();
	    		}	
	     	},
	     	controller: 'PipelinesController'	    
	     })
	    .state('rules', {
	        url: '/rules',
	        templateUrl: '/assets/templates/rules.html'
	    });	    
	$locationProvider.html5Mode(true);
});