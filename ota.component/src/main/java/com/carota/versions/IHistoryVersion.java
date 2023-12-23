package com.carota.versions;

public interface IHistoryVersion {

    class HVersionInfo {
        public String title;        // 获取title字段的值
        public String taskId;       // 获取task_id字段的值
        public long timestamp;      // 获取_at字段的值
    }
}
