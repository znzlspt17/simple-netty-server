// File: `Dao/src/main/java/com/znzlspt/dao/DaoModule.java`
package com.znzlspt.dao;

import com.znzlspt.dao.model.LocalUser;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.znzlspt.dao.mapper.R2dbcMapper.executeAndMulti;
import static com.znzlspt.dao.mapper.R2dbcMapper.executeAndSingle;

/**
 * DAO 모듈의 진입점을 담당할 클래스입니다.
 */
public final class DaoModule {

    private static volatile DaoModule instance;
    private ConnectionFactory conn;


    private DaoModule() {
        conn = DaoConnectionPool.connectionFactory();
    }

    public static DaoModule getInstance() {
        if (instance == null) {
            synchronized (DaoModule.class) {
                if (instance == null) {
                    instance = new DaoModule();
                }
            }
        }
        return instance;
    }

    public ConnectionFactory getConnectionFactory() {
        return conn;
    }

    public Mono<Integer> createUser(String uuid, String id, String credential) {
        String sql = ""
                + "DECLARE @ret int, @result int; "
                + "EXEC @ret = dbo.proc_create_user "
                + "@uuid = @p1, @id = @p2, @credential = @p3, @out_result = @result OUTPUT;"
                + "SELECT @result;";

        return Mono.usingWhen(
                Mono.from(conn.create()),
                connection -> Mono.from(executeAndSingle(connection, sql, (row, meta) -> row.get("ret", Integer.class), uuid, id, credential)),
                connection -> Mono.from(connection.close())
        );
    }

    public Mono<LocalUser> loginUser(String id, String credential) {
        String sql = ""
                + "DECLARE @ret int, @result int; "
                + "EXEC @ret = dbo.proc_login_user "
                + "@id = @p1, @credential = @p2, @out_result = @result OUTPUT;"
                + "SELECT @result;";

        return Mono.usingWhen(
                Mono.from(conn.create()),
                connection -> Mono.from(executeAndSingle(connection, sql, LocalUser::fromRow, id, credential)),
                connection -> Mono.from(connection.close())
        );
    }

    public Mono<Integer> logoutUser(UUID uuid) {
        String sql = ""
                + "DECLARE @ret int, @result int; "
                + "EXEC @ret = dbo.proc_logout_user "
                + "@uuid = @p1 @out_result = @result OUTPUT;"
                + "SELECT @result;";

        return Mono.usingWhen(
                Mono.from(conn.create()),
                connection -> Mono.from(executeAndSingle(connection, sql, (row, meta) -> row.get("ret", Integer.class), uuid)),
                connection -> Mono.from(connection.close())
        );
    }

    public Flux<LocalUser> selectAllUsers() {
        String sql = ""
                + "DECLARE @ret int, @result int; "
                + "EXEC @ret = dbo.proc_select_all_users "
                + "SELECT @result;";

        return Flux.usingWhen(
                Mono.from(conn.create()),
                connection -> Flux.from(executeAndMulti(connection, sql, LocalUser::fromRow)),
                connection -> Mono.from(connection.close())
        );
    }

    public Mono<Integer> insertChatLog(UUID uuid, int type, String chat) {
        String sql = ""
                + "DECLARE @ret int, @result int; "
                + "EXEC dbo.proc_insert_chat_log @uuid = @p1, @type = @p2, @chat = @p3, @out_result = @result OUTPUT; "
                + "SELECT @result;";

        return Mono.usingWhen(
                Mono.from(conn.create()),
                connection -> Mono.from(executeAndSingle(connection, sql, (row, meta) -> row.get("ret", Integer.class))),
                connection -> Mono.from(connection.close())
        );
    }

}
