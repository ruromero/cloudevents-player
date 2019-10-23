package com.redhat.syseng.tools.cloudevents.resources;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class MessageResourceTest {

    @Test
    public void getAll() {
        given()
          .when().get("/messages")
          .then()
             .statusCode(200);
    }

}