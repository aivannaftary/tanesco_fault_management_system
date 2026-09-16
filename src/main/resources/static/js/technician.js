/* =========================================================
   TANESCO FAULT MANAGEMENT SYSTEM
   TECHNICIAN FRONTEND
========================================================= */

let technicianFaults = [];

let currentTechnician = null;

let currentTechnicianFault = null;


/* =========================================================
   INITIALIZATION
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        currentTechnician =
            requireTechnician();


        if (!currentTechnician) {
            return;
        }


        displayTechnician(
            currentTechnician
        );


        const page =
            document.body.dataset.page;


        try {


            if (
                page ===
                "technician-dashboard"
            ) {

                await loadTechnicianDashboard();
            }


            if (
                page ===
                "assigned-faults"
            ) {

                await loadAssignedFaultsPage();
            }


            if (
                page ===
                "technician-fault-details"
            ) {

                await loadTechnicianFaultDetailsPage();
            }


        } catch (error) {

            console.error(
                "Technician page error:",
                error
            );


            showTechnicianMessage(
                error.message ||
                "Unable to load the requested information."
            );
        }
    }
);


/* =========================================================
   SECURITY
========================================================= */

function requireTechnician() {

    const token =
        getToken();


    const user =
        getUser();


    if (
        !token ||
        !user
    ) {

        window.location.href =
            "/";

        return null;
    }


    const role =
        String(
            user.role || ""
        )
            .replace(
                "ROLE_",
                ""
            )
            .toUpperCase();


    if (
        role !==
        "TECHNICIAN"
    ) {

        redirectTechnicianRole(
            role
        );

        return null;
    }


    return user;
}


function redirectTechnicianRole(
    role
) {

    switch (role) {


        case "CUSTOMER":

            window.location.href =
                "/customer/dashboard.html";

            break;


        case "OFFICER":

            window.location.href =
                "/officer/dashboard.html";

            break;


        case "ADMIN":

            window.location.href =
                "/admin/dashboard.html";

            break;


        default:

            logout();
    }
}


/* =========================================================
   DISPLAY TECHNICIAN
========================================================= */

