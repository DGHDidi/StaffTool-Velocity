package org.dghdidi.stafftool.util;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import org.dghdidi.stafftool.StaffTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRegistrar {
    private static final List<String> REGISTERED = new ArrayList<>();

    public static void register(String name, SimpleCommand command, String... aliases) {
        CommandMeta meta = StaffTool.proxy.getCommandManager()
                .metaBuilder(name)
                .aliases(aliases)
                .plugin(StaffTool.plugin)
                .build();
        StaffTool.proxy.getCommandManager().register(meta, command);
        REGISTERED.add(name);
        REGISTERED.addAll(Arrays.asList(aliases));
    }

    public static void unregisterAll() {
        for (String name : REGISTERED) {
            StaffTool.proxy.getCommandManager().unregister(name);
        }
        REGISTERED.clear();
    }
}
