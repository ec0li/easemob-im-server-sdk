package com.easemob.im.server.api.chatgroups.list;

import com.easemob.im.server.api.AbstractApiTest;
import com.easemob.im.server.model.EMGroup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class GroupListTest extends AbstractApiTest {
    private static int seq = 0;

    public GroupListTest() {
        super();
        this.server.addHandler("GET /easemob/demo/chatgroups?limit=10", this::handleGroupListRequest1);
        this.server.addHandler("GET /easemob/demo/chatgroups?limit=10&cursor=1", this::handleGroupListRequest2);
        this.server.addHandler("GET /easemob/demo/chatgroups?limit=10&cursor=2", this::handleGroupListRequest3);
        this.server.addHandler("GET /easemob/demo/users/alice/joined_chatgroups", this::handleGroupListUserJoined);
    }



    @Test
    public void testGroupListHighLevelApi() {
        GroupList groupList = new GroupList(this.context);
        groupList.all(10)
            .as(StepVerifier::create)
            .expectNextCount(25)
            .expectComplete()
            .verify(Duration.ofSeconds(3));
    }

    @Test
    public void testGroupListLowLevelApi() {
        GroupList groupList = new GroupList(this.context);
        groupList.all(10, "1")
            .as(StepVerifier::create)
            .expectNextMatches(rsp -> rsp.getGroups().size() == 10 && rsp.getCursor().equals("2"))
            .expectComplete()
            .verify(Duration.ofSeconds(3));
    }

    @Test
    public void testGroupListUserJoined() {
        GroupList groupList = new GroupList(this.context);

        groupList.userJoined("alice")
            .as(StepVerifier::create)
            .expectNext(new EMGroup("aliceGroup"))
            .expectNext(new EMGroup("rabbitGroup"))
            .expectComplete()
            .verify(Duration.ofSeconds(3));
    }


    private JsonNode handleGroupListRequest1(JsonNode jsonNode) {
        return buildResponse(10, "1");
    }

    private JsonNode handleGroupListRequest2(JsonNode jsonNode) {
        return buildResponse(10, "2");
    }

    private JsonNode handleGroupListRequest3(JsonNode jsonNode) {
        return buildResponse(5, null);
    }

    private JsonNode handleGroupListUserJoined(JsonNode jsonNode) {
        ArrayNode data = this.objectMapper.createArrayNode();

        ObjectNode group1 = this.objectMapper.createObjectNode();
        group1.put("groupId", "aliceGroup");
        data.add(group1);

        ObjectNode group2 = this.objectMapper.createObjectNode();
        group2.put("groupId", "rabbitGroup");
        data.add(group2);

        ObjectNode rsp = this.objectMapper.createObjectNode();
        rsp.set("data", data);
        return rsp;
    }

    private ObjectNode buildResponse(int count, String cursor) {
        ArrayNode data = this.objectMapper.createArrayNode();
        for (int i = 0; i < count; i++) {
            ObjectNode group = this.objectMapper.createObjectNode();
            group.put("groupId", String.format("%d", seq++));
            data.add(group);
        }
        ObjectNode rsp = this.objectMapper.createObjectNode();
        rsp.set("data", data);
        if (cursor != null) {
            rsp.put("cursor", cursor);
        }
        return rsp;
    }

}