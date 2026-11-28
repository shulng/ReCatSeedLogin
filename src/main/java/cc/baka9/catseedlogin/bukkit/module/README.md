# Bukkit modules

The Bukkit entry point wires feature modules through `BukkitModuleManager`.
Each module owns one lifecycle concern and can be enabled or disabled independently:

- `ListenerModule`: Bukkit event listeners.
- `CommandModule`: command executors and tab completion.
- `RuntimeModule`: scheduled tasks and proxy communication socket.

Modules are enabled in registration order and disabled in reverse order. A failed
enable rolls back every module that was already started.
