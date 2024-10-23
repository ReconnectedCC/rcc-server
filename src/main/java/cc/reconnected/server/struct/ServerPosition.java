package cc.reconnected.server.struct;

import cc.reconnected.server.RccServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ServerPosition {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public ServerWorld world;

    public ServerPosition(double x, double y, double z, float yaw, float pitch, ServerWorld world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
    }

    public ServerPosition(ServerPlayerEntity player) {
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.yaw = player.getYaw();
        this.pitch = player.getPitch();
        this.world = player.getServerWorld();
    }

    public void teleport(ServerPlayerEntity player) {
        var currentPosition = new ServerPosition(player);
        RccServer.lastPlayerPositions.put(player.getUuid(), currentPosition);

        player.teleport(
                this.world,
                this.x,
                this.y,
                this.z,
                this.yaw,
                this.pitch
        );
    }
}
