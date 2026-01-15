package org.rednote.search.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.search.service.IEsSyncService;
import org.rednote.common.constant.MQConstants;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoteEventListener {

    private final IEsSyncService esSyncService;

    @RabbitListener(
            bindings = @QueueBinding(
                    exchange = @Exchange(value = MQConstants.NOTE_EVENT_EXCHANGE),
                    value = @Queue(value = MQConstants.NOTE_ES_SYNC_QUEUE, durable = "true"),
                    key = { MQConstants.NOTE_CREATE_KEY, MQConstants.NOTE_UPDATE_KEY }
            )
    )
    public void handleNoteCreateOrUpdateEvent(Long noteId) {
        log.info("笔记事件队列接收到数据，同步笔记到 ES: {}", noteId);
        esSyncService.syncNoteToEs(noteId);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    exchange = @Exchange(value = MQConstants.NOTE_EVENT_EXCHANGE),
                    value = @Queue(value = MQConstants.NOTE_ES_DELETE_QUEUE, durable = "true"),
                    key = { MQConstants.NOTE_DELETE_KEY }
            )
    )
    public void handleNoteDeleteEvent(Long noteId) {
        log.info("笔记事件队列接收到数据，从 ES 中删除笔记: {}", noteId);
        esSyncService.deleteNoteFromEs(noteId);
    }
}
