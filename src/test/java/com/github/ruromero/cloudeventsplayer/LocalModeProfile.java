package com.github.ruromero.cloudeventsplayer;

import java.util.Map;

import com.github.ruromero.cloudeventsplayer.model.PlayerMode;

import io.quarkus.test.junit.QuarkusTestProfile;

public class LocalModeProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("player.mode", PlayerMode.LOCAL.name());
    }

}
