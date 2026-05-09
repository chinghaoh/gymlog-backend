package com.gymlog.exercise;

import com.gymlog.common.SearchCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class ExerciseSpecification implements Specification<Exercise> {

    private final SearchCriteria criteria;

    public ExerciseSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Exercise> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder cb) {

        if (criteria.getOperation().equalsIgnoreCase("=")) {
            if (root.get(criteria.getKey()).getJavaType() == String.class) {
                return cb.equal(
                        cb.lower(root.get(criteria.getKey())),
                        criteria.getValue().toString().toLowerCase()
                );
            } else {
                return cb.equal(
                        root.get(criteria.getKey()),
                        criteria.getValue()
                );
            }
        }

        if (criteria.getOperation().equalsIgnoreCase(">")) {
            return cb.greaterThanOrEqualTo(
                    root.get(criteria.getKey()),
                    criteria.getValue().toString()
            );
        }

        if (criteria.getOperation().equalsIgnoreCase("<")) {
            return cb.lessThanOrEqualTo(
                    root.get(criteria.getKey()),
                    criteria.getValue().toString()
            );
        }

        return null;
    }
}