package com.cofrete.financeworker.calculation;

public record CalculationTraceEntry(
    String category,
    String formula,
    String amount
) {
}
