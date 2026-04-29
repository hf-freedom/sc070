package com.iot.firmware.model;

public enum UpgradeTaskStatus {
    PENDING,
    QUEUED,
    DELIVERED,
    DOWNLOADING,
    INSTALLING,
    REBOOTING,
    SUCCESS,
    FAILED,
    CANCELLED
}
