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
package org.apereo.lap.test;

import org.apereo.lap.LearningAnalyticsProcessor;
import org.apereo.lap.security.SecurityConfig;
import org.apereo.lap.services.storage.h2.H2PersistentConfig;
import org.apereo.lap.services.storage.h2.H2TempConfig;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {LearningAnalyticsProcessor.class, SecurityConfig.class, H2PersistentConfig.class, H2TempConfig.class} )
@Category(UnitTests.class)
public abstract class AbstractTest {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
}
