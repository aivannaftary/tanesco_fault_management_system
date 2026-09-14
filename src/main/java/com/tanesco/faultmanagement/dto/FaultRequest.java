package com.tanesco.faultmanagement.dto;

import com.tanesco.faultmanagement.entity.Priority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FaultRequest {

    @NotBlank(message = "Fault category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotBlank(message = "Fault description is required")
    @Size(
            min = 10,
            max = 2000,
            message = "Description must be between 10 and 2000 characters"
    )
    private String description;

    @NotBlank(message = "Fault location is required")
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @Size(max = 100, message = "Area must not exceed 100 characters")
    private String area;

    @NotNull(message = "Priority is required")
    private Priority priority;

    public FaultRequest() {
    }

    public FaultRequest(
            String category,
            String description,
            String location,
            String area,
            Priority priority
    ) {
        this.category = category;
        this.description = description;
        this.location = location;
        this.area = area;
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}