package com.example.backend.dict.service;

import java.util.HashMap;
import java.util.Map;

public class UserDirectory {

    private final Map<String, String> localCache = new HashMap<>();
    private final Map<String, String> store = new HashMap<>();

    public UserDirectory() {
        store.put("user:1001", "Alice");
        store.put("user:1002", "Bob");
    }

    public String findDisplayName(String userKey) {
        String cached = localCache.get(userKey);
        if (cached != null) {
            return cached;
        }

        String value = store.get(userKey);
        if (value != null) {
            localCache.put(userKey, value);
        }
        return value;
    }
}
