import at.favre.lib.crypto.bcrypt.BCrypt;
import com.znzlspt.dao.DaoConnectionPool;
import com.znzlspt.dao.DaoModule;
import com.znzlspt.dao.util.BCryptHelper;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static com.znzlspt.dao.mapper.R2dbcMapper.executeAndSingle;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConnectionTest {

    static DaoModule daoModule = null;

    @BeforeAll
    public static void reflect() {
        Class clazz;
        try {
            clazz = Class.forName("com.znzlspt.dao.DaoModule");
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            daoModule = (DaoModule) constructor.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateUser() {
        String uuid = UUID.randomUUID().toString();
        String id = "testuser";                 // nchar(12) 이내로 설정
        String password = "password123";
        String credential = BCryptHelper.hashPassword(password);

        ConnectionFactory conn = daoModule.getConnectionFactory();

        String sql = ""
                + "DECLARE @return_value int, @out_result int; "
                + "EXEC @return_value = dbo.proc_create_user "
                + "@uuid = @p1, @id = @p2, @credential = @p3, @result = @out_result OUTPUT;"
                + "SELECT @out_result AS ret;";
        Mono<Integer> result = Mono.usingWhen(
                Mono.from(conn.create()),
                connection -> Mono.from(executeAndSingle(connection, sql, (row, meta) -> row.get("ret", Integer.class), uuid, id, credential)),
                connection -> Mono.from(connection.close())
        );

        StepVerifier.create(result)
                .assertNext(ret -> {
                    assertNotNull(ret);
                    System.out.println("User creation result: " + ret);
                })
                .verifyComplete();
    }
}
