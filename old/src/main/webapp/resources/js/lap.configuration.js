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

    function Configuration() {
    	var self = this;
    	
    	self.save = function(button) {

    		$form = $(button).parents('form');
    		
    		lap.call({
	            type: $form.attr('method'),
	            dataType: 'json',
	            url: $form.attr('action'),
	            data: $form.serializeObject(),
	            success: function (response) {
	            	alert('Saved successfully!');
	            }
	        });
    		
    		return false;
    	};
	}

	lap.configuration = new Configuration();
    
}( window.lap = window.lap || {}, jQuery ));

$.fn.serializeObject = function()
{
    var o = {};
    var value = '';
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            value = this.value || '';

            if(value == "on")
            {
            	value = 'true';
            }

            o[this.name].push(value);
        } else {
        	value = this.value || '';
        	
        	 if(value == "on")
             {
             	value = 'true';
             }

            o[this.name] = value;
        }
    });
    return o;
};
