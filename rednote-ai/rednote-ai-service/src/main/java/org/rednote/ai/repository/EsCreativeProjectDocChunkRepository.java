package org.rednote.ai.repository;

import org.rednote.ai.entity.EsCreativeProjectDocChunk;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EsCreativeProjectDocChunkRepository extends ElasticsearchRepository<EsCreativeProjectDocChunk, String> {
    long deleteByProjectId(String projectId);
}
