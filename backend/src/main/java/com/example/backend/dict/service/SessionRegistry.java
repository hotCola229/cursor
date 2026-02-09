package com.example.backend.dict.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SessionRegistry {

    private static final List<SessionInfo> registry = new ArrayList<>();

    public void register(String userId) {
        registry.add(new SessionInfo(userId, UUID.randomUUID().toString(), new byte[1024 * 1024]));
    }

    public int size() {
        return registry.size();
    }

    static class SessionInfo {
        private final String userId;
        private final String token;
        private final byte[] attachment;

        SessionInfo(String userId, String token, byte[] attachment) {
            this.userId = userId;
            this.token = token;
            this.attachment = attachment;
        }

        public String getUserId() {
            return userId;
        }

        public String getToken() {
            return token;
        }
    }
}
