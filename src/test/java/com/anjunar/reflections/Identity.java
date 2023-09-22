package com.anjunar.reflections;

import jakarta.validation.constraints.NotBlank;

public abstract class Identity {

    @NotBlank
    public abstract String getFirstName();

    public abstract void setFirstName(String firstName);

    @NotBlank
    public abstract String getLastName();

    public abstract void setLastName(String lastName);


}
