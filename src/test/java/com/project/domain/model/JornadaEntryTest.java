package com.project.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JornadaEntryTest {

    @Test
    void played_withGoalsStatus_returnsTrue() {
        JornadaEntry entry = new JornadaEntry(JornadaStatus.GOALS, 2);
        assertThat(entry.played()).isTrue();
    }

    @Test
    void played_withGoalsStatusZeroGoals_returnsTrue() {
        JornadaEntry entry = new JornadaEntry(JornadaStatus.GOALS, 0);
        assertThat(entry.played()).isTrue();
    }

    @Test
    void played_withAbsentStatus_returnsFalse() {
        JornadaEntry entry = new JornadaEntry(JornadaStatus.ABSENT, 0);
        assertThat(entry.played()).isFalse();
    }

    @Test
    void played_withPendingStatus_returnsFalse() {
        JornadaEntry entry = new JornadaEntry(JornadaStatus.PENDING, 0);
        assertThat(entry.played()).isFalse();
    }

    @Test
    void played_withNoPresentadoStatus_returnsFalse() {
        JornadaEntry entry = new JornadaEntry(JornadaStatus.NO_PRESENTADO, 0);
        assertThat(entry.played()).isFalse();
    }

    @Test
    void goals_storedCorrectly() {
        JornadaEntry entry = new JornadaEntry(JornadaStatus.GOALS, 3);
        assertThat(entry.goals()).isEqualTo(3);
    }

    @Test
    void status_storedCorrectly() {
        JornadaEntry entry = new JornadaEntry(JornadaStatus.ABSENT, 0);
        assertThat(entry.status()).isEqualTo(JornadaStatus.ABSENT);
    }
}
