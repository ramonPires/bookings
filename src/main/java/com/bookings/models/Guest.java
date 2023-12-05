package com.bookings.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;


public class Guest {
    @Size(min = 3, max = 50)
    @Column(name = "guest_first_name",nullable = false)
    @NotBlank(message = "First name can't be in blank")
    @NotEmpty(message = "First name can't be empty")
    @JsonProperty("guestFirstName")
    private String firstName;

    @Size(min = 3, max = 50)
    @Column(name = "guest_last_name",nullable = false)
    @NotBlank(message = "Last name can't be in blank")
    @NotEmpty(message = "Last name can't be empty")
    @JsonProperty("guestLastName")
    private String lastName;
    @Column(name = "guest_age",nullable = false)
    @Min(18)
    @JsonProperty("guestAge")
    private int age;

    @NotBlank(message = "Social security id can't be in blank")
    @NotEmpty(message = "Social security id can't be empty")
    @Column(name = "guest_social_security_id", nullable = false)
    @JsonProperty("guestSocialSecurityId")
    private String socialSecurityId;

    @Deprecated
    public Guest() {}
    public Guest(String firstName, String lastName, int age, String socialSecurityId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.socialSecurityId = socialSecurityId;
    }

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSocialSecurityId() {
        return socialSecurityId;
    }

    public void setSocialSecurityId(String socialSecurityId) {
        this.socialSecurityId = socialSecurityId;
    }

    @Override
    public String toString() {
        return "Guest{" +
                " firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", socialSecurityId='" + socialSecurityId + '\'' +
                '}';
    }
}
