package com.znzlspt.dao.model;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

import java.util.UUID;

public class PubUser {
    UUID uuid;
    String id;

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static PubUser fromRow(Row row, RowMetadata meta) {
        PubUser pu = new PubUser();
        pu.setUUID(row.get("uuid", UUID.class));
        pu.setId(row.get("id", String.class));

        return pu;
    }

}


