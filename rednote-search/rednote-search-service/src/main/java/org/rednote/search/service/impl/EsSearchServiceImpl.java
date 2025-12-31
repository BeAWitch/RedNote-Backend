package org.rednote.search.service.impl;

import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.rednote.search.api.dto.SearchNoteDTO;
import org.rednote.search.api.entity.EsNote;
import org.rednote.search.service.IEsSearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EsSearchServiceImpl implements IEsSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * ES 搜索笔记
     *
     * @param currentPage   当前页
     * @param pageSize      每页大小
     * @param searchNoteDTO 搜索条件
     */
    @Override
    public Page<EsNote> searchNote(long currentPage, long pageSize, SearchNoteDTO searchNoteDTO) {
        // 构建 Bool Query
        Query query = BoolQuery.of(b -> {

            // 关键词搜索（title + content）
            if (StrUtil.isNotBlank(searchNoteDTO.getKeyword())) {
                b.must(m -> m.multiMatch(mm -> mm
                        .query(searchNoteDTO.getKeyword())
                        .fields("title^3",
                                "content^1"
                        )
                ));
            }

            // 分类过滤
            if (searchNoteDTO.getCpid() != null) {
                b.filter(f -> f.term(t -> t.field("cpid").value(searchNoteDTO.getCpid())));
            }
            if (searchNoteDTO.getCid() != null) {
                b.filter(f -> f.term(t -> t.field("cid").value(searchNoteDTO.getCid())));
            }

            // 只查审核通过
            b.filter(f -> f.term(t -> t.field("auditStatus").value(1)));

            return b;
        })._toQuery();

        // 排序
        List<SortOptions> sorts = new ArrayList<>();

        if (searchNoteDTO.getType() != null) {
            if (searchNoteDTO.getType() == 1) {
                // 点赞排序
                sorts.add(SortOptions.of(s -> s
                        .field(f -> f.field("likeCount").order(SortOrder.Desc))
                ));
            } else if (searchNoteDTO.getType() == 2) {
                // 时间排序
                sorts.add(SortOptions.of(s -> s
                        .field(f -> f.field("updateTime").order(SortOrder.Desc))
                ));
            }
        }

        // 构建 NativeQuery
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(
                        (int) currentPage - 1,
                        (int) pageSize
                ));
        // 只有当 sorts 不为空时才添加排序
        if (!sorts.isEmpty()) {
            queryBuilder.withSort(sorts);
        }
        NativeQuery nativeQuery = queryBuilder.build();

        // 执行查询
        SearchHits<EsNote> searchHits =
                elasticsearchOperations.search(nativeQuery, EsNote.class);

        // 转换为列表
        List<EsNote> records = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // 组装 Page
        Page<EsNote> page = new Page<>();
        page.setRecords(records);
        page.setTotal(searchHits.getTotalHits());

        return page;
    }
}
