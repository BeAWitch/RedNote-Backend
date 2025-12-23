package org.rednote.interaction.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.common.domain.dto.Result;
import org.rednote.interaction.api.dto.MessageCountDTO;
import org.rednote.interaction.api.dto.MessageDTO;
import org.rednote.interaction.api.enums.UncheckedMessageEnum;
import org.rednote.interaction.api.vo.ChatConversationVO;
import org.rednote.interaction.api.vo.ChatMessageVO;
import org.rednote.interaction.service.IWebChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "聊天管理", description = "用户聊天相关接口")
@RequestMapping("/web/chat")
@RestController
@RequiredArgsConstructor
public class WebChatController {

    private final IWebChatService chatService;

    @Operation(summary = "发送消息", description = "发送消息")
    @PostMapping("sendMessage")
    public Result<?> sendMessage(@Parameter(description = "消息实体") @RequestBody MessageDTO messageDTO) {
        chatService.sendMessage(messageDTO);
        return Result.ok();
    }

    @Operation(summary = "获取所有的聊天记录", description = "获取所有的聊天记录")
    @GetMapping("getMessage/{currentPage}/{pageSize}")
    public Result<?> getMessage(@Parameter(description = "当前页码") @PathVariable long currentPage,
                                @Parameter(description = "每页大小") @PathVariable long pageSize,
                                @Parameter(description = "会话 ID") Long conversationId) {
        Page<ChatMessageVO> page = chatService.getMessage(currentPage, pageSize, conversationId);
        return Result.ok(page);
    }

    @Operation(summary = "获取当前用户下所有聊天的信息", description = "获取当前用户下所有聊天的信息")
    @GetMapping("getConversationList")
    public Result<?> getConversationList() {
        List<ChatConversationVO> list = chatService.getConversationList();
        return Result.ok(list);
    }

    @Operation(summary = "获取所有未读消息数量", description = "获取所有未读消息数量")
    @GetMapping("getUncheckedMessageCount")
    public Result<?> getMessageCount() {
        MessageCountDTO messageCountDTO = chatService.getUncheckedMessageCount();
        return Result.ok(messageCountDTO);
    }

    @Operation(summary = "清除未确认消息数量", description = "清除未确认消息数量")
    @GetMapping("clearUncheckedMessageCount")
    public Result<?> clearUncheckedMessageCount(@Parameter(description = "类型（点赞、评论、关注）") UncheckedMessageEnum type) {
        chatService.clearUncheckedMessageCount(type);
        return Result.ok();
    }

    @Operation(summary = "清除未读消息数量", description = "清除未读消息数量，仅用于聊天")
    @GetMapping("clearUnreadMessageCount")
    public Result<?> clearUnreadMessageCount(@Parameter(description = "会话 ID") Long conversationId) {
        chatService.clearUnreadMessageCount(conversationId);
        return Result.ok();
    }

    @Operation(summary = "关闭聊天", description = "关闭聊天")
    @RequestMapping("closeChat/{sendUid}")
    public boolean closeChat(@Parameter(description = "发送方用户 ID") @PathVariable("sendUid") Long sendUid) {
        return chatService.closeChat(sendUid);
    }
}
