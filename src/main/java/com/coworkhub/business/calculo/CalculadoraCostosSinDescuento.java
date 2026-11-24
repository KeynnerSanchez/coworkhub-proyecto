package com.coworkhub.business.calculo;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Segunda implementacion (no se usa en la API, solo muestra @Qualifier): ignora el descuento del plan
@Component("calculadoraSinDescuento")
public class CalculadoraCostosSinDescuento implements CalculadoraCostos {

    @Override
    public BigDecimal costoSala(BigDecimal horasExcedentes, BigDecimal tarifaPorHora, int descuentoPorcentaje) {
        return horasExcedentes.multiply(tarifaPorHora).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal cargoCancelacion(BigDecimal costoSala) {
        return costoSala.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }
}
