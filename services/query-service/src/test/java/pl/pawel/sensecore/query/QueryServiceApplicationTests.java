package pl.pawel.sensecore.query;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.pawel.sensecore.query.model.TelemetryReadingMapper;

@ActiveProfiles("test")
@SpringBootTest
class QueryServiceApplicationTests {

    @Autowired
    TelemetryReadingMapper telemetryReadingMapper;

    @Test
    void contextLoads() {

    }

}
