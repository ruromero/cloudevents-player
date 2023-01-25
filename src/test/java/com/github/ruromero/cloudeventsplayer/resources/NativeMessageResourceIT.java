package com.github.ruromero.cloudeventsplayer.resources;

import com.github.ruromero.cloudeventsplayer.LocalModeProfile;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@TestProfile(LocalModeProfile.class)
public class NativeMessageResourceIT extends MessageResourceTest {

    // Execute the same tests but in native mode.

}