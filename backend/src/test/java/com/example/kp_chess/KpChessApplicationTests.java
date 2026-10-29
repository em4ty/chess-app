package com.example.kp_chess;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Поднимает весь контекст и ходит в PostgreSQL, поэтому включайте вручную, когда БД запущена. */
@Disabled("Требуется запущенная PostgreSQL")
@SpringBootTest
class KpChessApplicationTests {

    @Test
    void contextLoads() {
    }

}
