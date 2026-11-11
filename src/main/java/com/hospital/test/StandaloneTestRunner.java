package com.hospital.test;

import com.hospital.exception.ValidationException;
import com.hospital.model.Appointment;
import com.hospital.model.Bill;
import com.hospital.model.Billable;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.Person;
import com.hospital.service.BillingService;
import com.hospital.service.Validator;
import com.hospital.util.PasswordHasher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Standalone Automated Test Runner.
 * Executes the entire test suite without requiring an external build tool or Maven installed.
 * Useful for viva demonstrations, quick testing, and automated continuous checks.
 */
public class StandaloneTestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     HOSPITAL MANAGEMENT SYSTEM - STANDALONE AUTOMATED TEST SUITE               ");
        System.out.println("================================================================================");

        runTest("OOP: Inheritance (Patient and Doctor extend Person)", () -> {
            Patient p = new Patient("Test Patient", 30, "Male", "9876543210", "Greendale", "None");
            Doctor d = new Doctor("Dr. Test", 45, "Female", "9876543211", "Greendale", "Cardiology", new BigDecimal("500.00"));
            assertTrue(p instanceof Person, "Patient must extend Person");
            assertTrue(d instanceof Person, "Doctor must extend Person");
        });

        runTest("OOP: Encapsulation (Setters validate inputs defensively)", () -> {
            Patient p = new Patient();
            p.setName("Valid Name");
            p.setAge(40);
            p.setPhone("9876543210");
            assertEquals("Valid Name", p.getName());
            assertEquals(40, p.getAge());
            assertEquals("9876543210", p.getPhone());

            assertThrows(IllegalArgumentException.class, () -> p.setAge(-1), "Negative age should fail");
            assertThrows(IllegalArgumentException.class, () -> p.setAge(130), "Age > 125 should fail");
            assertThrows(IllegalArgumentException.class, () -> p.setName(""), "Empty name should fail");
            assertThrows(IllegalArgumentException.class, () -> p.setPhone("123"), "Phone != 10 digits should fail");
        });

        runTest("OOP: Runtime Polymorphism (Overriding displayInfo and getSummary)", () -> {
            Person p1 = new Patient(1, "Arthur", 58, "Male", "9876543210", "Greendale", "Hypertension", LocalDate.now());
            Person p2 = new Doctor(2, "Dr. Welby", 52, "Male", "9123456780", "Greendale", "Cardiology", new BigDecimal("750.00"), true);

            List<Person> list = new ArrayList<>();
            list.add(p1);
            list.add(p2);

            assertTrue(list.get(0).getSummary().contains("[Patient #1]"), "Dynamic dispatch failed for Patient");
            assertTrue(list.get(1).getSummary().contains("[Doctor #2]"), "Dynamic dispatch failed for Doctor");
            assertTrue(list.get(0).displayInfo().contains("PATIENT RECORD CARD"), "Dynamic dispatch displayInfo Patient");
            assertTrue(list.get(1).displayInfo().contains("DOCTOR PROFILE CARD"), "Dynamic dispatch displayInfo Doctor");
        });

        runTest("OOP: Abstraction (Billable interface and calculateTotal contract)", () -> {
            Bill bill = new Bill(1, 1, null, new BigDecimal("600.00"), new BigDecimal("150.00"), Bill.STATUS_PENDING);
            Billable billable = bill; // Upcast to interface
            assertEquals(new BigDecimal("750.00"), billable.calculateTotal());
        });

        runTest("OOP: Method Overloading (BillingService.calculateTotal with 1 and 2 params)", () -> {
            BillingService service = new BillingService(null, null, null, null);
            BigDecimal total1 = service.calculateTotal(new BigDecimal("700.00"));
            BigDecimal total2 = service.calculateTotal(new BigDecimal("700.00"), new BigDecimal("350.50"));

            assertEquals(new BigDecimal("700.00"), total1);
            assertEquals(new BigDecimal("1050.50"), total2);
        });

        runTest("Billing: Rejection of negative amounts", () -> {
            BillingService service = new BillingService(null, null, null, null);
            assertThrows(ValidationException.class, () ->
                    service.calculateTotal(new BigDecimal("-100.00"), BigDecimal.ZERO), "Negative fee rejected");
            assertThrows(ValidationException.class, () ->
                    service.calculateTotal(new BigDecimal("100.00"), new BigDecimal("-50.00")), "Negative other rejected");
        });

        runTest("Validator: Patient and Doctor Age Rules", () -> {
            assertEquals(0, Validator.validatePatientAge(0));
            assertEquals(125, Validator.validatePatientAge(125));
            assertThrows(ValidationException.class, () -> Validator.validatePatientAge(-1), "Negative age rejected");
            assertThrows(ValidationException.class, () -> Validator.validatePatientAge(126), "Age > 125 rejected");

            assertEquals(21, Validator.validateDoctorAge(21));
            assertEquals(100, Validator.validateDoctorAge(100));
            assertThrows(ValidationException.class, () -> Validator.validateDoctorAge(20), "Doctor < 21 rejected");
            assertThrows(ValidationException.class, () -> Validator.validateDoctorAge(101), "Doctor > 100 rejected");
        });

        runTest("Validator: Phone format (exactly 10 digits)", () -> {
            assertEquals("9876543210", Validator.validatePhone("9876543210", "Phone"));
            assertEquals("9876543210", Validator.validatePhone("987-654-3210", "Phone"));
            assertThrows(ValidationException.class, () -> Validator.validatePhone("987654321", "Phone"), "9 digits rejected");
            assertThrows(ValidationException.class, () -> Validator.validatePhone("98765432100", "Phone"), "11 digits rejected");
        });

        runTest("Validator: Past Appointment Dates Rejected", () -> {
            LocalDate future = LocalDate.now().plusDays(2);
            LocalDate past = LocalDate.now().minusDays(1);
            assertEquals(future, Validator.validateAppointmentDate(future));
            assertThrows(ValidationException.class, () -> Validator.validateAppointmentDate(past), "Past date rejected");
        });

        runTest("Appointment: Status State Transitions", () -> {
            Appointment a = new Appointment(1, 2, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
            assertEquals(Appointment.STATUS_SCHEDULED, a.getStatus());
            a.setStatus(Appointment.STATUS_COMPLETED);
            assertEquals(Appointment.STATUS_COMPLETED, a.getStatus());
            assertThrows(IllegalArgumentException.class, () -> a.setStatus("InvalidStatus"), "Invalid status rejected");
        });

        runTest("Security: PBKDF2 Password Hashing & Salt Verification", () -> {
            String password = "HospitalSecretPassword123";
            String salt = PasswordHasher.generateSalt();
            String hash = PasswordHasher.hashPassword(password, salt);

            assertTrue(PasswordHasher.verifyPassword(password, hash, salt), "Valid password verified");
            assertTrue(!PasswordHasher.verifyPassword("WrongPassword", hash, salt), "Invalid password rejected");
        });

        System.out.println("================================================================================");
        System.out.printf("TEST SUMMARY: %d Passed | %d Failed | Total: %d\n", passed, failed, passed + failed);
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, Runnable testRunnable) {
        try {
            testRunnable.run();
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } catch (Throwable t) {
            System.out.printf("  [FAIL] %s: %s\n", testName, t.getMessage());
            failed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Expected '" + expected + "' but got '" + actual + "'");
    }

    private static void assertThrows(Class<? extends Throwable> expectedException, Runnable action, String message) {
        try {
            action.run();
            throw new AssertionError("Expected exception " + expectedException.getSimpleName() + " was NOT thrown: " + message);
        } catch (Throwable t) {
            if (!expectedException.isInstance(t)) {
                throw new AssertionError("Expected " + expectedException.getSimpleName() + " but caught " + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
    }
}
