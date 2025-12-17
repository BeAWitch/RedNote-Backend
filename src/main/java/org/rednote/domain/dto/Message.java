package org.rednote.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天消息
 */
@Data
public class Message implements Serializable {

    private static final long serialVersionUID = 2405172041950251807L;

    private Long sendUid;

    private Long acceptUid;

    private String content;

    /**
     * 消息类型（0：通知消息，1：文本消息，2：图片消息，3：语音消息，4：视频消息，5：自定义消息）
     */
    private Integer msgType;

    /**
     * 聊天类型（0：私聊，1：群聊）
     */
    private Integer chatType;
}