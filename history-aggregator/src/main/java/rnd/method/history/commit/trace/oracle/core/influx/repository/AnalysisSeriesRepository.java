package rnd.method.history.commit.trace.oracle.core.influx.repository;

import rnd.method.history.commit.trace.oracle.core.influx.series.AnalysisSeries;

import java.util.List;

/**
 * @since 12/3/2023
 */
public interface AnalysisSeriesRepository {
    void deleteAll();

    List<AnalysisSeries> saveAll(List<AnalysisSeries> analysisSeriesList);

    void deleteByOracleId(Integer oracleId);
}
