package com.anjunar.reflections;

import jakarta.validation.constraints.Size;

public class Person {

    @Size(min = 3, max = 80)
    private String firstName;

    @Size(min = 3, max = 80)
    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
