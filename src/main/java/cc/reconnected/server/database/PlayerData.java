package cc.reconnected.server.database;

import cc.reconnected.server.RccServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerData {
    public static final String nodePrefix = "rcc";

    private static LuckPerms luckPerms() {
        return RccServer.getInstance().luckPerms();
    }

    public static class KEYS {
        public static final String username = "username";
        public static final String discordId = "discord_id";
        public static final String isBot = "is_bot";
        public static final String isAlt = "is_alt";
        public static final String pronouns = "pronouns";
        public static final String firstJoinedDate = "first_joined_date";
        public static final String supporterLevel = "supporter_level";
    }

    private final User lpUser;

    private final UUID uuid;
    @Nullable
    private String name;

    private Set<MetaNode> rawNodes;
    private Map<String, String> nodes;

    private PlayerData(UUID uuid, User lpUser) {
        this.uuid = uuid;
        this.lpUser = lpUser;

        refreshNodes();
    }

    public UUID getUuid() {
        return uuid;
    }

    public @Nullable String getUsername() {
        var username = get(KEYS.username);
        if (username == null) {
            return name;
        }
        return username;
    }

    public String getEffectiveName() {
        var effName = getUsername();
        if (effName == null)
            return uuid.toString();
        return effName;
    }

    public void refreshNodes() {
        rawNodes = lpUser.getNodes(NodeType.META)
                .parallelStream()
                .filter(node -> node.getMetaKey().startsWith(nodePrefix + "."))
                .collect(Collectors.toSet());

        nodes = rawNodes.stream().collect(Collectors.toMap(MetaNode::getMetaKey, MetaNode::getMetaValue));
    }

    public void set(String key, @Nullable String value) {
        var node = meta(key, value).build();
        luckPerms().getUserManager().modifyUser(uuid, user -> {
            user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(key)));
            user.data().add(node);
            refreshNodes();
        });
    }

    public @Nullable String get(String key) {
        if (!nodes.containsKey(nodePrefix + "." + key))
            return null;
        return nodes.get(nodePrefix + "." + key);
    }

    public @Nullable MetaNode getNode(String key) {
        return rawNodes.stream().filter(rawNode -> rawNode.getMetaKey().equals(key)).findFirst().orElse(null);
    }

    public void setBoolean(String key, boolean value) {
        set(key, Boolean.toString(value));
    }

    public boolean getBoolean(String key) {
        if (!nodes.containsKey(nodePrefix + "." + key))
            return false;
        return Boolean.parseBoolean(nodes.get(nodePrefix + "." + key));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (!nodes.containsKey(nodePrefix + "." + key))
            return defaultValue;
        return Boolean.parseBoolean(nodes.get(nodePrefix + "." + key));
    }

    public void setDate(String key, Date date) {
        var dateString = DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
        set(key, dateString);
    }

    public Date getDate(String key) {
        if (!nodes.containsKey(nodePrefix + "." + key))
            return null;
        var dateString = nodes.get(nodePrefix + "." + key);
        var ta = DateTimeFormatter.ISO_INSTANT.parse(dateString);
        return Date.from(Instant.from(ta));
    }

    public void delete(String key) {
        luckPerms().getUserManager().modifyUser(uuid, user -> {
            user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(nodePrefix + "." + key)));
        });
    }

    public static PlayerData getPlayer(UUID uuid) {
        var lp = luckPerms();
        var userManager = lp.getUserManager();

        var userFuture = userManager.loadUser(uuid);
        // TODO: ouch, not good...
        var lpUser = userFuture.join();

        var playerData = new PlayerData(uuid, lpUser);
        playerData.name = lpUser.getUsername();
        return playerData;
    }

    public static PlayerData getPlayer(ServerPlayerEntity player) {
        var user = luckPerms().getPlayerAdapter(ServerPlayerEntity.class).getUser(player);
        var playerData = new PlayerData(player.getUuid(), user);
        playerData.name = player.getEntityName();
        return playerData;
    }

    public static NodeBuilder<?, ?> node(String key) {
        return Node.builder(nodePrefix + "." + key);
    }

    public static NodeBuilder<?, ?> meta(String key, String value) {
        return MetaNode.builder(nodePrefix + "." + key, value);
    }
}
