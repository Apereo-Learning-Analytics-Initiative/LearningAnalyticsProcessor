package org.apereo.lap.kettle;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "kettle")
public class KettleProperties {
    private List<String> databaseConnectionNames = new ArrayList<>();

    public List<String> getDatabaseConnectionNames() {
        return databaseConnectionNames;
    }
}
