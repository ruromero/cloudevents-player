package com.redhat.syseng.tools.cloudevents;

import java.util.Map;

import com.redhat.syseng.tools.cloudevents.model.PlayerMode;

import io.quarkus.test.junit.QuarkusTestProfile;

public class LocalModeProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("player.mode", PlayerMode.LOCAL.name());
    }

}
