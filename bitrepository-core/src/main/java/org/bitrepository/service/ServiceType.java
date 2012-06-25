package org.bitrepository.service;

public enum ServiceType {
    AlarmService,
    IntegrityService,
    AuditTrailService,
    MonitoringService;

    public String value() {
        return name();
    }

    public static ServiceType fromValue(String v) {
        return valueOf(v);
    }
}
