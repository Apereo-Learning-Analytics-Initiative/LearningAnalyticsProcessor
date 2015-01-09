/*
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
/**
 * Custom LAP javascript goes in here,
 * Sample below shows how to properly namespace it
 * -AZ
 */
(function( lap, $, undefined ) {

    lap.call = function (options) {

        var def = {
            url: '',
            type: "GET",
            data: undefined,
            dataType: '',
            contentType: "application/json",
            success: null,
            error: null
        };

        $.extend(def, options);
    
        $.ajax({
            url: def.url,
                data: (def.data ? JSON.stringify(def.data) : ''),
                type: def.type,
                dataType: def.dataType,
                cache: false,
                contentType: def.contentType,
                statusCode: {
                    200: function(data) {

                        try {
                            var json = lap.parseResponse(data);

                            if ('object' === typeof json) {
                                if (json.errors.length > 0) {

                                    $.each(json.errors, function() {
                                        alert(this);
                                    });

                                    if (typeof (def.error) == 'function') {
                                        def.error(json);
                                    }
                                } else {
                                    def.success(data);
                                }
                            } else {
                                def.success(data);
                            }

                        } catch (e) {
                            def.success(data);
                        }
                    },
                    401: function(response) {

                    },
                    404: function(response) {

                    },
                    500: function(response) {
                    	if(typeof(def.error) == 'function')
                    	{
        					def.error(response);
                    	}
                    }
                },
                complete: function(xhr) {


                }
            });
    };
    
    lap.parseResponse = function (data) {

        var json = data;
        if (typeof data === "String" || typeof data == "string") {
            json = JSON.parse(data);
        }
        return json;
    };

}( window.lap = window.lap || {}, jQuery ));
