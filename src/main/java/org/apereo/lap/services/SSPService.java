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
package org.apereo.lap.services;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apereo.lap.services.pipeline.KettleDataPipelineProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class SSPService {
	static final Logger logger = LoggerFactory.getLogger(KettleDataPipelineProcessor.class);

	public String createEarlyAlert(String baseUrl, String userId) throws IOException {
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(baseUrl + "/ssp/api/1/person/" + userId + "/earlyAlert");

		StringRequestEntity requestEntity = new StringRequestEntity(
			    "{}",
			    "application/json",
			    "UTF-8");

        post.setRequestEntity(requestEntity);

		client.executeMethod(post);
	    String responseFromPost = post.getResponseBodyAsString();
	    
	    logger.debug(responseFromPost);
	    
	    post.releaseConnection();
	    return responseFromPost;
	}
	
}
