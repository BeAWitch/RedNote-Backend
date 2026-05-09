package org.rednote.ai.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.KnnSimilarity;

/**
 * 用户历史笔记向量索引，用于“历史笔记检索”。
 *
 * 由 note-service 的 MQ 事件增量维护。
 */
@Data
@Document(indexName = "ai_user_note_vector")
public class EsAiUserNoteVector {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long noteId;

    @Field(type = FieldType.Long)
    private Long uid;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Dense_Vector, dims = 2048, knnSimilarity = KnnSimilarity.COSINE)
    private float[] embedding;

    @Field(type = FieldType.Long)
    private Long updatedAt;
}
