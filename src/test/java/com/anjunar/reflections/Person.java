package com.anjunar.reflections;

import jakarta.validation.constraints.Size;

public class Person extends Identity {

    private String firstName;

    private String lastName;

    private boolean enabled = true;

    @Size(min = 3, max = 80)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Size(min = 3, max = 80)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
