package org.rednote.ai.repository;

import org.rednote.ai.entity.EsAiUserNoteVector;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EsAiUserNoteVectorRepository extends ElasticsearchRepository<EsAiUserNoteVector, String> {
    long deleteByNoteId(Long noteId);
}
