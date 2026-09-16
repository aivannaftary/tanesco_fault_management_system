/* =========================================================
   TANESCO FAULT MANAGEMENT SYSTEM
   ADMIN PORTAL
========================================================= */

let adminUser = null;
let adminStaff = [];


/* =========================================================
   INITIALIZATION
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        adminUser = requireAdmin();

        if (!adminUser) {
            return;
        }

        displayAdmin(adminUser);

        const page =
            document.body.dataset.page;

        try {

            if (page === "admin-dashboard") {

                await loadAdminDashboard();
            }

            if (page === "admin-staff") {

                await loadStaffPage();
            }

            if (page === "admin-create-staff") {

                setupCreateStaffForm();
            }

        } catch (error) {

            console.error(
                "Admin portal error:",
                error
            );

            showAdminMessage(
                error.message ||
                "Unable to load admin information."
            );
        }
    }
);


/* =========================================================
   SECURITY
========================================================= */

function requireAdmin() {

    const token = getToken();
    const user = getUser();

    if (!token || !user) {

        window.location.href = "/";
        return null;
    }

    const role =
        String(user.role || "")
            .replace("ROLE_", "")
            .toUpperCase();

    if (role !== "ADMIN") {

        switch (role) {

            case "CUSTOMER":

                window.location.href =
                    "/customer/dashboard.html";

                break;


            case "TECHNICIAN":

                window.location.href =
                    "/technician/dashboard.html";

                break;


            case "OFFICER":

                window.location.href =
                    "/officer/dashboard.html";

                break;


            default:

                logout();
        }

        return null;
    }

    return user;
}


/* =========================================================
   ADMIN PROFILE
========================================================= */

function displayAdmin(user) {

    const fullName =
        user.fullName ||
        user.username ||
        "Administrator";

    setAdminText(
        "userFullName",
        fullName
    );

    setAdminText(
        "userRole",
        "ADMIN"
    );


    const avatar =
        document.getElementById(
            "userAvatar"
        );

    if (avatar) {

        avatar.textContent =
            adminInitials(fullName);
    }


    const welcome =
        document.getElementById(
            "welcomeMessage"
        );

    if (welcome) {

        welcome.textContent =
            `Welcome back, ${fullName}`;
    }
}


function adminInitials(name) {

    const parts =
        String(name)
            .trim()
            .split(/\s+/)
            .filter(Boolean);

    if (parts.length === 0) {
        return "A";
    }

    if (parts.length === 1) {

        return parts[0]
            .charAt(0)
            .toUpperCase();
    }

    return (
        parts[0].charAt(0) +
        parts[parts.length - 1].charAt(0)
    ).toUpperCase();
}


/* =========================================================
   API - STAFF
========================================================= */

async function fetchAdminStaff() {

    const response =
        await apiRequest(
            "/admin/staff"
        );

    const data =
        await readResponse(
            response
        );

    if (!response.ok) {

        throw new Error(
            adminBackendMessage(data)
        );
    }

    return Array.isArray(data)
        ? data
        : [];
}


/* =========================================================
   API - DASHBOARD
========================================================= */

async function fetchAdminDashboard() {

    const response =
        await apiRequest(
            "/staff/dashboard"
        );

    const data =
        await readResponse(
            response
        );

    if (!response.ok) {

        throw new Error(
            adminBackendMessage(data)
        );
    }

    return data || {};
}


/* =========================================================
   ADMIN DASHBOARD
========================================================= */

async function loadAdminDashboard() {

    /*
     * Load dashboard and staff separately.
     * If dashboard statistics fail, staff
     * statistics can still be displayed.
     */

    try {

        const dashboard =
            await fetchAdminDashboard();

        displayDashboardStatistics(
            dashboard
        );

    } catch (error) {

        console.error(
            "Dashboard statistics error:",
            error
        );
    }


    adminStaff =
        await fetchAdminStaff();


    displayStaffStatistics(
        adminStaff
    );
}


/* =========================================================
   DASHBOARD STATISTICS
========================================================= */

