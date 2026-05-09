package org.rednote.ai.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.ai.service.NoteVectorSyncService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 启动后异步执行历史笔记向量全量同步（仅首次）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BootNoteVectorSyncRunner implements ApplicationRunner {

    private static final String SYNC_FLAG_KEY = "ai:note:vector:synced";

    private final NoteVectorSyncService syncService;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${ai.note.vector.sync.enabled:true}")
    private boolean syncEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!syncEnabled) {
            log.info("笔记向量全量同步已通过配置禁用 (ai.note.vector.sync.enabled=false)，跳过");
            return;
        }

        Boolean already = stringRedisTemplate.hasKey(SYNC_FLAG_KEY);
        if (Boolean.TRUE.equals(already)) {
            log.info("笔记向量全量同步已完成 (Redis key={} 存在)，跳过", SYNC_FLAG_KEY);
            return;
        }

        log.info("检测到首次启动，将在后台异步执行笔记向量全量同步...");
        CompletableFuture.runAsync(() -> {
            try {
                NoteVectorSyncService.SyncResult result = syncService.syncAllPublishedNotes();
                stringRedisTemplate.opsForValue().set(SYNC_FLAG_KEY,
                        String.format("syncedNotes=%d,skipped=%d,elapsed=%dms",
                                result.syncedNotes(), result.skippedNotes(), result.elapsedMs()));
                log.info("笔记向量全量同步完成: {}", result);
            } catch (Exception e) {
                log.error("笔记向量全量同步失败", e);
            }
        });
    }
}
