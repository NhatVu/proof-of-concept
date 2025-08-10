package com.poc.postgres_key_value_cache.repo;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class CacheRepository {
    private final JdbcTemplate jdbc;
    private final int BATCH_SIZE = 100;

    public Optional<String> findValueIfFresh(String key) {
        String sql = "SELECT value, expires_at FROM cache_entries WHERE key = ?";
        return jdbc.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            Timestamp expires = rs.getTimestamp("expires_at");
            if (expires != null && expires.toInstant().isAfter(java.time.Instant.now())) {
                return Optional.of(rs.getString("value"));
            }
            return Optional.empty();
        }, key);
    }

    public Optional<String> get(String key) {
        String sql = "SELECT value, expires_at FROM cache_entries WHERE key = ?";
        return jdbc.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(rs.getString("value"));
        }, key);
    }

//    public Optional<CacheRow> selectRow(String key) {
//        String sql = "SELECT key, value, expires_at, last_accessed_at, is_refreshing, refresh_started_at FROM cache_entries WHERE key = ?";
//        List<CacheRow> rows = jdbc.query(sql, (rs, i) -> new CacheRow(
//                rs.getString("key"),
//                rs.getBytes("value"),
//                rs.getTimestamp("expires_at") != null ? rs.getTimestamp("expires_at").toInstant() : null,
//                rs.getTimestamp("last_accessed_at").toInstant(),
//                rs.getBoolean("is_refreshing"),
//                rs.getTimestamp("refresh_started_at") != null ? rs.getTimestamp("refresh_started_at").toInstant() : null
//        ), key);
//        return rows.stream().findFirst();
//    }

    // Acquire refresh lock atomically. Returns true if lock acquired.
    public boolean tryAcquireRefreshLock(String key, int staleSeconds) {
        String sql = """
                UPDATE cache_entries 
                SET is_refreshing = true, refresh_started_at = now() 
                WHERE key = ? 
                AND (is_refreshing = false OR refresh_started_at < now() - make_interval(secs => ?))
                """;
        int updated = jdbc.update(sql, key, staleSeconds);
        return updated == 1;
    }

    public void releaseLockAndUpdate(String key, String value, OffsetDateTime expiresAt) {
        String sql = "UPDATE cache_entries " +
                "SET is_refreshing = false, refresh_started_at = NULL, " +
                "value = ?::jsonb, expires_at = ?, last_accessed_at = now() " +
                "WHERE key = ?";
        jdbc.update(sql, value, Timestamp.from(expiresAt.toInstant()), key);
    }

    // Insert or upsert
    public void upsert(String key, String value, OffsetDateTime expiresAt) {
        String sql = """
                INSERT INTO cache_entries(key, value, expires_at, last_accessed_at, is_refreshing) 
                VALUES (?, ?::jsonb, ?, now(), false) 
                ON CONFLICT (key) 
                DO UPDATE SET value = EXCLUDED.value, expires_at = EXCLUDED.expires_at
                """;
        jdbc.update(sql, key, value, Timestamp.from(expiresAt.toInstant()));
    }

    // Batch update last_accessed_at
    public void batchUpdateLastAccessed(List<String> keys) {
        String sql = "UPDATE cache_entries SET last_accessed_at = now() WHERE key = ?";
        jdbc.batchUpdate(sql, keys, BATCH_SIZE, (ps, argument) -> {
            ps.setString(1, argument);
        });
//        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                ps.setString(1, keys.get(i));
//            }
//
//            @Override
//            public int getBatchSize() {
//                return keys.size();
//            }
//        });
    }

    // Select expiring keys for refresh
    public List<String> selectRefreshCandidateKeys(int limit, int retentionDays) {
        String sql = """
                SELECT key FROM cache_entries 
                WHERE expires_at <= now() 
                AND last_accessed_at >= now() - make_interval(days => ?)
                AND (is_refreshing = false OR refresh_started_at < now() - interval '5 minutes')
                ORDER BY expires_at ASC LIMIT ?
                """;
        return jdbc.queryForList(sql, String.class, retentionDays, limit);
    }

    // LRU cleanup
    public int deleteOldKeys(int retentionDays) {
        String sql = """
            DELETE FROM cache_entries WHERE last_accessed_at < now() - make_interval(days => ?)
        """;
        return jdbc.update(sql, retentionDays);
    }

    /**
     * Force release the refresh lock for a given key
     * @param key The cache key
     * @return number of rows updated
     */
    public int forceReleaseLock(String key) {
        String sql = """
            UPDATE cache_keys
            SET is_refreshing = FALSE,
                refresh_started_at = NULL
            WHERE key = ?
            """;
        return jdbc.update(sql, key);
    }

    /**
     * Force release locks for multiple keys in batch
     * @param keys list of keys
     * @return array of row counts for each key
     */
    public int[][] forceReleaseLock(List<String> keys) {
        String sql = """
            UPDATE cache_keys
            SET is_refreshing = FALSE,
                refresh_started_at = NULL
            WHERE key = ?
            """;

        return jdbc.batchUpdate(sql, keys, BATCH_SIZE,
                (ps, k) -> ps.setString(1, k));
    }
}
