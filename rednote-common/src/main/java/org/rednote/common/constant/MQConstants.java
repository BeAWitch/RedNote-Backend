package org.rednote.common.constant;

public class MQConstants {

    /* ================== 笔记事件 ================== */

    // Exchange
    public static final String NOTE_EVENT_EXCHANGE = "note.event.exchange";

    // Routing Keys
    public static final String NOTE_CREATE_KEY = "note.create";
    public static final String NOTE_UPDATE_KEY = "note.update";
    public static final String NOTE_DELETE_KEY = "note.delete";

    // Queue
    public static final String NOTE_ES_SYNC_QUEUE = "note.es.sync.queue";
    public static final String NOTE_ES_DELETE_QUEUE = "note.es.delete.queue";


    /* ================== 文件事件 ================== */

    // Exchange
    public static final String FILE_EVENT_EXCHANGE = "file.event.exchange";

    // Routing Keys
    public static final String FILE_IMAGE_UPLOAD_USER = "file.image.upload.user";
    public static final String FILE_IMAGE_UPLOAD_NOTE = "file.image.upload.note";

    // Queue
    public static final String FILE_IMAGE_UPLOAD_QUEUE = "file.image.upload.queue";
    public static final String FILE_IMAGE_DELETE_QUEUE = "file.image.upload.queue";

    private MQConstants() {}

}
