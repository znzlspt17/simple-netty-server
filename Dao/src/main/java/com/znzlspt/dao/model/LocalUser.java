package com.znzlspt.dao.model;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

import java.util.UUID;


public class LocalUser {
    UUID uuid;
    String nick;

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }


    public static LocalUser fromRow(Row row, RowMetadata meta) {
        LocalUser u = new LocalUser();
        u.setUUID(row.get("uuid", UUID.class));
        u.setNick(row.get("nick", String.class));
        return u;
    }

}
