package org.rednote.search.repository;

import org.rednote.search.api.entity.EsNote;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EsNoteRepository extends ElasticsearchRepository<EsNote, Long> {
}