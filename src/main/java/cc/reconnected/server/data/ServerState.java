package cc.reconnected.server.data;

import cc.reconnected.server.struct.ServerPosition;
import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public class ServerState {
    public boolean dirty = false;
    public boolean saving = false;

    @Expose
    public @Nullable ServerPosition spawn;
    @Expose
    public ConcurrentHashMap<String, ServerPosition> warps = new ConcurrentHashMap<>();

}
