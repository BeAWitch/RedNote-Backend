package org.rednote.search.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.note.api.entity.WebNote;
import org.rednote.search.api.entity.EsNote;
import org.rednote.search.feign.NoteServiceFeign;
import org.rednote.search.repository.EsNoteRepository;
import org.rednote.search.service.IEsSyncService;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EsSyncService implements IEsSyncService{

    private final NoteServiceFeign noteServiceFeign;
    private final EsNoteRepository esNoteRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    
    /**
     * 全量同步笔记到 ES
     */
    @Override
    @Async("taskExecutor")
    public void fullSyncNotesToEs() {
        log.info("开始全量同步笔记到 ES...");
        
        try {
            // 清空现有索引
            if (elasticsearchOperations.indexOps(EsNote.class).exists()) {
                elasticsearchOperations.indexOps(EsNote.class).delete();
            }
            elasticsearchOperations.indexOps(EsNote.class).create();
            elasticsearchOperations.indexOps(EsNote.class).putMapping();
            
            // 分批从数据库读取数据
            long pageSize = 1000L;
            long currentPage = 1L;
            long count = 0L;
            boolean hasMore = true;
            
            while (hasMore) {
                Page<WebNote> page = noteServiceFeign.selectNotePage(currentPage, pageSize);
                
                List<WebNote> notes = page.getRecords();
                if (notes.isEmpty()) {
                    break;
                }
                
                // 转换为 ES 实体
                List<EsNote> esNotes = notes.stream()
                    .filter(note -> note.getAuditStatus() == 1)  // 只同步审核通过的
                    .map(this::convertToEsNote)
                    .collect(Collectors.toList());
                
                // 批量保存到ES
                if (!esNotes.isEmpty()) {
                    esNoteRepository.saveAll(esNotes);
                    count += esNotes.size();
                    log.info("同步第 {} 页数据，共 {} 条", currentPage, esNotes.size());
                }
                
                // 判断是否还有下一页
                if (notes.size() < pageSize) {
                    hasMore = false;
                } else {
                    currentPage++;
                }
                
                // 短暂休眠，避免对数据库造成压力
                Thread.sleep(100);
            }
            
            log.info("全量同步完成，共同步 {} 条数据", count);
            
        } catch (Exception e) {
            log.error("同步笔记到 ES 失败", e);
            throw new RuntimeException("同步 ES 数据失败", e);
        }
    }
    
    /**
     * 增量同步（更新/新增单条笔记）
     */
    @Override
    @Async("taskExecutor")
    public void syncNoteToEs(Long noteId) {
        try {
            WebNote webNote = noteServiceFeign.getNoteById(noteId);
            if (webNote == null) {
                log.warn("笔记不存在，ID: {}", noteId);
                return;
            }
            
            // 只同步审核通过的笔记
            if (webNote.getAuditStatus() != 1) {
                // 如果存在则删除
                esNoteRepository.deleteById(noteId);
                log.info("删除未审核通过的笔记，ID: {}", noteId);
                return;
            }
            
            EsNote esNote = convertToEsNote(webNote);
            esNoteRepository.save(esNote);
            
            log.debug("同步笔记到ES成功，ID: {}", noteId);
            
        } catch (Exception e) {
            log.error("同步单条笔记到ES失败，ID: {}", noteId, e);
        }
    }
    
    /**
     * 批量增量同步
     */
    @Override
    @Async("taskExecutor")
    public void batchSyncNotesToEs(List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) {
            return;
        }
        
        log.info("批量同步笔记到ES，数量: {}", noteIds.size());
        
        // 分批处理，每批 100 条
        List<List<Long>> batches = Lists.partition(noteIds, 100);
        
        for (List<Long> batch : batches) {
            try {
                List<WebNote> webNotes = noteServiceFeign.getNoteByIds(batch);
                
                List<EsNote> esNotes = webNotes.stream()
                    .filter(note -> note.getAuditStatus() == 1)
                    .map(this::convertToEsNote)
                    .collect(Collectors.toList());
                
                if (!esNotes.isEmpty()) {
                    esNoteRepository.saveAll(esNotes);
                }
                
                Thread.sleep(50);  // 短暂休眠
                
            } catch (Exception e) {
                log.error("批量同步笔记失败，批次: {}", batch, e);
            }
        }
    }
    
    /**
     * 删除 ES 中的笔记
     */
    @Override
    @Async("taskExecutor")
    public void deleteNoteFromEs(Long noteId) {
        try {
            if (esNoteRepository.existsById(noteId)) {
                esNoteRepository.deleteById(noteId);
                log.debug("从ES删除笔记成功，ID: {}", noteId);
            }
        } catch (Exception e) {
            log.error("从ES删除笔记失败，ID: {}", noteId, e);
        }
    }
    
    /**
     * 转换 WebNote 为 EsNote
     */
    private EsNote convertToEsNote(WebNote webNote) {
        return BeanUtil.copyProperties(webNote, EsNote.class);
    }
    
    /**
     * 初始化 ES 索引（如果不存在）
     */
    @PostConstruct
    public void initEsIndex() {
        try {
            if (!elasticsearchOperations.indexOps(EsNote.class).exists()) {
                elasticsearchOperations.indexOps(EsNote.class).create();
                elasticsearchOperations.indexOps(EsNote.class).putMapping();
                log.info("创建ES索引成功");
            }
        } catch (Exception e) {
            log.error("初始化ES索引失败", e);
        }
    }
}