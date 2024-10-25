package cc.reconnected.server.data;

import cc.reconnected.server.struct.ServerPosition;
import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerState {
    public boolean dirty = false;
    public boolean saving = false;

    @Expose
    public UUID uuid;
    @Expose
    public String username;
    @Expose
    public @Nullable Date firstJoinedDate;
    @Expose
    public @Nullable ServerPosition logoffPosition = null;
    @Expose
    public int activeTime = 0;
    @Expose
    public ConcurrentHashMap<String, ServerPosition> homes = new ConcurrentHashMap<>();


}
