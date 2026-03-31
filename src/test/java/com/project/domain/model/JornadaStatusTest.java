package com.project.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JornadaStatusTest {

    @Test
    void allValues_exist() {
        JornadaStatus[] values = JornadaStatus.values();
        assertThat(values).containsExactlyInAnyOrder(
                JornadaStatus.GOALS,
                JornadaStatus.ABSENT,
                JornadaStatus.PENDING,
                JornadaStatus.NO_PRESENTADO
        );
    }

    @Test
    void valueOf_returnsCorrectConstant() {
        assertThat(JornadaStatus.valueOf("GOALS")).isEqualTo(JornadaStatus.GOALS);
        assertThat(JornadaStatus.valueOf("ABSENT")).isEqualTo(JornadaStatus.ABSENT);
        assertThat(JornadaStatus.valueOf("PENDING")).isEqualTo(JornadaStatus.PENDING);
        assertThat(JornadaStatus.valueOf("NO_PRESENTADO")).isEqualTo(JornadaStatus.NO_PRESENTADO);
    }
}
