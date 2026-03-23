package com.retailrewards;

import org.junit.jupiter.api.Test;

class RetailRewardServiceApplicationTest {

    @Test
    void shouldRunMainMethod() {
        RetailRewardServiceApplication.main(new String[] {
                "--spring.main.web-application-type=none",
                "--spring.main.banner-mode=off",
                "--spring.main.lazy-initialization=true"
        });
    }
}