function displayDashboardStatistics(data) {

    /*
     * Supports several possible naming styles
     * while keeping the frontend compatible
     * with the current DashboardResponse.
     */

    setAdminText(
        "totalFaults",
        dashboardValue(
            data,
            "totalFaults",
            "total"
        )
    );

    setAdminText(
        "submittedFaults",
        dashboardValue(
            data,
            "submittedFaults",
            "submitted"
        )
    );

    setAdminText(
        "assignedFaults",
        dashboardValue(
            data,
            "assignedFaults",
            "assigned"
        )
    );

    setAdminText(
        "inProgressFaults",
        dashboardValue(
            data,
            "inProgressFaults",
            "inProgress"
        )
    );

    setAdminText(
        "resolvedFaults",
        dashboardValue(
            data,
            "resolvedFaults",
            "resolved"
        )
    );

    setAdminText(
        "closedFaults",
        dashboardValue(
            data,
            "closedFaults",
            "closed"
        )
    );
}


function dashboardValue(
    data,
    ...keys
) {

    for (const key of keys) {

        if (
            data[key] !== undefined &&
            data[key] !== null
        ) {

            return data[key];
        }
    }

    return 0;
}


/* =========================================================
   STAFF STATISTICS
========================================================= */

function displayStaffStatistics(staff) {

    setAdminText(
        "totalStaff",
        staff.length
    );

    setAdminText(
        "adminCount",
        countStaffRole(
            staff,
            "ADMIN"
        )
    );

    setAdminText(
        "officerCount",
        countStaffRole(
            staff,
            "OFFICER"
        )
    );

    setAdminText(
        "technicianCount",
        countStaffRole(
            staff,
            "TECHNICIAN"
        )
    );
}


function countStaffRole(
    staff,
    role
) {

    return staff.filter(
        member =>
            String(member.role)
                .toUpperCase() === role
    ).length;
}


/* =========================================================
   STAFF MANAGEMENT PAGE
========================================================= */

async function loadStaffPage() {

    adminStaff =
        await fetchAdminStaff();

    renderStaff(
        adminStaff
    );


    document
        .getElementById(
            "staffSearch"
        )
        ?.addEventListener(
            "input",
            filterStaff
        );


    document
        .getElementById(
            "roleFilter"
        )
        ?.addEventListener(
            "change",
            filterStaff
        );
}


/* =========================================================
   FILTER STAFF
========================================================= */

function filterStaff() {

    const search =
        String(
            document.getElementById(
                "staffSearch"
            )?.value || ""
        )
            .trim()
            .toLowerCase();


    const role =
        document.getElementById(
            "roleFilter"
        )?.value || "";


    const filtered =
        adminStaff.filter(
            member => {

                const searchable =
                    [
                        member.fullName,
                        member.username,
                        member.email
                    ]
                        .filter(Boolean)
                        .join(" ")
                        .toLowerCase();


                const matchesSearch =
                    !search ||
                    searchable.includes(
                        search
                    );


                const matchesRole =
                    !role ||
                    member.role === role;


                return (
                    matchesSearch &&
                    matchesRole
                );
            }
        );


    renderStaff(filtered);
}


/* =========================================================
   RENDER STAFF TABLE
========================================================= */

function renderStaff(staff) {

    const body =
        document.getElementById(
            "staffTableBody"
        );

    if (!body) {
        return;
    }


    if (!staff ||
        staff.length === 0) {

        body.innerHTML = `

            <tr>

                <td
                    colspan="5"
                    class="empty-cell"
                >
                    No staff accounts found.
                </td>

            </tr>

        `;

        return;
    }


    body.innerHTML =
        staff.map(member => `

            <tr>

                <td>
                    ${adminEscape(
                        member.id
                    )}
                </td>

                <td>
                    <strong>
                        ${adminEscape(
                            member.fullName
                        )}
                    </strong>
                </td>

                <td>
                    ${adminEscape(
                        member.username
                    )}
                </td>

                <td>
                    ${adminEscape(
                        member.email
                    )}
                </td>

                <td>

                    <span class="
                        status-badge
                        ${adminRoleClass(
                            member.role
                        )}
                    ">

                        ${adminFormatValue(
                            member.role
                        )}

                    </span>

                </td>

            </tr>

        `).join("");
}


