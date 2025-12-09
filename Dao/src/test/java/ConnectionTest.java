import com.znzlspt.dao.UserDao;
import com.znzlspt.dao.util.BCryptHelper;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static com.znzlspt.dao.mapper.R2dbcMapper.executeAndSingle;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConnectionTest {

    static UserDao userDao = null;

    @BeforeAll
    public static void setup() {
       userDao = new UserDao();
    }

    @Test
    public void testCreateUser() {
        String uuid = UUID.randomUUID().toString();
        String id = "testuser";                 // nchar(12) 이내로 설정
        String password = "password123";
        String credential = BCryptHelper.hashPassword(password);

        ConnectionFactory conn = userDao.getConnectionFactory();
        String sql = ""
                + "DECLARE @return_value int, @out_result int; "
                + "EXEC @return_value = dbo.proc_create_user "
                + "@uuid = @p1, @id = @p2, @credential = @p3, @out_result = @out_result OUTPUT;"
                + "SELECT @out_result AS ret;";
        Mono<Integer> result = Mono.usingWhen(
                Mono.from(conn.create()),
                connection -> Mono.from(executeAndSingle(connection, sql, (row, meta) -> row.get("ret", Integer.class), uuid, id, credential)),
                connection -> Mono.from(connection.close())
        );

        result.subscribe(
                ret -> System.out.println("User creation result: " + ret),
                error -> System.err.println("Error: " + error.getMessage()),
                () -> System.out.println("Completed")
        );

        StepVerifier.create(result)
                .verifyComplete();
    }
}
