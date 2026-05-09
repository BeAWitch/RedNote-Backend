package org.rednote.ai.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.KnnSimilarity;

/**
 * 创作台: 临时项目文档 chunk 向量索引。
 *
 * 生命周期: 用户确认采用正文后触发 cleanup 删除。
 */
@Data
@Document(indexName = "ai_project_doc_chunk")
public class EsCreativeProjectDocChunk {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String projectId;

    @Field(type = FieldType.Keyword)
    private String docId;

    @Field(type = FieldType.Keyword)
    private String docName;

    @Field(type = FieldType.Keyword)
    private String chunkId;

    @Field(type = FieldType.Text)
    private String text;

    @Field(type = FieldType.Dense_Vector, dims = 2048, knnSimilarity = KnnSimilarity.COSINE)
    private float[] embedding;

    @Field(type = FieldType.Long)
    private Long createdAt;
}
