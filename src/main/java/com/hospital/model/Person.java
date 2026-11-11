package com.hospital.model;

/**
 * Abstract base class representing an individual within the hospital ecosystem.
 * Demonstrates:
 * 1. Abstraction: Cannot be instantiated directly; provides common state & abstract contracts.
 * 2. Inheritance: Extended by Patient and Doctor.
 * 3. Encapsulation: All fields are private, accessed via public getters and validating setters.
 */
public abstract class Person {

    private int id;
    private String name;
    private int age;
    private String gender;
    private String phone;
    private String address;

    /**
     * Default constructor.
     */
    public Person() {
    }

    /**
     * Parameterized constructor.
     */
    public Person(int id, String name, int age, String gender, String phone, String address) {
        setId(id);
        setName(name);
        setAge(age);
        setGender(gender);
        setPhone(phone);
        setAddress(address);
    }

    // -------------------------------------------------------------------------
    // Abstract Methods (Polymorphism - Overridden by Patient and Doctor)
    // -------------------------------------------------------------------------

    /**
     * Returns a one-line concise summary of this person suitable for directory lists.
     */
    public abstract String getSummary();

    /**
     * Returns a multi-line formatted profile card for this person.
     */
    public abstract String displayInfo();

    // -------------------------------------------------------------------------
    // Getters and Validating Setters (Encapsulation)
    // -------------------------------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Person ID cannot be negative");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (name.trim().length() < 2 || name.trim().length() > 100) {
            throw new IllegalArgumentException("Name length must be between 2 and 100 characters");
        }
        this.name = name.trim();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age < 0 || age > 125) {
            throw new IllegalArgumentException("Age must be between 0 and 125 years (provided: " + age + ")");
        }
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be null or empty");
        }
        String g = gender.trim();
        if (!g.equalsIgnoreCase("Male") && !g.equalsIgnoreCase("Female") && !g.equalsIgnoreCase("Other")) {
            throw new IllegalArgumentException("Gender must be 'Male', 'Female', or 'Other'");
        }
        this.gender = g.substring(0, 1).toUpperCase() + g.substring(1).toLowerCase();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.length() != 10) {
            throw new IllegalArgumentException("Phone number must contain exactly 10 digits (provided: '" + phone + "')");
        }
        this.phone = cleaned;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        this.address = address.trim();
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
