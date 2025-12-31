package org.rednote.search.api.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Document(indexName = "note")
@Data
public class EsNote {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String content;

    private String noteCover;
    private Integer noteCoverHeight;

    private Long uid;
    private String author;

    private Long cid;
    private Long cpid;

    private String urls;
    private Integer count;

    private Integer pinned;
    private Integer auditStatus;
    private Integer noteType;

    private Long likeCount;
    private Long favoriteCount;
    private Long commentCount;
    private Long viewCount;

    private Date createTime;
    private Date updateTime;
}
