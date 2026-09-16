/* =========================================================
   TANESCO FAULT MANAGEMENT SYSTEM
   CUSTOMER FRONTEND
========================================================= */

let customerFaults = [];


/* =========================================================
   PAGE INITIALIZATION
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        const user = requireCustomer();

        if (!user) {
            return;
        }

        displayUser(user);

        const page =
            document.body.dataset.page;

        try {

            if (page === "customer-dashboard") {
                await loadDashboard();
            }

            if (page === "my-faults") {
                await loadMyFaults();
            }

            if (page === "fault-details") {
                await loadFaultDetailsPage();
            }

        } catch (error) {

            console.error(
                "Customer page error:",
                error
            );

            showCustomerMessage(
                error.message ||
                "Unable to load the requested information."
            );
        }
    }
);


/* =========================================================
   CUSTOMER SECURITY
========================================================= */

function requireCustomer() {

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

    if (role !== "CUSTOMER") {

        redirectUserByRole(role);

        return null;
    }

    return user;
}


function redirectUserByRole(role) {

    switch (role) {

        case "TECHNICIAN":

            window.location.href =
                "/technician/dashboard.html";

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
   USER DISPLAY
========================================================= */

function displayUser(user) {

    const fullName =
        user.fullName ||
        user.username ||
        "Customer";


    const nameElement =
        document.getElementById(
            "userFullName"
        );


    const roleElement =
        document.getElementById(
            "userRole"
        );


    const avatarElement =
        document.getElementById(
            "userAvatar"
        );


    const welcomeElement =
        document.getElementById(
            "welcomeMessage"
        );


    if (nameElement) {

        nameElement.textContent =
            fullName;
    }


    if (roleElement) {

        roleElement.textContent =
            "CUSTOMER";
    }


    if (avatarElement) {

        avatarElement.textContent =
            getInitials(fullName);
    }


    if (welcomeElement) {

        welcomeElement.textContent =
            `Welcome back, ${fullName}`;
    }
}


function getInitials(name) {

    const parts =
        name.trim()
            .split(/\s+/)
            .filter(Boolean);


    if (parts.length === 0) {

        return "C";
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
   MESSAGES
========================================================= */

function showCustomerMessage(
    text,
    type = "error"
) {

    const message =
        document.getElementById(
            "message"
        );


    if (!message) {
        return;
    }


    message.textContent = text;

    message.className =
        `message ${type}`;
}


function clearCustomerMessage() {

    const message =
        document.getElementById(
            "message"
        );


    if (!message) {
        return;
    }


    message.textContent = "";

    message.className =
        "message";
}


/* =========================================================
   LOAD CUSTOMER FAULTS
========================================================= */

async function fetchMyFaults() {

    const response =
        await apiRequest(
            "/faults/my"
        );


    const data =
        await readResponse(
            response
        );


    if (!response.ok) {

        throw new Error(
            getBackendMessage(data)
        );
    }


    return Array.isArray(data)
        ? data
        : [];
}


/* =========================================================
   DASHBOARD
========================================================= */

async function loadDashboard() {

    customerFaults =
        await fetchMyFaults();


    const total =
        customerFaults.length;


    const active =
        customerFaults.filter(
            fault =>
                [
                    "SUBMITTED",
                    "ASSIGNED",
                    "IN_PROGRESS"
                ].includes(
                    fault.status
                )
        ).length;


    const resolved =
        customerFaults.filter(
            fault =>
                fault.status ===
                "RESOLVED"
        ).length;


    const closed =
        customerFaults.filter(
            fault =>
                fault.status ===
                "CLOSED"
        ).length;


    setText(
        "totalFaults",
        total
    );


    setText(
        "activeFaults",
        active
    );


    setText(
        "resolvedFaults",
        resolved
    );


    setText(
        "closedFaults",
        closed
    );


    const recent =
        [...customerFaults]
            .sort(
                (a, b) =>
                    new Date(
                        b.reportedAt
                    ) -
                    new Date(
                        a.reportedAt
                    )
            )
            .slice(
                0,
                5
            );


    renderFaultTable(
        recent,
        "recentFaultsBody"
    );
}


/* =========================================================
   MY FAULTS
========================================================= */

async function loadMyFaults() {

    customerFaults =
        await fetchMyFaults();


    renderFaultTable(
        customerFaults,
        "faultsTableBody"
    );


    const search =
        document.getElementById(
            "faultSearch"
        );


    const status =
        document.getElementById(
            "statusFilter"
        );


    if (search) {

        search.addEventListener(
            "input",
            filterFaults
        );
    }


    if (status) {

        status.addEventListener(
            "change",
            filterFaults
        );
    }
}


function filterFaults() {

    const searchValue =
        (
            document.getElementById(
                "faultSearch"
            )?.value || ""
        )
            .trim()
            .toLowerCase();


    const statusValue =
        document.getElementById(
            "statusFilter"
        )?.value || "";


    const filtered =
        customerFaults.filter(
            fault => {

                const searchable =
                    [
                        fault.referenceNumber,
                        fault.category,
                        fault.area,
                        fault.location
                    ]
                        .filter(Boolean)
                        .join(" ")
                        .toLowerCase();


                const matchesSearch =
                    !searchValue ||
                    searchable.includes(
                        searchValue
                    );


                const matchesStatus =
                    !statusValue ||
                    fault.status ===
                    statusValue;


                return (
                    matchesSearch &&
                    matchesStatus
                );
            }
        );


    renderFaultTable(
        filtered,
        "faultsTableBody"
    );
}


/* =========================================================
   RENDER FAULT TABLE
========================================================= */

function renderFaultTable(
    faults,
    bodyId
) {

    const body =
        document.getElementById(
            bodyId
        );


    if (!body) {
        return;
    }


    if (!faults.length) {

        body.innerHTML = `
            <tr>
                <td
                    colspan="7"
                    class="empty-cell"
                >
                    No faults found.
                </td>
            </tr>
        `;

        return;
    }


    body.innerHTML =
        faults.map(
            fault => `

                <tr>

                    <td>

                        <strong>
                            ${escapeHtml(
                                fault.referenceNumber
                            )}
                        </strong>

                    </td>


                    <td>
                        ${escapeHtml(
                            fault.category
                        )}
                    </td>


                    <td>
                        ${escapeHtml(
                            fault.area
                        )}
                    </td>


                    <td>

                        <span class="
                            priority-badge
                            ${priorityClass(
                                fault.priority
                            )}
                        ">

                            ${formatValue(
                                fault.priority
                            )}

                        </span>

                    </td>


                    <td>

                        <span class="
                            status-badge
                            ${statusClass(
                                fault.status
                            )}
                        ">

                            ${formatValue(
                                fault.status
                            )}

                        </span>

                    </td>


                    <td>

                        ${formatDate(
                            fault.reportedAt
                        )}

                    </td>


                    <td>

                        <a
                            class="view-button"
                            href="/customer/fault-details.html?reference=${encodeURIComponent(
                                fault.referenceNumber
                            )}"
                        >
                            View
                        </a>

                    </td>

                </tr>

            `
        ).join("");
}


/* =========================================================
   REPORT FAULT
========================================================= */

const faultForm =
    document.getElementById(
        "faultForm"
    );


if (faultForm) {

    faultForm.addEventListener(
        "submit",
        async event => {

            event.preventDefault();

            clearCustomerMessage();


            const button =
                document.getElementById(
                    "submitFaultButton"
                );


            button.disabled = true;

            button.textContent =
                "Submitting...";


            try {

                /*
                 * IMPORTANT:
                 * Priority is required by the backend.
                 */
                const requestBody = {

                    category:
                        document
                            .getElementById(
                                "category"
                            )
                            .value,


                    priority:
                        document
                            .getElementById(
                                "priority"
                            )
                            .value,


                    area:
                        document
                            .getElementById(
                                "area"
                            )
                            .value
                            .trim(),


                    location:
                        document
                            .getElementById(
                                "location"
                            )
                            .value
                            .trim(),


                    description:
                        document
                            .getElementById(
                                "description"
                            )
                            .value
                            .trim()
                };


                const response =
                    await apiRequest(
                        "/faults",
                        {
                            method: "POST",

                            body:
                                JSON.stringify(
                                    requestBody
                                )
                        }
                    );


                const data =
                    await readResponse(
                        response
                    );


                if (!response.ok) {

                    showCustomerMessage(
                        getBackendMessage(
                            data
                        )
                    );

                    return;
                }


                showCustomerMessage(
                    `Fault reported successfully. Reference: ${data.referenceNumber}`,
                    "success"
                );


                faultForm.reset();


                setTimeout(
                    () => {

                        window.location.href =
                            `/customer/fault-details.html?reference=${encodeURIComponent(
                                data.referenceNumber
                            )}`;

                    },
                    1200
                );


            } catch (error) {

                console.error(
                    "Report fault error:",
                    error
                );


                showCustomerMessage(
                    "Unable to submit the fault."
                );


            } finally {

                button.disabled = false;

                button.textContent =
                    "Submit Fault";
            }
        }
    );
}


/* =========================================================
   FAULT DETAILS PAGE
========================================================= */

async function loadFaultDetailsPage() {

    const reference =
        getReferenceFromUrl();


    if (!reference) {

        showCustomerMessage(
            "No fault reference was provided."
        );

        return;
    }


    /*
     * Load each section independently.
     *
     * This prevents one failed secondary request
     * from stopping the main fault information
     * from being displayed.
     */

    await loadFaultDetails(
        reference
    );


    try {

        await loadAttachments(
            reference
        );

    } catch (error) {

        console.error(
            "Attachment loading error:",
            error
        );

        const list =
            document.getElementById(
                "attachmentsList"
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

        await loadFaultHistory(
            reference
        );

    } catch (error) {

        console.error(
            "History loading error:",
            error
        );

        const list =
            document.getElementById(
                "historyList"
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
   LOAD FAULT DETAILS
========================================================= */

async function loadFaultDetails(
    reference
) {

    const response =
        await apiRequest(
            `/faults/${encodeURIComponent(
                reference
            )}`
        );


    const data =
        await readResponse(
            response
        );


    if (!response.ok) {

        throw new Error(
            getBackendMessage(data)
        );
    }


    setText(
        "detailReference",
        data.referenceNumber
    );


    setText(
        "detailCategory",
        data.category
    );


    setText(
        "detailPriority",
        formatValue(
            data.priority
        )
    );


    setText(
        "detailArea",
        data.area
    );


    setText(
        "detailLocation",
        data.location
    );


    setText(
        "detailDescription",
        data.description
    );


    setText(
        "detailTechnician",
        data.technicianFullName ||
        "Not assigned"
    );


    setText(
        "detailReported",
        formatDateTime(
            data.reportedAt
        )
    );


    const status =
        document.getElementById(
            "detailStatus"
        );


    if (status) {

        status.textContent =
            formatValue(
                data.status
            );


        status.className =
            `status-badge ${statusClass(
                data.status
            )}`;
    }


    const resolutionSection =
        document.getElementById(
            "resolutionSection"
        );


    if (resolutionSection) {

        if (
            data.status ===
            "RESOLVED"
        ) {

            resolutionSection.style.display =
                "block";

        } else {

            resolutionSection.style.display =
                "none";
        }
    }
}


/* =========================================================
   ATTACHMENTS
========================================================= */

async function loadAttachments(
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
            getBackendMessage(data)
        );
    }


    const list =
        document.getElementById(
            "attachmentsList"
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
                        ${escapeHtml(
                            attachment.originalFileName
                        )}
                    </strong>


                    <span>

                        ${escapeHtml(
                            attachment.contentType
                        )}

                        ·

                        ${formatFileSize(
                            attachment.fileSize
                        )}

                    </span>


                    <span>

                        Uploaded

                        ${formatDateTime(
                            attachment.uploadedAt
                        )}

                    </span>

                </div>

            `
        ).join("");
}


/* =========================================================
   UPLOAD ATTACHMENT
========================================================= */

const attachmentForm =
    document.getElementById(
        "attachmentForm"
    );


if (attachmentForm) {

    attachmentForm.addEventListener(
        "submit",
        async event => {

            event.preventDefault();

            clearCustomerMessage();


            const reference =
                getReferenceFromUrl();


            if (!reference) {

                showCustomerMessage(
                    "Fault reference is missing."
                );

                return;
            }


            const input =
                document.getElementById(
                    "attachmentFile"
                );


            if (
                !input ||
                !input.files.length
            ) {

                showCustomerMessage(
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

                showCustomerMessage(
                    "File must not exceed 10 MB."
                );

                return;
            }


            if (
                !allowedTypes.includes(
                    file.type
                )
            ) {

                showCustomerMessage(
                    "Only JPEG, PNG, WEBP and PDF files are allowed."
                );

                return;
            }


            const button =
                document.getElementById(
                    "uploadButton"
                );


            button.disabled = true;

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
                            method: "POST",

                            body:
                                formData
                        }
                    );


                const data =
                    await readResponse(
                        response
                    );


                if (!response.ok) {

                    showCustomerMessage(
                        getBackendMessage(
                            data
                        )
                    );

                    return;
                }


                showCustomerMessage(
                    "Evidence uploaded successfully.",
                    "success"
                );


                attachmentForm.reset();


                await loadAttachments(
                    reference
                );


            } catch (error) {

                console.error(
                    "Upload error:",
                    error
                );


                showCustomerMessage(
                    "Unable to upload evidence."
                );


            } finally {

                button.disabled = false;

                button.textContent =
                    "Upload Evidence";
            }
        }
    );
}


/* =========================================================
   FAULT HISTORY
========================================================= */

async function loadFaultHistory(
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
            getBackendMessage(data)
        );
    }


    const list =
        document.getElementById(
            "historyList"
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
                No history is available.
            </p>
        `;

        return;
    }


    list.innerHTML =
        data.map(
            history => `

                <div class="timeline-item">

                    <strong>

                        ${escapeHtml(
                            formatValue(
                                history.status
                            )
                        )}

                    </strong>


                    <p>

                        ${escapeHtml(
                            history.message ||
                            history.description ||
                            "Fault status updated."
                        )}

                    </p>


                    <small>

                        ${formatDateTime(
                            history.createdAt ||
                            history.updatedAt
                        )}

                    </small>

                </div>

            `
        ).join("");
}


/* =========================================================
   CONFIRM RESOLUTION
========================================================= */

const confirmationForm =
    document.getElementById(
        "confirmationForm"
    );


if (confirmationForm) {

    confirmationForm.addEventListener(
        "submit",
        async event => {

            event.preventDefault();

            clearCustomerMessage();


            const reference =
                getReferenceFromUrl();


            if (!reference) {

                showCustomerMessage(
                    "Fault reference is missing."
                );

                return;
            }


            const message =
                document
                    .getElementById(
                        "confirmationMessage"
                    )
                    .value
                    .trim();


            if (!message) {

                showCustomerMessage(
                    "Confirmation message is required."
                );

                return;
            }


            const button =
                document.getElementById(
                    "confirmButton"
                );


            button.disabled = true;

            button.textContent =
                "Confirming...";


            try {

                const response =
                    await apiRequest(
                        `/faults/${encodeURIComponent(
                            reference
                        )}/confirm-resolution`,
                        {
                            method: "PATCH",

                            body:
                                JSON.stringify({
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

                    showCustomerMessage(
                        getBackendMessage(
                            data
                        )
                    );

                    return;
                }


                showCustomerMessage(
                    "Resolution confirmed successfully. The fault is now closed.",
                    "success"
                );


                setTimeout(
                    () => {

                        window.location.reload();

                    },
                    1000
                );


            } catch (error) {

                console.error(
                    "Resolution confirmation error:",
                    error
                );


                showCustomerMessage(
                    "Unable to confirm the resolution."
                );


            } finally {

                button.disabled = false;

                button.textContent =
                    "Confirm Resolution";
            }
        }
    );
}


/* =========================================================
   HELPERS
========================================================= */

function getReferenceFromUrl() {

    return new URLSearchParams(
        window.location.search
    ).get(
        "reference"
    );
}


function setText(
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


/* =========================================================
   STATUS CLASS
========================================================= */

function statusClass(status) {

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


/* =========================================================
   PRIORITY CLASS
========================================================= */

function priorityClass(priority) {

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


/* =========================================================
   FORMAT ENUM VALUES
========================================================= */

function formatValue(value) {

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
            letter =>
                letter.toUpperCase()
        );
}


/* =========================================================
   FORMAT DATE
========================================================= */

function formatDate(value) {

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


/* =========================================================
   FORMAT DATE AND TIME
========================================================= */

function formatDateTime(value) {

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


/* =========================================================
   FORMAT FILE SIZE
========================================================= */

function formatFileSize(bytes) {

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


/* =========================================================
   BACKEND ERROR MESSAGE
========================================================= */

function getBackendMessage(data) {

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


/* =========================================================
   HTML ESCAPING
========================================================= */

function escapeHtml(value) {

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