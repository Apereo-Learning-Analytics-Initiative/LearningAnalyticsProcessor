/**
 * Copyright 2013 Unicon (R) Licensed under the
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
 */
package org.apereo.lap.services.notification.handlers;

import org.apereo.lap.services.notification.NotificationService;

/**
 * Handles a specific notification type
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public interface NotificationHandler {

    /**
     * @return the name of this type of notification (e.g. EMAIL)
     */
    String getNotificationType();

    /**
     * Sends a notification after pipeline processing is complete
     * @param type
     * @param message
     */
    public void sendNotification(NotificationService.NotificationLevel type, String message);

}
