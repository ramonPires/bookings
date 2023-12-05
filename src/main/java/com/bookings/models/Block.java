package com.bookings.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "blocks", uniqueConstraints =  { @UniqueConstraint(columnNames = { "property_id", "start_date" , "end_date"}) } )
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Future(message = "Start date need to be in the future")
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", columnDefinition = "DATE", nullable = false)
    private LocalDate startDate;
    @Future(message = "End date need to be in the future")
    @NotNull(message = "End date is required")
    @Column(name = "end_date", columnDefinition = "DATE", nullable = false)
    private LocalDate endDate;

    @JsonIgnore
    @NotNull(message = "Property is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Deprecated
    public Block() {
    }

    public Block(LocalDate startDate, LocalDate endDate, Property property) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.property = property;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    @JsonIgnore
    public boolean hasValidBlockDates() {
        return this.startDate.isBefore(this.endDate);
    }

    @JsonGetter("propertyId")
    public Long getPropertyId() {
        return property.getId();
    }

    @JsonSetter("propertyId")
    public void setPropertyId(Long propertyId) {
        if (property == null) {
            this.property = new Property(propertyId);
        } else {
            property.setId(propertyId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return Objects.equals(id, block.id) && Objects.equals(startDate, block.startDate) && Objects.equals(endDate, block.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startDate, endDate);
    }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", property=" + property +
                '}';
    }
}
