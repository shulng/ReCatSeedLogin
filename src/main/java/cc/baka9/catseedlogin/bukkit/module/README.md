# Bukkit modules

The Bukkit entry point wires feature modules through `BukkitModuleManager`.
Each module owns one lifecycle concern and can be enabled or disabled independently:

- `ListenerModule`: Bukkit event listeners.
- `CommandModule`: command executors and tab completion.
- `RuntimeModule`: scheduled tasks and proxy communication socket.
- `DatabaseModule`: database selection, schema initialization, cache hydration, and shutdown.
- `IntegrationModule`: optional ProtocolLib support, TOTP bootstrap, and legacy-plugin checks.

Modules are enabled in registration order and disabled in reverse order. A failed
enable rolls back every module that was already started.

The expected dependency order is `DatabaseModule`, `IntegrationModule`,
`ListenerModule`, `CommandModule`, then `RuntimeModule`. The Bukkit entry point
only wires these modules and owns platform-level configuration setup; feature
lifecycle code belongs in the module that owns it.
