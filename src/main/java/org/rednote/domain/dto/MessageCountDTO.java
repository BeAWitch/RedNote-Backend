package org.rednote.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息数量
 */
@Data
public class MessageCountDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uid;

    private Long likeOrFavoriteCount;

    private Long commentCount;

    private Long followCount;

    private Long chatCount;

    private Long trendCount;
}