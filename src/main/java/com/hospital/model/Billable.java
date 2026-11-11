package com.hospital.model;

import java.math.BigDecimal;

/**
 * Abstraction representing any commercial or medical service that generates a billable charge.
 * Demonstrates Abstraction in OOP by defining an invariant contract for financial calculations.
 */
public interface Billable {

    /**
     * Calculates the total amount payable for this billable item.
     *
     * @return Non-negative BigDecimal representing the total cost.
     */
    BigDecimal calculateTotal();
}
