package org.dghdidi.stafftool.database.table;

import org.dghdidi.stafftool.StaffTool;

import static org.dghdidi.stafftool.StaffTool.databaseManager;
import static org.dghdidi.stafftool.database.DatabaseManager.identityColumnDefinition;

public class ChatLogTable {

    public static void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS chat_log (
                    id %s,
                    player_name VARCHAR(64) NOT NULL,
                    message VARCHAR(512) NOT NULL,
                    server_name VARCHAR(64),
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """.formatted(identityColumnDefinition());
        databaseManager.executeUpdate(sql);

        String indexSql = """
                CREATE INDEX idx_chat_log_player_time
                ON chat_log(player_name, created_at)
                """;
        databaseManager.createIndex(indexSql);

        StaffTool.logger.info("§a聊天记录表已创建或已存在");
    }

}