function displayTechnician(user) {

    const fullName =
        user.fullName ||
        user.username ||
        "Technician";


    setTechnicianText(
        "userFullName",
        fullName
    );


    setTechnicianText(
        "userRole",
        "TECHNICIAN"
    );


    const avatar =
        document.getElementById(
            "userAvatar"
        );


    if (avatar) {

        avatar.textContent =
            technicianInitials(
                fullName
            );
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


function technicianInitials(
    name
) {

    const parts =
        String(name)
            .trim()
            .split(/\s+/)
            .filter(Boolean);


    if (
        parts.length === 0
    ) {

        return "T";
    }


    if (
        parts.length === 1
    ) {

        return parts[0]
            .charAt(0)
            .toUpperCase();
    }


    return (
        parts[0].charAt(0) +
        parts[
            parts.length - 1
        ].charAt(0)
    ).toUpperCase();
}


/* =========================================================
   MESSAGES
========================================================= */

function showTechnicianMessage(
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


    element.textContent =
        text;


    element.className =
        `message ${type}`;
}


function clearTechnicianMessage() {

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


/* =========================================================
   GET STAFF FAULTS
========================================================= */

async function fetchStaffFaults() {

    const response =
        await apiRequest(
            "/staff/faults"
        );


    const data =
        await readResponse(
            response
        );


    if (!response.ok) {

        throw new Error(
            technicianBackendMessage(
                data
            )
        );
    }


    return Array.isArray(data)
        ? data
        : [];
}


/* =========================================================
   FILTER FAULTS ASSIGNED TO LOGGED-IN TECHNICIAN
========================================================= */

function onlyMyTechnicianFaults(
    faults
) {

    if (
        !currentTechnician
    ) {

        return [];
    }


    const username =
        String(
            currentTechnician.username ||
            ""
        )
            .trim()
            .toLowerCase();


    return faults.filter(
        fault => {

            const assignedUsername =
                String(
                    fault.technicianUsername ||
                    ""
                )
                    .trim()
                    .toLowerCase();


            return (
                assignedUsername &&
                assignedUsername ===
                username
            );
        }
    );
}


/* =========================================================
   DASHBOARD
========================================================= */

async function loadTechnicianDashboard() {

    const allFaults =
        await fetchStaffFaults();


    technicianFaults =
        onlyMyTechnicianFaults(
            allFaults
        );


    const total =
        technicianFaults.length;


    const assigned =
        technicianFaults.filter(
            fault =>
                fault.status ===
                "ASSIGNED"
        ).length;


    const inProgress =
        technicianFaults.filter(
            fault =>
                fault.status ===
                "IN_PROGRESS"
        ).length;


    const resolved =
        technicianFaults.filter(
            fault =>
                fault.status ===
                    "RESOLVED" ||
                fault.status ===
                    "CLOSED"
        ).length;


    setTechnicianText(
        "totalAssigned",
        total
    );


    setTechnicianText(
        "assignedCount",
        assigned
    );


    setTechnicianText(
        "inProgressCount",
        inProgress
    );


    setTechnicianText(
        "resolvedCount",
        resolved
    );


    const recent =
        [...technicianFaults]
            .sort(
                (a, b) =>
                    new Date(
                        b.assignedAt ||
                        b.reportedAt
                    ) -
                    new Date(
                        a.assignedAt ||
                        a.reportedAt
                    )
            )
            .slice(
                0,
                5
            );


    renderTechnicianFaults(
        recent,
        "recentTechnicianFaults",
        true
    );
}


/* =========================================================
   ASSIGNED FAULTS PAGE
========================================================= */

async function loadAssignedFaultsPage() {

    const allFaults =
        await fetchStaffFaults();


    technicianFaults =
        onlyMyTechnicianFaults(
            allFaults
        );


    renderTechnicianFaults(
        technicianFaults,
        "technicianFaultsBody",
        false
    );


    const search =
        document.getElementById(
            "technicianFaultSearch"
        );


    const status =
        document.getElementById(
            "technicianStatusFilter"
        );


    if (search) {

        search.addEventListener(
            "input",
            filterTechnicianFaults
        );
    }


    if (status) {

        status.addEventListener(
            "change",
            filterTechnicianFaults
        );
    }
}


function filterTechnicianFaults() {

    const search =
        String(
            document.getElementById(
                "technicianFaultSearch"
            )?.value || ""
        )
            .trim()
            .toLowerCase();


    const status =
        document.getElementById(
            "technicianStatusFilter"
        )?.value || "";


    const filtered =
        technicianFaults.filter(
            fault => {

                const searchable =
                    [
                        fault.referenceNumber,
                        fault.customerFullName,
                        fault.customerUsername,
                        fault.category,
                        fault.area,
                        fault.location
                    ]
                        .filter(Boolean)
                        .join(" ")
                        .toLowerCase();


                const searchMatches =
                    !search ||
                    searchable.includes(
                        search
                    );


                const statusMatches =
                    !status ||
                    fault.status ===
                        status;


                return (
                    searchMatches &&
                    statusMatches
                );
            }
        );


    renderTechnicianFaults(
        filtered,
        "technicianFaultsBody",
        false
    );
}


/* =========================================================
   RENDER FAULT TABLE
========================================================= */

function renderTechnicianFaults(
    faults,
    bodyId,
    dashboard
) {

    const body =
        document.getElementById(
            bodyId
        );


    if (!body) {
        return;
    }


    if (
        !faults ||
        faults.length === 0
    ) {

        body.innerHTML = `

            <tr>

                <td
                    colspan="7"
                    class="empty-cell"
                >
                    No assigned faults found.
                </td>

            </tr>

        `;

        return;
    }


    body.innerHTML =
        faults.map(
            fault => {


                const detailsUrl =
                    `/technician/fault-details.html?reference=${encodeURIComponent(
                        fault.referenceNumber
                    )}`;


                if (dashboard) {

                    return `

                        <tr>

                            <td>

                                <strong>
                                    ${technicianEscape(
                                        fault.referenceNumber
                                    )}
                                </strong>

                            </td>


                            <td>
                                ${technicianEscape(
                                    fault.category
                                )}
                            </td>


                            <td>
                                ${technicianEscape(
                                    fault.area
                                )}
                            </td>


                            <td>

                                <span class="
                                    priority-badge
                                    ${technicianPriorityClass(
                                        fault.priority
                                    )}
                                ">

                                    ${technicianFormatValue(
                                        fault.priority
                                    )}

                                </span>

                            </td>


                            <td>

                                <span class="
                                    status-badge
                                    ${technicianStatusClass(
                                        fault.status
                                    )}
                                ">

                                    ${technicianFormatValue(
                                        fault.status
                                    )}

                                </span>

                            </td>


                            <td>

                                ${technicianFormatDate(
                                    fault.assignedAt ||
                                    fault.reportedAt
                                )}

                            </td>


                            <td>

                                <a
                                    class="view-button"
                                    href="${detailsUrl}"
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
                                ${technicianEscape(
                                    fault.referenceNumber
                                )}
                            </strong>

                        </td>


                        <td>

                            ${technicianEscape(
                                fault.customerFullName ||
                                fault.customerUsername
                            )}

                        </td>


                        <td>

                            ${technicianEscape(
                                fault.category
                            )}

                        </td>


                        <td>

                            ${technicianEscape(
                                fault.area
                            )}

                        </td>


                        <td>

                            <span class="
                                priority-badge
                                ${technicianPriorityClass(
                                    fault.priority
                                )}
                            ">

                                ${technicianFormatValue(
                                    fault.priority
                                )}

                            </span>

                        </td>


                        <td>

                            <span class="
                                status-badge
                                ${technicianStatusClass(
                                    fault.status
                                )}
                            ">

                                ${technicianFormatValue(
                                    fault.status
                                )}

                            </span>

                        </td>


                        <td>

                            <a
                                class="view-button"
                                href="${detailsUrl}"
                            >
                                View
                            </a>

                        </td>

                    </tr>

                `;

            }
        ).join("");
}


/* =========================================================
   TECHNICIAN FAULT DETAILS
========================================================= */

async function loadTechnicianFaultDetailsPage() {

    const reference =
        technicianReference();


    if (!reference) {

        showTechnicianMessage(
            "No fault reference was provided."
        );

        return;
    }


    const allFaults =
        await fetchStaffFaults();


    technicianFaults =
        onlyMyTechnicianFaults(
            allFaults
        );


    currentTechnicianFault =
        technicianFaults.find(
            fault =>
                fault.referenceNumber ===
                reference
        );


    if (
        !currentTechnicianFault
    ) {

        showTechnicianMessage(
            "This fault is not assigned to your technician account."
        );

        hideTechnicianActions();

        return;
    }


    displayTechnicianFaultDetails(
        currentTechnicianFault
    );


    try {

        await loadTechnicianAttachments(
            reference
        );

    } catch (error) {

        console.error(
            "Attachment loading error:",
            error
        );


        const list =
            document.getElementById(
                "technicianAttachmentsList"
            );


        if (list) {

            list.innerHTML = `

                <p class="muted-text">
                    Attachments could not be loaded.
                </p>

            `;
        }
    }


    try {

        await loadTechnicianHistory(
            reference
        );

    } catch (error) {

        console.error(
            "History loading error:",
            error
        );


        const list =
            document.getElementById(
                "technicianHistoryList"
            );


        if (list) {

            list.innerHTML = `

                <p class="muted-text">
                    Fault history could not be loaded.
                </p>

            `;
        }
    }
}


/* =========================================================
   DISPLAY DETAILS
========================================================= */

function displayTechnicianFaultDetails(
    fault
) {

    setTechnicianText(
        "detailReference",
        fault.referenceNumber
    );


    setTechnicianText(
        "detailCustomer",
        fault.customerFullName ||
        "-"
    );


    setTechnicianText(
        "detailCustomerUsername",
        fault.customerUsername ||
        "-"
    );


    setTechnicianText(
        "detailCategory",
        fault.category
    );


    setTechnicianText(
        "detailPriority",
        technicianFormatValue(
            fault.priority
        )
    );


    setTechnicianText(
        "detailArea",
        fault.area
    );


    setTechnicianText(
        "detailLocation",
        fault.location
    );


    setTechnicianText(
        "detailDescription",
        fault.description
    );


    setTechnicianText(
        "detailReported",
        technicianFormatDateTime(
            fault.reportedAt
        )
    );


    setTechnicianText(
        "detailAssigned",
        technicianFormatDateTime(
            fault.assignedAt
        )
    );


    setTechnicianText(
        "detailStarted",
        technicianFormatDateTime(
            fault.startedAt
        )
    );


    setTechnicianText(
        "detailResolved",
        technicianFormatDateTime(
            fault.resolvedAt
        )
    );


    const status =
        document.getElementById(
            "detailStatus"
        );


    if (status) {

        status.textContent =
            technicianFormatValue(
                fault.status
            );


        status.className =
            `status-badge ${technicianStatusClass(
                fault.status
            )}`;
    }


    configureTechnicianActions(
        fault.status
    );
}


/* =========================================================
   SHOW/HIDE ACTIONS
========================================================= */

function configureTechnicianActions(
    status
) {

    const statusSection =
        document.getElementById(
            "statusUpdateSection"
        );


    const resolveSection =
        document.getElementById(
            "resolveSection"
        );


    if (statusSection) {

        statusSection.style.display =
            (
                status === "ASSIGNED"
            )
                ? "block"
                : "none";
    }


    if (resolveSection) {

        resolveSection.style.display =
            (
                status === "IN_PROGRESS"
            )
                ? "block"
                : "none";
    }
}


function hideTechnicianActions() {

    const statusSection =
        document.getElementById(
            "statusUpdateSection"
        );


    const resolveSection =
        document.getElementById(
            "resolveSection"
        );


    if (statusSection) {

        statusSection.style.display =
            "none";
    }


    if (resolveSection) {

        resolveSection.style.display =
            "none";
    }
}


/* =========================================================
   UPDATE STATUS
========================================================= */

const technicianStatusForm =
    document.getElementById(
        "statusUpdateForm"
    );


if (technicianStatusForm) {

    technicianStatusForm.addEventListener(
        "submit",
        async event => {

            event.preventDefault();


            clearTechnicianMessage();


            const reference =
                technicianReference();


            const status =
                document.getElementById(
                    "newStatus"
                ).value;


            const message =
                document.getElementById(
                    "statusMessage"
                )
                    .value
                    .trim();


            if (
                !status ||
                !message
            ) {

                showTechnicianMessage(
                    "Status and progress message are required."
                );

                return;
            }


            const button =
                document.getElementById(
                    "updateStatusButton"
                );


            button.disabled =
                true;


            button.textContent =
                "Updating...";


            try {


                const response =
                    await apiRequest(
                        `/staff/faults/${encodeURIComponent(
                            reference
                        )}/status`,
                        {

                            method:
                                "PATCH",

                            body:
                                JSON.stringify({
                                    status:
                                        status,

                                    message:
                                        message
                                })
                        }
                    );


                const data =
                    await readResponse(
                        response
                    );


                if (!response.ok) {

                    showTechnicianMessage(
                        technicianBackendMessage(
                            data
                        )
                    );

                    return;
                }


                showTechnicianMessage(
                    "Fault status updated successfully.",
                    "success"
                );


                currentTechnicianFault =
                    data;


                displayTechnicianFaultDetails(
                    data
                );


                technicianStatusForm.reset();


                await loadTechnicianHistory(
                    reference
                );


            } catch (error) {


                console.error(
                    "Status update error:",
                    error
                );


                showTechnicianMessage(
                    "Unable to update the fault status."
                );


            } finally {


                button.disabled =
                    false;


                button.textContent =
                    "Update Status";
            }
        }
    );
}


/* =========================================================
   RESOLVE FAULT
========================================================= */

const resolveFaultForm =
    document.getElementById(
        "resolveFaultForm"
    );


if (resolveFaultForm) {

    resolveFaultForm.addEventListener(
        "submit",
        async event => {

            event.preventDefault();


            clearTechnicianMessage();


            const reference =
                technicianReference();


            const actualCause =
                document.getElementById(
                    "actualCause"
                )
                    .value
                    .trim();


            const resolutionNotes =
                document.getElementById(
                    "resolutionNotes"
                )
                    .value
                    .trim();


            if (
                !actualCause ||
                !resolutionNotes
            ) {

                showTechnicianMessage(
                    "Actual cause and resolution notes are required."
                );

                return;
            }


            const button =
                document.getElementById(
                    "resolveButton"
                );


            button.disabled =
                true;


            button.textContent =
                "Resolving...";


            try {


                const response =
                    await apiRequest(
                        `/staff/faults/${encodeURIComponent(
                            reference
                        )}/resolve`,
                        {

                            method:
                                "PATCH",

                            body:
                                JSON.stringify({

                                    actualCause:
                                        actualCause,

                                    resolutionNotes:
                                        resolutionNotes

                                })
                        }
                    );


                const data =
                    await readResponse(
                        response
                    );


                if (!response.ok) {

                    showTechnicianMessage(
                        technicianBackendMessage(
                            data
                        )
                    );

                    return;
                }


                showTechnicianMessage(
                    "Fault marked as resolved successfully.",
                    "success"
                );


                currentTechnicianFault =
                    data;


                displayTechnicianFaultDetails(
                    data
                );


                resolveFaultForm.reset();


                await loadTechnicianHistory(
                    reference
                );


            } catch (error) {


                console.error(
                    "Resolve fault error:",
                    error
                );


                showTechnicianMessage(
                    "Unable to resolve the fault."
                );


            } finally {


                button.disabled =
                    false;


                button.textContent =
                    "Mark as Resolved";
            }
        }
    );
}


/* =========================================================
   ATTACHMENTS
========================================================= */

async function loadTechnicianAttachments(
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
            technicianBackendMessage(
                data
            )
        );
    }


    const list =
        document.getElementById(
            "technicianAttachmentsList"
        );


    if (!list) {
        return;
    }


    if (
        !Array.isArray(data) ||
        data.length === 0
    ) {

        list.innerHTML = `

            <p class="muted-text">
                No evidence uploaded yet.
            </p>

        `;

        return;
    }


    list.innerHTML =
        data.map(
            attachment => `

                <div class="attachment-item">

                    <strong>

                        ${technicianEscape(
                            attachment.originalFileName
                        )}

                    </strong>


                    <span>

                        ${technicianEscape(
                            attachment.contentType
                        )}

                        ·

                        ${technicianFileSize(
                            attachment.fileSize
                        )}

                    </span>


                    <span>

                        Uploaded by

                        ${technicianEscape(
                            attachment.uploadedByUsername ||
                            attachment.uploadedBy ||
                            "User"
                        )}

                    </span>


                    <span>

                        ${technicianFormatDateTime(
                            attachment.uploadedAt
                        )}

                    </span>

                </div>

            `
        ).join("");
}


/* =========================================================
   UPLOAD TECHNICIAN EVIDENCE
========================================================= */

const technicianAttachmentForm =
    document.getElementById(
        "technicianAttachmentForm"
    );


if (technicianAttachmentForm) {

    technicianAttachmentForm.addEventListener(
        "submit",
        async event => {

            event.preventDefault();


            clearTechnicianMessage();


            const reference =
                technicianReference();


            const input =
                document.getElementById(
                    "technicianAttachmentFile"
                );


            if (
                !input ||
                !input.files.length
            ) {

                showTechnicianMessage(
                    "Select a file first."
                );

                return;
            }


            const file =
                input.files[0];


            const maximumSize =
                10 * 1024 * 1024;


            const allowedTypes = [

                "image/jpeg",

                "image/png",

                "image/webp",

                "application/pdf"

            ];


            if (
                file.size >
                maximumSize
            ) {

                showTechnicianMessage(
                    "File must not exceed 10 MB."
                );

                return;
            }


            if (
                !allowedTypes.includes(
                    file.type
                )
            ) {

                showTechnicianMessage(
                    "Only JPEG, PNG, WEBP and PDF files are allowed."
                );

                return;
            }


            const button =
                document.getElementById(
                    "technicianUploadButton"
                );


            button.disabled =
                true;


            button.textContent =
                "Uploading...";


            try {


                const formData =
                    new FormData();


                formData.append(
                    "file",
                    file
                );


                const response =
                    await apiRequest(
                        `/faults/${encodeURIComponent(
                            reference
                        )}/attachments`,
                        {

                            method:
                                "POST",

                            body:
                                formData

                        }
                    );


                const data =
                    await readResponse(
                        response
                    );


                if (!response.ok) {

                    showTechnicianMessage(
                        technicianBackendMessage(
                            data
                        )
                    );

                    return;
                }


                showTechnicianMessage(
                    "Evidence uploaded successfully.",
                    "success"
                );


                technicianAttachmentForm.reset();


                await loadTechnicianAttachments(
                    reference
                );


            } catch (error) {


                console.error(
                    "Technician upload error:",
                    error
                );


                showTechnicianMessage(
                    "Unable to upload evidence."
                );


            } finally {


                button.disabled =
                    false;


                button.textContent =
                    "Upload Evidence";
            }
        }
    );
}


/* =========================================================
   HISTORY
========================================================= */

async function loadTechnicianHistory(
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
            technicianBackendMessage(
                data
            )
        );
    }


    const list =
        document.getElementById(
            "technicianHistoryList"
        );


    if (!list) {
        return;
    }


    if (
        !Array.isArray(data) ||
        data.length === 0
    ) {

        list.innerHTML = `

            <p class="muted-text">
                No fault history is available.
            </p>

        `;

        return;
    }


    list.innerHTML =
        data.map(
            item => `

                <div class="timeline-item">


                    <strong>

                        ${technicianEscape(
                            technicianFormatValue(
                                item.status ||
                                item.newStatus
                            )
                        )}

                    </strong>


                    <p>

                        ${technicianEscape(
                            item.message ||
                            item.description ||
                            "Fault status updated."
                        )}

                    </p>


                    <small>

                        ${technicianFormatDateTime(
                            item.createdAt ||
                            item.updatedAt
                        )}

                    </small>


                </div>

            `
        ).join("");
}


