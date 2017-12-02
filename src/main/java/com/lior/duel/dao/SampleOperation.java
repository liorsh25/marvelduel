package com.lior.duel.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.Assert;

public class SampleOperation implements AggregationOperation  {

    private int size;

    public SampleOperation(int size) {
        Assert.isTrue(size > 0, " Size must be positive!");
        this.size = size;
    }

    public AggregationOperation setSize(int size) {
        Assert.isTrue(size > 0, " Size must be positive!");
        this.size = size;
        return this;
    }

    @Override
    public DBObject toDBObject(AggregationOperationContext context) {
        return new BasicDBObject("$sample", context.getMappedObject(Criteria.where("size").is(size).getCriteriaObject()));
    }

}