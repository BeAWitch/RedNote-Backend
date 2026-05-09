package org.rednote.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.ai.service.NoteVectorSyncService;
import org.rednote.common.domain.dto.Result;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Tag(name = "AI 管理接口", description = "笔记向量索引管理等运维接口")
@RequestMapping("/web/ai/admin")
@RestController
@RequiredArgsConstructor
public class AiAdminController {

    private static final String SYNC_FLAG_KEY = "ai:note:vector:synced";

    private final NoteVectorSyncService syncService;
    private final StringRedisTemplate stringRedisTemplate;

    @Operation(summary = "手动触发笔记向量全量同步")
    @PostMapping("note-vector/sync")
    public Result<String> syncNoteVectors() {
        CompletableFuture.runAsync(() -> {
            try {
                NoteVectorSyncService.SyncResult result = syncService.syncAllPublishedNotes();
                stringRedisTemplate.opsForValue().set(SYNC_FLAG_KEY,
                        String.format("syncedNotes=%d,skipped=%d,elapsed=%dms",
                                result.syncedNotes(), result.skippedNotes(), result.elapsedMs()));
            } catch (Exception e) {
                // already logged in the service
            }
        });
        return Result.ok("笔记向量全量同步已在后台启动");
    }

    @Operation(summary = "重置同步标记（下次重启会重新同步）")
    @DeleteMapping("note-vector/sync-flag")
    public Result<String> resetSyncFlag() {
        stringRedisTemplate.delete(SYNC_FLAG_KEY);
        return Result.ok("同步标记已重置");
    }
}
