/* =========================================================
   TANESCO FAULT MANAGEMENT SYSTEM
   OFFICER PORTAL
========================================================= */

let officerUser = null;
let officerFaults = [];
let currentOfficerFault = null;


/* =========================================================
   INITIALIZATION
========================================================= */

document.addEventListener("DOMContentLoaded", async () => {

    officerUser = requireOfficer();

    if (!officerUser) {
        return;
    }

    displayOfficer(officerUser);

    const page = document.body.dataset.page;

    try {

        if (page === "officer-dashboard") {
            await loadOfficerDashboard();
        }

        if (page === "officer-faults") {
            await loadOfficerFaultsPage();
        }

        if (page === "officer-fault-details") {
            await loadOfficerFaultDetails();
        }

    } catch (error) {

        console.error("Officer portal error:", error);

        showOfficerMessage(
            error.message ||
            "Unable to load the requested information."
        );
    }
});


/* =========================================================
   SECURITY
========================================================= */

function requireOfficer() {

    const token = getToken();
    const user = getUser();

    if (!token || !user) {

        window.location.href = "/";
        return null;
    }

    const role = String(user.role || "")
        .replace("ROLE_", "")
        .toUpperCase();

    if (role !== "OFFICER") {

        switch (role) {

            case "CUSTOMER":
                window.location.href =
                    "/customer/dashboard.html";
                break;

            case "TECHNICIAN":
                window.location.href =
                    "/technician/dashboard.html";
                break;

            case "ADMIN":
                window.location.href =
                    "/admin/dashboard.html";
                break;

            default:
                logout();
        }

        return null;
    }

    return user;
}


/* =========================================================
   USER DISPLAY
========================================================= */

