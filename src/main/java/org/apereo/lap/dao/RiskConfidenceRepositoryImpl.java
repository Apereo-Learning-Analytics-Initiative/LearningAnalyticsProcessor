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
package org.apereo.lap.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apereo.lap.dao.model.RiskConfidence;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class RiskConfidenceRepositoryImpl implements RiskConfidenceRepositoryExtension  {
	
    @PersistenceContext
    public EntityManager entityManager;
	
	public List<RiskConfidence> findByUserCourseDate(String user, String course) {
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<RiskConfidence> criteria = builder.createQuery(RiskConfidence.class);
		Root<RiskConfidence> root = criteria.from(RiskConfidence.class);
		EntityType<RiskConfidence> type = entityManager.getMetamodel().entity(RiskConfidence.class);
		criteria.orderBy(builder.desc(root.get("dateCreated")));
		
		List<RiskConfidence> lastRiskConfidences = entityManager.createQuery( criteria )
				.setFirstResult(0) 
		         .setMaxResults(1)
		         .getResultList();
		
		if(lastRiskConfidences.size() == 0)
			return new ArrayList<RiskConfidence>();
		
		RiskConfidence lastRickConfidence = lastRiskConfidences.get(0);
		
		if(StringUtils.isEmpty(user) && StringUtils.isEmpty(course))
			return new ArrayList<RiskConfidence>();

		builder = entityManager.getCriteriaBuilder();
		criteria = builder.createQuery(RiskConfidence.class);
		root = criteria.from(RiskConfidence.class);
		type = entityManager.getMetamodel().entity(RiskConfidence.class);
		
		Predicate groupPredicate = builder.equal(root.get("groupId"), lastRickConfidence.getGroupId());
				
		if(!StringUtils.isEmpty(user))
			criteria.where(groupPredicate, builder.like(builder.lower(root.get(type.getDeclaredSingularAttribute("alternativeId", String.class))), "%" + user.toLowerCase() + "%" ) );
		
		if(!StringUtils.isEmpty(course))
			criteria.where(groupPredicate, builder.like(builder.lower(root.get(type.getDeclaredSingularAttribute("courseId", String.class))), "%" + course.toLowerCase() + "%" ) );
		
		return entityManager.createQuery( criteria ).getResultList();
	}
}
