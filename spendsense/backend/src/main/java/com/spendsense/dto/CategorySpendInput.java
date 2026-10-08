package com.spendsense.dto;

import java.math.BigDecimal;

public record CategorySpendInput(String categoryName, BigDecimal amount) {}
