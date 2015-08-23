(function(angular) {
	'use strict';

	angular.module('LAP').directive(
			'loadingWidget',
			function($timeout, requestNotification) {
				return {
					restrict : "AC",
					link : function(scope, element) {
						// hide the element initially
						element.hide();

						// subscribe to listen when a request starts
						requestNotification
								.subscribeOnRequestStarted(function() {
									// show the spinner!
									element.show();
								});

						requestNotification.subscribeOnRequestEnded(function() {
							// hide the spinner if there are no more pending
							// requests
							if (requestNotification.getRequestCount() === 0)
								element.hide();
						});
					}
				};
			});
})(angular);