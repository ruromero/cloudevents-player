package com.redhat.syseng.tools.cloudevents.serialization;

public interface CloudEventV3Serializable {

    String EVENT_SOURCE_HEADER = "ce-source";
    String EVENT_ID_HEADER = "ce-id";
    String EVENT_TYPE_HEADER = "ce-type";
    String EVENT_SPECVERSION_HEADER = "ce-specversion";
}
