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