/* =========================================================
   CREATE STAFF FORM
========================================================= */

function setupCreateStaffForm() {

    const form =
        document.getElementById(
            "createStaffForm"
        );

    if (!form) {
        return;
    }


    form.addEventListener(
        "submit",
        async event => {

            event.preventDefault();

            clearAdminMessage();


            const fullName =
                document
                    .getElementById(
                        "fullName"
                    )
                    .value
                    .trim();


            const username =
                document
                    .getElementById(
                        "username"
                    )
                    .value
                    .trim();


            const email =
                document
                    .getElementById(
                        "email"
                    )
                    .value
                    .trim();


            const role =
                document
                    .getElementById(
                        "role"
                    )
                    .value;


            const password =
                document
                    .getElementById(
                        "password"
                    )
                    .value;


            const confirmPassword =
                document
                    .getElementById(
                        "confirmPassword"
                    )
                    .value;


            if (
                password !==
                confirmPassword
            ) {

                showAdminMessage(
                    "Passwords do not match."
                );

                return;
            }


            if (password.length < 8) {

                showAdminMessage(
                    "Password must contain at least 8 characters."
                );

                return;
            }


            if (!role) {

                showAdminMessage(
                    "Select a staff role."
                );

                return;
            }


            const button =
                document.getElementById(
                    "createStaffButton"
                );


            button.disabled = true;

            button.textContent =
                "Creating Account...";


            try {

                const response =
                    await apiRequest(
                        "/admin/staff",
                        {
                            method: "POST",

                            body:
                                JSON.stringify({
                                    username,
                                    fullName,
                                    email,
                                    password,
                                    role
                                })
                        }
                    );


                const data =
                    await readResponse(
                        response
                    );


                if (!response.ok) {

                    showAdminMessage(
                        adminBackendMessage(
                            data
                        )
                    );

                    return;
                }


                showAdminMessage(
                    `${adminFormatValue(
                        data.role
                    )} account created successfully.`,
                    "success"
                );


                form.reset();


            } catch (error) {

                console.error(
                    "Create staff error:",
                    error
                );


                showAdminMessage(
                    "Unable to create the staff account."
                );


            } finally {

                button.disabled = false;

                button.textContent =
                    "Create Staff Account";
            }
        }
    );
}


/* =========================================================
   ROLE STYLING
========================================================= */

function adminRoleClass(role) {

    switch (
        String(role || "")
            .toUpperCase()
    ) {

        case "ADMIN":
            return "status-resolved";

        case "OFFICER":
            return "status-assigned";

        case "TECHNICIAN":
            return "status-in-progress";

        default:
            return "status-submitted";
    }
}


/* =========================================================
   GENERAL HELPERS
========================================================= */

function setAdminText(
    id,
    value
) {

    const element =
        document.getElementById(id);

    if (element) {

        element.textContent =
            value ?? 0;
    }
}


function showAdminMessage(
    text,
    type = "error"
) {

    const element =
        document.getElementById(
            "message"
        );

    if (!element) {
        return;
    }

    element.textContent = text;

    element.className =
        `message ${type}`;
}


function clearAdminMessage() {

    const element =
        document.getElementById(
            "message"
        );

    if (!element) {
        return;
    }

    element.textContent = "";

    element.className =
        "message";
}


function adminFormatValue(value) {

    if (!value) {
        return "-";
    }

    return String(value)
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(
            /\b\w/g,
            character =>
                character.toUpperCase()
        );
}


function adminBackendMessage(data) {

    if (!data) {
        return "Request failed.";
    }


    if (
        data.validationErrors &&
        typeof data.validationErrors ===
        "object"
    ) {

        return Object.values(
            data.validationErrors
        ).join(" ");
    }


    return (
        data.message ||
        data.error ||
        "Request failed."
    );
}


function adminEscape(value) {

    if (
        value === null ||
        value === undefined
    ) {
        return "-";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}