package com.iot.firmware.model;

import lombok.Data;
import java.util.Map;

@Data
public class CoverageReport {
    private String reportId;
    private String firmwareId;
    private String version;
    private Integer totalDevices;
    private Integer upgradedDevices;
    private Integer pendingDevices;
    private Integer failedDevices;
    private Double coverageRate;
    private Map<String, Integer> byModel;
    private Map<String, Integer> byRegion;
}
