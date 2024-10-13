package cc.reconnected.server.data;

import cc.reconnected.server.RccServer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {
    public final HashMap<UUID, WorldPlayerData> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var playersNbt = new NbtCompound();
        players.forEach((uuid, data) -> {
            var playerNbt = new NbtCompound();
            playerNbt.putInt("activeTime", data.activeTime);
            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound nbt) {
        var state = new StateSaverAndLoader();

        var playersNbt = nbt.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            var playerData = new WorldPlayerData();

            playerData.activeTime = playersNbt.getCompound(key).getInt("activeTime");
            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        return state;
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        var persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        var state = persistentStateManager.getOrCreate(
                StateSaverAndLoader::createFromNbt,
                StateSaverAndLoader::new,
                RccServer.MOD_ID
        );
        state.markDirty();
        return state;
    }

    public static WorldPlayerData getPlayerState(LivingEntity player) {
        var serverState = getServerState(player.getWorld().getServer());
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new WorldPlayerData());
    }
}
