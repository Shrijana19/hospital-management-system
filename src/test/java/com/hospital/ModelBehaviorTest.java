package com.hospital;

import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.Person;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests verifying Object-Oriented Programming (OOPS) foundations:
 * Encapsulation, Inheritance, Polymorphism, and Abstraction.
 */
public class ModelBehaviorTest {

    @Test
    @DisplayName("Inheritance: Patient and Doctor must extend abstract parent Person")
    public void testInheritanceHierarchy() {
        Patient patient = new Patient("Eleanor Vance", 34, "Female", "9876543211", "88 Maple Grove", "Asthma");
        Doctor doctor = new Doctor("Dr. Marcus Welby", 52, "Male", "9123456780", "12 Medical Enclave", "Cardiology", new BigDecimal("750.00"));

        Assertions.assertTrue(patient instanceof Person, "Patient must be an instance of Person");
        Assertions.assertTrue(doctor instanceof Person, "Doctor must be an instance of Person");
    }

    @Test
    @DisplayName("Encapsulation: Setters must defensively validate inputs and reject illegal state")
    public void testEncapsulationValidation() {
        Patient p = new Patient();

        // Valid setters
        p.setName("Arthur Pendelton");
        p.setAge(58);
        p.setGender("Male");
        p.setPhone("9876543210");
        p.setAddress("14 Cedar Lane");

        Assertions.assertEquals("Arthur Pendelton", p.getName());
        Assertions.assertEquals(58, p.getAge());
        Assertions.assertEquals("9876543210", p.getPhone());

        // Defensive rejection
        Assertions.assertThrows(IllegalArgumentException.class, () -> p.setName(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> p.setAge(-5));
        Assertions.assertThrows(IllegalArgumentException.class, () -> p.setAge(130));
        Assertions.assertThrows(IllegalArgumentException.class, () -> p.setGender("Alien"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> p.setPhone("12345"));
    }

    @Test
    @DisplayName("Polymorphism: Overridden displayInfo() and getSummary() invoked via Person references")
    public void testRuntimePolymorphism() {
        Person p1 = new Patient(10, "Arthur Pendelton", 58, "Male", "9876543210", "Greendale", "Hypertension", LocalDate.now());
        Person p2 = new Doctor(20, "Dr. Serena Sterling", 44, "Female", "9123456781", "Greendale", "Dermatology", new BigDecimal("600.00"), true);

        List<Person> directory = new ArrayList<>();
        directory.add(p1);
        directory.add(p2);

        // Runtime dynamic dispatch for getSummary()
        String summary1 = directory.get(0).getSummary();
        String summary2 = directory.get(1).getSummary();

        Assertions.assertTrue(summary1.contains("[Patient #10]"), "Should dispatch to Patient.getSummary()");
        Assertions.assertTrue(summary2.contains("[Doctor #20]"), "Should dispatch to Doctor.getSummary()");
        Assertions.assertTrue(summary2.contains("Dermatology"), "Doctor summary should contain specialization");

        // Runtime dynamic dispatch for displayInfo()
        String card1 = directory.get(0).displayInfo();
        String card2 = directory.get(1).displayInfo();

        Assertions.assertTrue(card1.contains("PATIENT RECORD CARD"), "Should dispatch to Patient.displayInfo()");
        Assertions.assertTrue(card2.contains("DOCTOR PROFILE CARD"), "Should dispatch to Doctor.displayInfo()");
    }
}
