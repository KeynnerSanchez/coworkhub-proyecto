package com.coworkhub.business.calculo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Primary
@Component("calculadoraEstandar")
public class CalculadoraCostosEstandar implements CalculadoraCostos {

    private final int porcentajeCancelacion;

    public CalculadoraCostosEstandar(@Value("${app.cancelacion.porcentaje}") int porcentajeCancelacion) {
        this.porcentajeCancelacion = porcentajeCancelacion;
    }

    @Override
    public BigDecimal costoSala(BigDecimal horasExcedentes, BigDecimal tarifaPorHora, int descuentoPorcentaje) {
        BigDecimal factor = BigDecimal.valueOf(100 - descuentoPorcentaje).divide(BigDecimal.valueOf(100));
        return horasExcedentes.multiply(tarifaPorHora).multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal cargoCancelacion(BigDecimal costoSala) {
        return costoSala.multiply(BigDecimal.valueOf(porcentajeCancelacion))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
