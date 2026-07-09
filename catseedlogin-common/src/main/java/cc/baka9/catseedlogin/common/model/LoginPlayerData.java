package cc.baka9.catseedlogin.common.model;

import cc.baka9.catseedlogin.common.crypto.HashVersion;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable player data value object.
 * Replaces the mutable LoginPlayer from v2.
 */
@Getter
public final class LoginPlayerData {

    private final String name;
    private final String passwordHash;
    private final HashVersion hashVersion;
    private final String email;
    private final List<String> ips;
    private final long lastAction;
    private final String location;

    private LoginPlayerData(Builder builder) {
        this.name = builder.name;
        this.passwordHash = builder.passwordHash;
        this.hashVersion = builder.hashVersion;
        this.email = builder.email;
        this.ips = Collections.unmodifiableList(new ArrayList<>(builder.ips));
        this.lastAction = builder.lastAction;
        this.location = builder.location;
    }

    public List<String> getIps() {
        return ips;
    }

    /**
     * Create a new builder.
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Create a builder pre-populated with this player's data.
     */
    public Builder toBuilder() {
        Builder builder = new Builder(name);
        builder.passwordHash = this.passwordHash;
        builder.hashVersion = this.hashVersion;
        builder.email = this.email;
        builder.ips.addAll(this.ips);
        builder.lastAction = this.lastAction;
        builder.location = this.location;
        return builder;
    }

    /**
     * Builder for LoginPlayerData.
     */
    public static class Builder {
        private final String name;
        private String passwordHash = "";
        private HashVersion hashVersion = HashVersion.V1_LEGACY;
        private String email = "";
        private final List<String> ips = new ArrayList<>();
        private long lastAction = 0;
        private String location = "";

        private Builder(String name) {
            this.name = name;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder hashVersion(HashVersion hashVersion) {
            this.hashVersion = hashVersion;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder ips(List<String> ips) {
            this.ips.clear();
            if (ips != null) {
                this.ips.addAll(ips);
            }
            return this;
        }

        public Builder addIp(String ip) {
            if (ip != null && !ip.isEmpty() && !ips.contains(ip)) {
                ips.add(ip);
            }
            return this;
        }

        public Builder lastAction(long lastAction) {
            this.lastAction = lastAction;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public LoginPlayerData build() {
            return new LoginPlayerData(this);
        }
    }
}
