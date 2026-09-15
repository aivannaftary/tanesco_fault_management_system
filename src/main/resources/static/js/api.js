/* =========================================================
   TANESCO FAULT MANAGEMENT SYSTEM
   API HELPER
========================================================= */

const API_BASE_URL = "/api";


/*
 * =========================================================
 * TOKEN MANAGEMENT
 * =========================================================
 */

function saveToken(token) {

    localStorage.setItem(
        "tanesco_token",
        token
    );
}


function getToken() {

    return localStorage.getItem(
        "tanesco_token"
    );
}


function removeToken() {

    localStorage.removeItem(
        "tanesco_token"
    );
}


/*
 * =========================================================
 * USER INFORMATION
 * =========================================================
 */

function saveUser(user) {

    localStorage.setItem(
        "tanesco_user",
        JSON.stringify(user)
    );
}


function getUser() {

    const user =
        localStorage.getItem(
            "tanesco_user"
        );

    if (!user) {
        return null;
    }

    try {

        return JSON.parse(user);

    } catch (error) {

        removeUser();

        return null;
    }
}


function removeUser() {

    localStorage.removeItem(
        "tanesco_user"
    );
}


/*
 * =========================================================
 * LOGOUT
 * =========================================================
 */

function logout() {

    removeToken();
    removeUser();

    window.location.href = "/";
}


/*
 * =========================================================
 * AUTHENTICATED REQUEST
 * =========================================================
 */

async function apiRequest(
    endpoint,
    options = {}
) {

    const token = getToken();

    const headers = {
        ...(options.headers || {})
    };


    /*
     * Only add JSON Content-Type when the body
     * is not FormData.
     *
     * This is important for evidence uploads.
     */
    if (
        options.body &&
        !(options.body instanceof FormData) &&
        !headers["Content-Type"]
    ) {

        headers["Content-Type"] =
            "application/json";
    }


    if (token) {

        headers["Authorization"] =
            `Bearer ${token}`;
    }


    const response =
        await fetch(
            `${API_BASE_URL}${endpoint}`,
            {
                ...options,
                headers
            }
        );


    /*
     * Automatically remove expired/invalid login
     * information when backend returns 401.
     */
    if (response.status === 401) {

        removeToken();
        removeUser();
    }


    return response;
}


/*
 * =========================================================
 * READ RESPONSE
 * =========================================================
 */

async function readResponse(
    response
) {

    const contentType =
        response.headers.get(
            "content-type"
        );


    if (
        contentType &&
        contentType.includes(
            "application/json"
        )
    ) {

        return await response.json();
    }


    const text =
        await response.text();


    if (!text) {
        return null;
    }


    return {
        message: text
    };
}