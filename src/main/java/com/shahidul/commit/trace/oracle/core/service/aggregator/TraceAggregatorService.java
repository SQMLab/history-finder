package com.shahidul.commit.trace.oracle.core.service.aggregator;

/**
 * @author Shahidul Islam
 * @since 11/13/2023
 */
public interface TraceAggregatorService {
    void populateMetaData();
    void aggregate();
}
