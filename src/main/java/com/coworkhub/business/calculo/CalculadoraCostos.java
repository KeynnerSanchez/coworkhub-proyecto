package com.coworkhub.business.calculo;

import java.math.BigDecimal;

// Reglas de dinero (RN-07 y RN-08). Hay dos implementaciones, por eso se usa @Primary y @Qualifier
public interface CalculadoraCostos {

    // Costo de la sala: solo se cobran las horas que exceden las del plan, con el descuento del plan
    BigDecimal costoSala(BigDecimal horasExcedentes, BigDecimal tarifaPorHora, int descuentoPorcentaje);

    // Cargo cuando se cancela con poca anticipacion
    BigDecimal cargoCancelacion(BigDecimal costoSala);
}
