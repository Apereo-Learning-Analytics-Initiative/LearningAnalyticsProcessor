package org.apereo.lap.services.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "datasource")
public class DatasourceProperties {
    public static class JdbcInfo {
        private String username;
        private String password;
        private String driverClassName;
        private String url;
        private String dialect;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDialect() {
            return dialect;
        }

        public void setDialect(String dialect) {
            this.dialect = dialect;
        }
    }

    private JdbcInfo temp = new JdbcInfo();
    private JdbcInfo persistent = new JdbcInfo();

    public JdbcInfo getTemp() {
        return temp;
    }

    public JdbcInfo getPersistent() {
        return persistent;
    }
}
