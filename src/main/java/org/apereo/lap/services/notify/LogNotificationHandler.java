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
package org.apereo.lap.services.notify;

import org.apereo.lap.services.ConfigurationService;
import org.apereo.lap.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles specific notifications by writing them to the logs
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class LogNotificationHandler implements NotificationHandler {

    private static final Logger logger = LoggerFactory.getLogger(LogNotificationHandler.class);

    @Autowired
    ConfigurationService configuration;

    @Override
    public String getNotificationType() {
        return "Logger";
    }

    @Override
    public void sendNotification(NotificationService.NotificationLevel type, String message) {
        if (NotificationService.NotificationLevel.CRITICAL == type) {
            logger.error(message);
        } else {
            logger.info(message);
        }
    }
}
