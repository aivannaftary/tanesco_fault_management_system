# TANESCO Fault Management System

A web-based system for reporting, tracking, assigning, managing, and resolving customer electrical faults.

## Overview

The TANESCO Fault Management System provides a centralized platform for managing customer electrical fault complaints.

The system allows customers to report and track faults, officers to assign faults to technicians, technicians to work on and resolve assigned faults, and administrators to manage staff accounts.

## User Roles

### Customer
- Register and log in.
- Report electrical faults.
- View reported faults.
- Track fault status.
- Confirm fault resolution.

### Officer
- View customer faults.
- Search and filter faults.
- Assign technicians to faults.
- Monitor fault progress.

### Technician
- View assigned faults.
- Start working on assigned faults.
- Update fault status.
- Provide actual fault cause and resolution notes.
- Upload evidence.
- Resolve faults.

### Administrator
- View the admin dashboard.
- View staff accounts.
- Create administrators, officers, and technicians.
- Monitor system information.

## Fault Workflow

The main fault lifecycle is:

SUBMITTED → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED

1. Customer reports a fault.
2. Officer assigns a technician.
3. Technician starts working on the fault.
4. Technician resolves the fault.
5. Customer confirms the resolution.
6. Fault is closed.

## Technologies Used

### Backend
- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- JWT
- BCrypt
- Maven

### Frontend
- HTML
- CSS
- JavaScript

### Database
- MySQL

### Development Tools
- Visual Studio Code
- Postman
- Git
- GitHub

## Security

The system includes:

- JWT authentication.
- BCrypt password hashing.
- Role-based authorization.
- Protected API endpoints.
- Input validation.
- File upload validation.

## Running the Project

Start MySQL and make sure the required database configuration is available.

Run the Spring Boot application using:

```bash
mvn spring-boot:run