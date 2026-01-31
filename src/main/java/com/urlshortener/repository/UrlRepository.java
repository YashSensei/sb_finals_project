package com.urlshortener.repository;

import com.urlshortener.model.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends MongoRepository<Url, String> {

    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    Page<Url> findByUserId(String userId, Pageable pageable);

    Page<Url> findByUserIdAndIsActive(String userId, boolean isActive, Pageable pageable);

    @Query("{'userId': ?0, 'tags': {$in: ?1}}")
    Page<Url> findByUserIdAndTagsIn(String userId, List<String> tags, Pageable pageable);

    @Query("{'userId': ?0, '$or': [{'title': {$regex: ?1, $options: 'i'}}, {'originalUrl': {$regex: ?1, $options: 'i'}}]}")
    Page<Url> searchByUserIdAndKeyword(String userId, String keyword, Pageable pageable);

    long countByUserId(String userId);

    long countByUserIdAndIsActive(String userId, boolean isActive);

    @Query(value = "{'userId': ?0, 'expiresAt': {$lt: ?1, $ne: null}}", count = true)
    Long countByUserIdAndExpired(String userId, LocalDateTime now);

    List<Url> findTop5ByUserIdOrderByClickCountDesc(String userId);

    List<Url> findTop5ByUserIdOrderByCreatedAtDesc(String userId);

    @Query("{'expiresAt': {$lt: ?0, $ne: null}, 'isActive': true}")
    List<Url> findExpiredUrls(LocalDateTime now);
}
