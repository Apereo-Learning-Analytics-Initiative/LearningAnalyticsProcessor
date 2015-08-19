package org.apereo.lap.services.storage.mongo.tests;

import org.apereo.lap.LearningAnalyticsProcessor;
import org.apereo.lap.services.storage.mongo.MongoMultiTenantConfig;
import org.apereo.lap.test.MongoUnitTests;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ActiveProfiles("mongo-multitenant")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {LearningAnalyticsProcessor.class, MongoMultiTenantConfig.class})
@Category(MongoUnitTests.class)
public abstract class MongoTests {

    @Before
    public void setup() {
        //Keeping with test setup.
        //Leave here for future unit tests.
    }

}
