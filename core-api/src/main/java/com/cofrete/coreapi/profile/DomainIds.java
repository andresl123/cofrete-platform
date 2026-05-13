package com.cofrete.coreapi.profile;

import java.util.UUID;

public final class DomainIds {

    private DomainIds() {
    }

    public static String prefixed(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
    }
}
