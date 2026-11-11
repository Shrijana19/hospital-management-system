package com.hospital;

import com.hospital.exception.ValidationException;
import com.hospital.model.Bill;
import com.hospital.model.Billable;
import com.hospital.service.BillingService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for billing calculations, overloaded methods, and monetary arithmetic.
 */
public class BillingServiceTest {

    private BillingService billingService;

    @BeforeEach
    public void setup() {
        billingService = new BillingService(null, null, null, null);
    }

    @Test
    @DisplayName("Overloaded calculateTotal(fee) should return exact fee formatted to 2 decimals")
    public void testCalculateTotalSingleArg() {
        BigDecimal fee = new BigDecimal("750.00");
        BigDecimal total = billingService.calculateTotal(fee);

        Assertions.assertEquals(new BigDecimal("750.00"), total);
    }

    @Test
    @DisplayName("Overloaded calculateTotal(fee, otherCharges) should accurately sum both amounts")
    public void testCalculateTotalTwoArgs() {
        BigDecimal fee = new BigDecimal("600.00");
        BigDecimal other = new BigDecimal("250.75");
        BigDecimal total = billingService.calculateTotal(fee, other);

        Assertions.assertEquals(new BigDecimal("850.75"), total);
    }

    @Test
    @DisplayName("Should handle null other charges gracefully by defaulting to zero")
    public void testCalculateTotalNullOtherCharges() {
        BigDecimal fee = new BigDecimal("500.00");
        BigDecimal total = billingService.calculateTotal(fee, null);

        Assertions.assertEquals(new BigDecimal("500.00"), total);
    }

    @Test
    @DisplayName("Should reject negative consultation fee or charges")
    public void testCalculateTotalNegativeAmounts() {
        Assertions.assertThrows(ValidationException.class, () ->
                billingService.calculateTotal(new BigDecimal("-100.00"), BigDecimal.ZERO));

        Assertions.assertThrows(ValidationException.class, () ->
                billingService.calculateTotal(new BigDecimal("100.00"), new BigDecimal("-50.00")));
    }

    @Test
    @DisplayName("Bill should implement Billable interface and calculate total correctly")
    public void testBillableAbstraction() {
        Bill bill = new Bill(1, 1, null, new BigDecimal("450.00"), new BigDecimal("150.00"), Bill.STATUS_PENDING);

        // Polymorphic reference
        Billable billable = bill;
        Assertions.assertEquals(new BigDecimal("600.00"), billable.calculateTotal());
    }
}
