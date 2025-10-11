package com.arckenver.towny.object;

import java.util.Date;
import java.util.UUID;

public class NationRequest {
    private final UUID nationUUID;
    private final UUID townUUID;
    private final Date date;

    public NationRequest(UUID nationUUID, UUID townUUID) {
        this.nationUUID = nationUUID;
        this.townUUID = townUUID;
        this.date = new Date();
    }

    public UUID getNationUUID() {
        return nationUUID;
    }

    public UUID getTownUUID() {
        return townUUID;
    }

    public Date getDate() {
        return date;
    }

    public boolean match(UUID nationUUID, UUID townUUID) {
        return this.nationUUID.equals(nationUUID) && this.townUUID.equals(townUUID);
    }
}