function displayOfficer(user) {

    const fullName =
        user.fullName ||
        user.username ||
        "Officer";

    setOfficerText(
        "userFullName",
        fullName
    );

    setOfficerText(
        "userRole",
        "OFFICER"
    );

    const avatar =
        document.getElementById(
            "userAvatar"
        );

    if (avatar) {

        avatar.textContent =
            officerInitials(fullName);
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


function officerInitials(name) {

    const parts =
        String(name)
            .trim()
            .split(/\s+/)
            .filter(Boolean);

    if (parts.length === 0) {
        return "O";
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
   API
========================================================= */

async function fetchOfficerFaults() {

    const response =
        await apiRequest(
            "/staff/faults"
        );

    const data =
        await readResponse(response);

    if (!response.ok) {

        throw new Error(
            officerBackendMessage(data)
        );
    }

    return Array.isArray(data)
        ? data
        : [];
}


async function fetchTechnicians() {

    const response =
        await apiRequest(
            "/staff/technicians"
        );

    const data =
        await readResponse(response);

    if (!response.ok) {

        throw new Error(
            officerBackendMessage(data)
        );
    }

    return Array.isArray(data)
        ? data
        : [];
}


/* =========================================================
   DASHBOARD
========================================================= */

async function loadOfficerDashboard() {

    officerFaults =
        await fetchOfficerFaults();

    setOfficerText(
        "totalFaults",
        officerFaults.length
    );

    setOfficerText(
        "submittedFaults",
        countOfficerStatus("SUBMITTED")
    );

    setOfficerText(
        "assignedFaults",
        countOfficerStatus("ASSIGNED")
    );

    setOfficerText(
        "inProgressFaults",
        countOfficerStatus("IN_PROGRESS")
    );

    setOfficerText(
        "resolvedFaults",
        countOfficerStatus("RESOLVED")
    );

    setOfficerText(
        "closedFaults",
        countOfficerStatus("CLOSED")
    );


    const recent =
        [...officerFaults]
            .sort(
                (a, b) =>
                    new Date(b.reportedAt) -
                    new Date(a.reportedAt)
            )
            .slice(0, 5);

    renderOfficerFaults(
        recent,
        "officerRecentFaults",
        true
    );
}


function countOfficerStatus(status) {

    return officerFaults.filter(
        fault =>
            fault.status === status
    ).length;
}


/* =========================================================
   FAULT MANAGEMENT PAGE
========================================================= */

async function loadOfficerFaultsPage() {

    officerFaults =
        await fetchOfficerFaults();

    renderOfficerFaults(
        officerFaults,
        "officerFaultsBody",
        false
    );

    document
        .getElementById("faultSearch")
        ?.addEventListener(
            "input",
            filterOfficerFaults
        );

    document
        .getElementById("statusFilter")
        ?.addEventListener(
            "change",
            filterOfficerFaults
        );

    document
        .getElementById("priorityFilter")
        ?.addEventListener(
            "change",
            filterOfficerFaults
        );
}


function filterOfficerFaults() {

    const search =
        String(
            document.getElementById(
                "faultSearch"
            )?.value || ""
        )
            .trim()
            .toLowerCase();

    const status =
        document.getElementById(
            "statusFilter"
        )?.value || "";

    const priority =
        document.getElementById(
            "priorityFilter"
        )?.value || "";


    const filtered =
        officerFaults.filter(fault => {

            const searchable =
                [
                    fault.referenceNumber,
                    fault.customerFullName,
                    fault.customerUsername,
                    fault.category,
                    fault.area,
                    fault.location,
                    fault.technicianFullName,
                    fault.technicianUsername
                ]
                    .filter(Boolean)
                    .join(" ")
                    .toLowerCase();

            const matchesSearch =
                !search ||
                searchable.includes(search);

            const matchesStatus =
                !status ||
                fault.status === status;

            const matchesPriority =
                !priority ||
                fault.priority === priority;

            return (
                matchesSearch &&
                matchesStatus &&
                matchesPriority
            );
        });


    renderOfficerFaults(
        filtered,
        "officerFaultsBody",
        false
    );
}


/* =========================================================
   RENDER FAULTS
========================================================= */

function renderOfficerFaults(
    faults,
    bodyId,
    dashboard
) {

    const body =
        document.getElementById(bodyId);

    if (!body) {
        return;
    }

    if (!faults || faults.length === 0) {

        body.innerHTML = `

            <tr>
                <td
                    colspan="${dashboard ? 7 : 8}"
                    class="empty-cell"
                >
                    No faults found.
                </td>
            </tr>

        `;

        return;
    }


    body.innerHTML =
        faults.map(fault => {

            const url =
                `/officer/fault-details.html?reference=${encodeURIComponent(
                    fault.referenceNumber
                )}`;

            const technician =
                fault.technicianFullName ||
                fault.technicianUsername ||
                "Not Assigned";


            if (dashboard) {

                return `

                    <tr>

                        <td>
                            <strong>
                                ${officerEscape(
                                    fault.referenceNumber
                                )}
                            </strong>
                        </td>

                        <td>
                            ${officerEscape(
                                fault.category
                            )}
                        </td>

                        <td>
                            ${officerEscape(
                                fault.area
                            )}
                        </td>

                        <td>
                            ${officerFormatValue(
                                fault.priority
                            )}
                        </td>

                        <td>
                            <span class="
                                status-badge
                                ${officerStatusClass(
                                    fault.status
                                )}
                            ">
                                ${officerFormatValue(
                                    fault.status
                                )}
                            </span>
                        </td>

                        <td>
                            ${officerEscape(
                                technician
                            )}
                        </td>

                        <td>
                            <a
                                href="${url}"
                                class="view-button"
                            >
                                View
                            </a>
                        </td>

                    </tr>

                `;
            }


            return `

                <tr>

                    <td>
                        <strong>
                            ${officerEscape(
                                fault.referenceNumber
                            )}
                        </strong>
                    </td>

                    <td>
                        ${officerEscape(
                            fault.customerFullName ||
                            fault.customerUsername
                        )}
                    </td>

                    <td>
                        ${officerEscape(
                            fault.category
                        )}
                    </td>

                    <td>
                        ${officerEscape(
                            fault.area
                        )}
                    </td>

                    <td>
                        ${officerFormatValue(
                            fault.priority
                        )}
                    </td>

                    <td>

                        <span class="
                            status-badge
                            ${officerStatusClass(
                                fault.status
                            )}
                        ">

                            ${officerFormatValue(
                                fault.status
                            )}

                        </span>

                    </td>

                    <td>
                        ${officerEscape(
                            technician
                        )}
                    </td>

                    <td>

                        <a
                            href="${url}"
                            class="view-button"
                        >
                            View
                        </a>

                    </td>

                </tr>

            `;

        }).join("");
}


/* =========================================================
   DETAILS PAGE
========================================================= */

async function loadOfficerFaultDetails() {

    const reference =
        getOfficerReference();

    if (!reference) {

        showOfficerMessage(
            "No fault reference was provided."
        );

        return;
    }


    officerFaults =
        await fetchOfficerFaults();


    currentOfficerFault =
        officerFaults.find(
            fault =>
                fault.referenceNumber ===
                reference
        );


    if (!currentOfficerFault) {

        showOfficerMessage(
            "Fault was not found."
        );

        return;
    }


    displayOfficerFault(
        currentOfficerFault
    );


    await loadTechnicianOptions();


    try {

        await loadOfficerHistory(
            reference
        );

    } catch (error) {

        console.error(
            "History error:",
            error
        );
    }


    try {

        await loadOfficerAttachments(
            reference
        );

    } catch (error) {

        console.error(
            "Attachment error:",
            error
        );
    }
}


/* =========================================================
   DISPLAY DETAILS
========================================================= */

function displayOfficerFault(fault) {

    setOfficerText(
        "detailReference",
        fault.referenceNumber
    );

    setOfficerText(
        "detailCustomer",
        fault.customerFullName ||
        "-"
    );

    setOfficerText(
        "detailCustomerUsername",
        fault.customerUsername ||
        "-"
    );

    setOfficerText(
        "detailCategory",
        fault.category
    );

    setOfficerText(
        "detailPriority",
        officerFormatValue(
            fault.priority
        )
    );

    setOfficerText(
        "detailArea",
        fault.area
    );

    setOfficerText(
        "detailLocation",
        fault.location
    );

    setOfficerText(
        "detailTechnician",
        fault.technicianFullName ||
        fault.technicianUsername ||
        "Not Assigned"
    );

    setOfficerText(
        "detailReported",
        officerFormatDateTime(
            fault.reportedAt
        )
    );

    setOfficerText(
        "detailDescription",
        fault.description
    );

    setOfficerText(
        "detailCause",
        fault.actualCause ||
        "-"
    );

    setOfficerText(
        "detailResolution",
        fault.resolutionNotes ||
        "-"
    );


    const status =
        document.getElementById(
            "detailStatus"
        );

    if (status) {

        status.textContent =
            officerFormatValue(
                fault.status
            );

        status.className =
            `status-badge ${officerStatusClass(
                fault.status
            )}`;
    }


    configureAssignmentSection(
        fault
    );
}


/* =========================================================
   ASSIGNMENT SECTION
========================================================= */

function configureAssignmentSection(fault) {

    const section =
        document.getElementById(
            "assignmentSection"
        );

    if (!section) {
        return;
    }

    /*
     * Assignment is primarily intended
     * for newly submitted faults.
     */

    section.style.display =
        fault.status === "SUBMITTED"
            ? "block"
            : "none";
}


/* =========================================================
   TECHNICIAN OPTIONS
========================================================= */

async function loadTechnicianOptions() {

    const select =
        document.getElementById(
            "technicianSelect"
        );

    if (!select) {
        return;
    }


    const technicians =
        await fetchTechnicians();


    if (technicians.length === 0) {

        select.innerHTML = `

            <option value="">
                No technicians available
            </option>

        `;

        return;
    }


    select.innerHTML = `

        <option value="">
            Select technician
        </option>

        ${technicians.map(
            technician => `

                <option
                    value="${technician.id}"
                >

                    ${officerEscape(
                        technician.fullName
                    )}

                    (${officerEscape(
                        technician.username
                    )})

                </option>

            `
        ).join("")}

    `;
}


/* =========================================================
   ASSIGN TECHNICIAN
========================================================= */

const assignmentForm =
    document.getElementById(
        "assignmentForm"
    );


if (assignmentForm) {

    assignmentForm.addEventListener(
        "submit",
        async event => {

            event.preventDefault();

            clearOfficerMessage();


            const reference =
                getOfficerReference();


            const technicianId =
                document.getElementById(
                    "technicianSelect"
                ).value;


            if (!technicianId) {

                showOfficerMessage(
                    "Select a technician."
                );

                return;
            }


            const button =
                document.getElementById(
                    "assignButton"
                );


            button.disabled = true;

            button.textContent =
                "Assigning...";


            try {

                const response =
                    await apiRequest(
                        `/staff/faults/${encodeURIComponent(
                            reference
                        )}/assign`,
                        {
                            method: "PATCH",

                            body: JSON.stringify({
                                technicianId:
                                    Number(
                                        technicianId
                                    )
                            })
                        }
                    );


                const data =
                    await readResponse(
                        response
                    );


                if (!response.ok) {

                    showOfficerMessage(
                        officerBackendMessage(
                            data
                        )
                    );

                    return;
                }


                currentOfficerFault =
                    data;


                displayOfficerFault(
                    data
                );


                showOfficerMessage(
                    "Technician assigned successfully.",
                    "success"
                );


                await loadOfficerHistory(
                    reference
                );


            } catch (error) {

                console.error(
                    "Assignment error:",
                    error
                );

                showOfficerMessage(
                    "Unable to assign technician."
                );

            } finally {

                button.disabled = false;

                button.textContent =
                    "Assign Technician";
            }
        }
    );
}


/* =========================================================
   HISTORY
========================================================= */

async function loadOfficerHistory(
    reference
) {

    const response =
        await apiRequest(
            `/faults/${encodeURIComponent(
                reference
            )}/history`
        );


    const data =
        await readResponse(
            response
        );


    if (!response.ok) {

        throw new Error(
            officerBackendMessage(data)
        );
    }


    const list =
        document.getElementById(
            "officerHistoryList"
        );


    if (!list) {
        return;
    }


    if (!Array.isArray(data) ||
        data.length === 0) {

        list.innerHTML = `

            <p class="muted-text">
                No history available.
            </p>

        `;

        return;
    }


    list.innerHTML =
        data.map(item => `

            <div class="timeline-item">

                <strong>

                    ${officerEscape(
                        officerFormatValue(
                            item.status ||
                            item.newStatus
                        )
                    )}

                </strong>

                <p>

                    ${officerEscape(
                        item.message ||
                        item.description ||
                        "Fault updated."
                    )}

                </p>

                <small>

                    ${officerFormatDateTime(
                        item.createdAt ||
                        item.updatedAt
                    )}

                </small>

            </div>

        `).join("");
}


/* =========================================================
   ATTACHMENTS
========================================================= */

async function loadOfficerAttachments(
    reference
) {

    const response =
        await apiRequest(
            `/faults/${encodeURIComponent(
                reference
            )}/attachments`
        );


    const data =
        await readResponse(
            response
        );


    if (!response.ok) {

        throw new Error(
            officerBackendMessage(data)
        );
    }


    const list =
        document.getElementById(
            "officerAttachmentsList"
        );


    if (!list) {
        return;
    }


    if (!Array.isArray(data) ||
        data.length === 0) {

        list.innerHTML = `

            <p class="muted-text">
                No evidence uploaded.
            </p>

        `;

        return;
    }


    list.innerHTML =
        data.map(
            attachment => `

                <div class="attachment-item">

                    <strong>

                        ${officerEscape(
                            attachment.originalFileName
                        )}

                    </strong>

                    <span>

                        ${officerEscape(
                            attachment.contentType
                        )}

                    </span>

                    <span>

                        ${officerFileSize(
                            attachment.fileSize
                        )}

                    </span>

                    <span>

                        Uploaded by

                        ${officerEscape(
                            attachment.uploadedByUsername ||
                            attachment.uploadedBy ||
                            "User"
                        )}

                    </span>

                    <span>

                        ${officerFormatDateTime(
                            attachment.uploadedAt
                        )}

                    </span>

                </div>

            `
        ).join("");
}


/* =========================================================
   HELPERS
========================================================= */

function getOfficerReference() {

    return new URLSearchParams(
        window.location.search
    ).get("reference");
}


function setOfficerText(
    id,
    value
) {

    const element =
        document.getElementById(id);

    if (element) {

        element.textContent =
            value ?? "-";
    }
}


function showOfficerMessage(
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


function clearOfficerMessage() {

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


function officerStatusClass(status) {

    switch (status) {

        case "SUBMITTED":
            return "status-submitted";

        case "ASSIGNED":
            return "status-assigned";

        case "IN_PROGRESS":
            return "status-in-progress";

        case "RESOLVED":
            return "status-resolved";

        case "CLOSED":
            return "status-closed";

        default:
            return "status-submitted";
    }
}


function officerFormatValue(value) {

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


function officerFormatDateTime(value) {

    if (!value) {
        return "-";
    }

    const date =
        new Date(value);

    if (Number.isNaN(
        date.getTime()
    )) {
        return value;
    }

    return date.toLocaleString();
}


function officerFileSize(bytes) {

    if (
        bytes === null ||
        bytes === undefined
    ) {
        return "-";
    }

    if (bytes < 1024) {
        return `${bytes} B`;
    }

    if (bytes < 1024 * 1024) {

        return `${(
            bytes / 1024
        ).toFixed(1)} KB`;
    }

    return `${(
        bytes /
        (1024 * 1024)
    ).toFixed(1)} MB`;
}


function officerBackendMessage(data) {

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


function officerEscape(value) {

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