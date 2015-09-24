package org.apereo.lap.test;

import org.apereo.lap.LearningAnalyticsProcessor;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@ActiveProfiles("mongo-multitenant")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LearningAnalyticsProcessor.class)
@Category(MongoUnitTests.class) //Want to be able to remove mongo tests with out affecting other Unit/Integration tests
@WebAppConfiguration
public abstract class MongoUnitTests {

    @Before
    public void setup() {
        //Keeping with test setup.
        //Leave here for future unit tests.
    }

}
