package com.carota.dev.dm;


public interface IMessageTopic {

    String TOPIC_DOWNLOAD_RESPONSE = "downloadResponse";
    String TOPIC_PROGRESS_RESPONSE = "progressResponse";
    String TOPIC_GET_FILE_RESPONSE = "getFileResponse";
    String TOPIC_DELETE_FILE_RESPONSE = "deleteFileResponse";
    String TOPIC_STOP_DOWNLOAD_RESPONSE = "stopDownloadResponse";
    String TOPIC_LOG_FILE_RESPONSE = "syncLogFileResponse";
    String TOPIC_LOG_FILE_FINISH_RESPONSE = "syncLogFileFinishResponse";
}
