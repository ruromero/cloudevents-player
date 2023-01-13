package com.redhat.syseng.tools.cloudevents.resources;

import com.redhat.syseng.tools.cloudevents.LocalModeProfile;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@TestProfile(LocalModeProfile.class)
public class NativeMessageResourceIT extends MessageResourceTest {

    // Execute the same tests but in native mode.

}