package cc.reconnected.server.data;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.struct.ServerPosition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StateManager {
    private static final Charset charset = StandardCharsets.UTF_8;
    private ServerState serverState;
    private final ConcurrentHashMap<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX") // iso 8601 my beloved
            .create();

    private Path basePath;
    private Path playersPath;
    private Path serverDataPath;

    public StateManager() {
    }

    public void register(Path path) {
        basePath = path;
        playersPath = basePath.resolve("players");
        serverDataPath = basePath.resolve("data.json");

        if (!basePath.toFile().exists()) {
            if (!basePath.toFile().mkdirs()) {
                RccServer.LOGGER.error("Could not create directory: {}", basePath.toAbsolutePath());
            }
        }

        if (!playersPath.toFile().exists()) {
            if (!playersPath.toFile().mkdirs()) {
                RccServer.LOGGER.error("Could not create directory: {}", playersPath.toAbsolutePath());
            }
        }

        if (serverDataPath.toFile().exists()) {
            try (var br = new BufferedReader(new FileReader(serverDataPath.toFile(), charset))) {
                serverState = gson.fromJson(br, ServerState.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            serverState = new ServerState();
            serverState.dirty = true;
        }

        ServerLifecycleEvents.SERVER_STOPPING.register(serverState -> save());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> loadPlayerState(handler.player.getUuid()));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            var playerState = getPlayerState(handler.getPlayer().getUuid());
            playerState.logoffPosition = new ServerPosition(handler.getPlayer());
            savePlayerState(handler.getPlayer().getUuid(), playerState);
            playerStates.remove(handler.player.getUuid());
        });

    }

    public void saveServerState() {
        if (!serverState.dirty)
            return;

        if (serverState.saving)
            return;
        serverState.saving = true;

        var json = gson.toJson(serverState, ServerState.class);
        try (var fw = new FileWriter(serverDataPath.toFile(), charset)) {
            fw.write(json);
            serverState.dirty = false;
        } catch (IOException e) {
            RccServer.LOGGER.error("Could not save server state", e);
        }
        serverState.saving = false;
    }

    public void savePlayerState(UUID uuid, PlayerState playerState) {
        if (!playerState.dirty)
            return;

        if (playerState.saving)
            return;
        playerState.saving = true;

        var path = playersPath.resolve(uuid.toString() + ".json");
        var json = gson.toJson(playerState, PlayerState.class);
        try (var fw = new FileWriter(path.toFile(), charset)) {
            fw.write(json);
            playerState.dirty = false;
        } catch (IOException e) {
            RccServer.LOGGER.error("Could not save player state", e);
        }
        playerState.saving = false;
    }

    public void save() {
        saveServerState();
        playerStates.forEach(this::savePlayerState);
    }

    private PlayerState loadPlayerState(UUID uuid) {
        var path = playersPath.resolve(uuid.toString() + ".json");
        PlayerState playerState;
        if (path.toFile().exists()) {
            try (var br = new BufferedReader(new FileReader(path.toFile(), charset))) {
                playerState = gson.fromJson(br, PlayerState.class);
            } catch (Exception e) {
                RccServer.LOGGER.error("Could not load player state: " + path.toAbsolutePath(), e);
                return null;
            }
        } else {
            playerState = new PlayerState();
            playerState.dirty = true;
        }

        playerStates.put(uuid, playerState);
        playerState.uuid = uuid;

        return playerState;
    }

    public PlayerState getPlayerState(UUID uuid) {
        PlayerState playerState;
        if (playerStates.containsKey(uuid)) {
            playerState = playerStates.get(uuid);
        } else {
            playerState = loadPlayerState(uuid);
        }

        if (playerState == null)
            return null;

        playerState.dirty = true;
        return playerState;
    }

    public ServerState getServerState() {
        serverState.dirty = true;
        return serverState;
    }
}
