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
package org.apereo.lap.services.input.handlers;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Handles the input processing for a single input data source
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public abstract class BaseInputHandler implements InputHandler {

    JdbcTemplate jdbc;
    public BaseInputHandler(JdbcTemplate jdbcTemplate) {
        assert jdbcTemplate != null;
        this.jdbc = jdbcTemplate;
    }

    @Override
    public JdbcTemplate getTempDatabase() {
        return jdbc;
    }

    // UTILITIES

    /**
     * Trim all the values in this array to null if they are empty
     * @param strings an array of strings
     */
    public static void trimStringArrayToNull(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            strings[i] = StringUtils.trimToNull(strings[i]);
        }
    }

    /**
     * Attempt to verify integer
     * @param string string to verify as integer
     * @param min OPTIONAL minimum value
     * @param max OPTIONAL maximum value
     * @param cannotBeBlank true if this must be a number and cannot be null
     * @param name the name of the string (the field), for error messages
     * @return the integer OR null (if allowed)
     * @throws java.lang.IllegalArgumentException is the string fails validation
     */
    public static Integer parseInt(String string, Integer min, Integer max, boolean cannotBeBlank, String name) {
        boolean blank = StringUtils.isBlank(string);
        if (cannotBeBlank && blank) {
            throw new IllegalArgumentException(name+" (" + string + ") cannot be blank");
        } else if (blank) {
            // can be blank, so return null
            return null;
        }
        Integer num;
        try {
            num = Integer.parseInt(string);
            if (min != null && num < min) {
                throw new IllegalArgumentException(name+" integer ("+num+") is less than the minimum ("+min+")");
            } else if (max != null && num > max) {
                throw new IllegalArgumentException(name+" integer ("+num+") is greater than the maximum ("+max+")");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name+" ("+string+") must be an integer: "+e);
        }
        return num;
    }

    public static Float parseFloat(String string, Float min, Float max, boolean cannotBeBlank, String name) {
        boolean blank = StringUtils.isBlank(string);
        if (cannotBeBlank && blank) {
            throw new IllegalArgumentException(name+" (" + string + ") cannot be blank");
        } else if (blank) {
            // can be blank, so return null
            return null;
        }
        Float num;
        try {
            num = Float.parseFloat(string);
            if (min != null && num < min) {
                throw new IllegalArgumentException(name+" number ("+num+") is less than the minimum ("+min+")");
            } else if (max != null && num > max) {
                throw new IllegalArgumentException(name+" number ("+num+") is greater than the maximum ("+max+")");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name+" ("+string+") must be a float: "+e);
        }
        return num;
    }

    public static Boolean parseBoolean(String string, boolean cannotBeBlank, String name) {
        boolean blank = StringUtils.isBlank(string);
        if (cannotBeBlank && blank) {
            throw new IllegalArgumentException(name+" (" + string + ") cannot be blank");
        } else if (blank) {
            // can be blank, so return null
            return null;
        }
        Boolean bool;
        if ("T".equalsIgnoreCase(string)
                || "Y".equalsIgnoreCase(string)
                || "YES".equalsIgnoreCase(string)
                ) {
            bool = Boolean.TRUE;
        } else {
            bool = Boolean.parseBoolean(string);
        }
        return bool;
    }

    public static String parseString(String string, String[] valid, boolean cannotBeBlank, String name) {
        boolean blank = StringUtils.isBlank(string);
        if (cannotBeBlank && blank) {
            throw new IllegalArgumentException(name + " ("+string+") cannot be blank");
        } else if (!blank) {
            if (valid != null && valid.length > 0) {
                if (!ArrayUtils.contains(valid, string)) {
                    // invalid if not in the valid set
                    throw new IllegalArgumentException(name + " ("+string+") must be in the valid set: "+ArrayUtils.toString(valid));
                }
            }
        }
        return string;
    }

    static String[] dateFormats = new String[]{"yyyy-MM-dd'T'HH:mm:ssZZ","yyyy-MM-dd'T'HH:mm:ss","yyyy-MM-dd'T'HH:mm","yyyy-MM-dd"};
    public static Timestamp parseDateTime(String string, boolean cannotBeBlank, String name) {
        Timestamp ts = null; // blank ts is null
        boolean blank = StringUtils.isBlank(string);
        if (cannotBeBlank && blank) {
            throw new IllegalArgumentException(name + " ("+string+") cannot be blank");
        } else if (!blank) {
            Date d;
            try {
                d = DateUtils.parseDate(string, dateFormats);
            } catch (ParseException e) {
                throw new IllegalArgumentException(name + " ("+string+") cannot be parsed into a Timestamp/Date, format should be ISO-8601 (yyyy-MM-dd'T'HH:mm, e.g. 2014-02-03T12:34)");
            }
            ts = new Timestamp(d.getTime());
        }
        return ts;
    }

}
