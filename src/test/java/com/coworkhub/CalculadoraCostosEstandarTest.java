package com.coworkhub;

import com.coworkhub.business.calculo.CalculadoraCostosEstandar;
import com.coworkhub.business.calculo.ConsumoHoras;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraCostosEstandarTest {

    private final CalculadoraCostosEstandar calculadora = new CalculadoraCostosEstandar(50);

    @Test
    void costoSalaAplicaElDescuentoDelPlan() {
        // 2 horas excedentes * 40000 * (100 - 30) / 100 = 56000
        BigDecimal costo = calculadora.costoSala(new BigDecimal("2.00"), new BigDecimal("40000"), 30);
        assertEquals(new BigDecimal("56000.00"), costo);
    }

    @Test
    void costoSalaEsCeroSiNoHayHorasExcedentes() {
        BigDecimal costo = calculadora.costoSala(BigDecimal.ZERO, new BigDecimal("40000"), 30);
        assertEquals(new BigDecimal("0.00"), costo);
    }

    @Test
    void cargoDeCancelacionEsElCincuentaPorCiento() {
        assertEquals(new BigDecimal("35000.00"), calculadora.cargoCancelacion(new BigDecimal("70000")));
    }

    @Test
    void convierteMinutosAHoras() {
        assertEquals(new BigDecimal("1.50"), ConsumoHoras.aHoras(90));
    }
}