/* =========================================================
   HELPERS
========================================================= */

function technicianReference() {

    return new URLSearchParams(
        window.location.search
    ).get(
        "reference"
    );
}


function setTechnicianText(
    id,
    value
) {

    const element =
        document.getElementById(
            id
        );


    if (element) {

        element.textContent =
            value ?? "-";
    }
}


function technicianStatusClass(
    status
) {

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


function technicianPriorityClass(
    priority
) {

    switch (priority) {


        case "HIGH":

            return "priority-high";


        case "MEDIUM":

            return "priority-medium";


        case "LOW":

            return "priority-low";


        default:

            return "";
    }
}


function technicianFormatValue(
    value
) {

    if (!value) {
        return "-";
    }


    return String(value)
        .replaceAll(
            "_",
            " "
        )
        .toLowerCase()
        .replace(
            /\b\w/g,
            character =>
                character.toUpperCase()
        );
}


function technicianFormatDate(
    value
) {

    if (!value) {
        return "-";
    }


    const date =
        new Date(value);


    if (
        Number.isNaN(
            date.getTime()
        )
    ) {

        return value;
    }


    return date.toLocaleDateString();
}


function technicianFormatDateTime(
    value
) {

    if (!value) {
        return "-";
    }


    const date =
        new Date(value);


    if (
        Number.isNaN(
            date.getTime()
        )
    ) {

        return value;
    }


    return date.toLocaleString();
}


function technicianFileSize(
    bytes
) {

    if (
        bytes === null ||
        bytes === undefined
    ) {

        return "-";
    }


    if (
        bytes < 1024
    ) {

        return `${bytes} B`;
    }


    if (
        bytes <
        1024 * 1024
    ) {

        return `${(
            bytes / 1024
        ).toFixed(1)} KB`;
    }


    return `${(
        bytes /
        (1024 * 1024)
    ).toFixed(1)} MB`;
}


function technicianBackendMessage(
    data
) {

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


function technicianEscape(
    value
) {

    if (
        value === null ||
        value === undefined
    ) {

        return "-";
    }


    return String(value)
        .replaceAll(
            "&",
            "&amp;"
        )
        .replaceAll(
            "<",
            "&lt;"
        )
        .replaceAll(
            ">",
            "&gt;"
        )
        .replaceAll(
            '"',
            "&quot;"
        )
        .replaceAll(
            "'",
            "&#039;"
        );
}