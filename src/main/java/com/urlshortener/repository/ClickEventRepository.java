package com.urlshortener.repository;

import com.urlshortener.model.ClickEvent;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClickEventRepository extends MongoRepository<ClickEvent, String> {

    long countByUrlId(String urlId);

    List<ClickEvent> findByUrlIdOrderByTimestampDesc(String urlId);

    @Query("{'urlId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<ClickEvent> findByUrlIdAndTimestampBetween(String urlId, LocalDateTime start, LocalDateTime end);

    @Query(value = "{'urlId': ?0}", fields = "{'ipAddress': 1}")
    List<ClickEvent> findDistinctIpAddressesByUrlId(String urlId);

    @Aggregation(pipeline = {
            "{'$match': {'urlId': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}}",
            "{'$group': {'_id': {'$dateToString': {'format': '%Y-%m-%d', 'date': '$timestamp'}}, 'count': {'$sum': 1}}}",
            "{'$sort': {'_id': 1}}"
    })
    List<DateClickCount> getClicksByDateRange(String urlId, LocalDateTime start, LocalDateTime end);

    @Aggregation(pipeline = {
            "{'$match': {'urlId': ?0}}",
            "{'$group': {'_id': '$country', 'count': {'$sum': 1}}}",
            "{'$sort': {'count': -1}}",
            "{'$limit': 10}"
    })
    List<FieldCount> getTopCountries(String urlId);

    @Aggregation(pipeline = {
            "{'$match': {'urlId': ?0}}",
            "{'$group': {'_id': '$browser', 'count': {'$sum': 1}}}",
            "{'$sort': {'count': -1}}",
            "{'$limit': 10}"
    })
    List<FieldCount> getTopBrowsers(String urlId);

    @Aggregation(pipeline = {
            "{'$match': {'urlId': ?0}}",
            "{'$group': {'_id': '$deviceType', 'count': {'$sum': 1}}}",
            "{'$sort': {'count': -1}}"
    })
    List<FieldCount> getDeviceBreakdown(String urlId);

    @Aggregation(pipeline = {
            "{'$match': {'urlId': ?0, 'referer': {'$ne': null, '$ne': ''}}}",
            "{'$group': {'_id': '$referer', 'count': {'$sum': 1}}}",
            "{'$sort': {'count': -1}}",
            "{'$limit': 10}"
    })
    List<FieldCount> getTopReferrers(String urlId);

    @Aggregation(pipeline = {
            "{'$match': {'urlId': ?0}}",
            "{'$group': {'_id': {'$hour': '$timestamp'}, 'count': {'$sum': 1}}}",
            "{'$sort': {'_id': 1}}"
    })
    List<FieldCount> getClicksByHour(String urlId);

    @Aggregation(pipeline = {
            "{'$match': {'userId': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}}",
            "{'$group': {'_id': {'$dateToString': {'format': '%Y-%m-%d', 'date': '$timestamp'}}, 'count': {'$sum': 1}}}",
            "{'$sort': {'_id': 1}}"
    })
    List<DateClickCount> getClicksByDateRangeForUser(String userId, LocalDateTime start, LocalDateTime end);

    long countByUserId(String userId);

    interface DateClickCount {
        String get_id();
        long getCount();
    }

    interface FieldCount {
        Object get_id();
        long getCount();
    }
}
