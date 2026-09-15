/* =========================================================
   TANESCO FAULT MANAGEMENT SYSTEM
   AUTHENTICATION
========================================================= */


/*
 * =========================================================
 * MESSAGE
 * =========================================================
 */

function showMessage(
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


function clearMessage() {

    const message =
        document.getElementById(
            "message"
        );

    if (!message) {
        return;
    }

    message.textContent = "";
    message.className = "message";
}


/*
 * =========================================================
 * EXTRACT ERROR MESSAGE
 * =========================================================
 */

function extractErrorMessage(data) {

    if (!data) {

        return "Something went wrong. Please try again.";
    }


    /*
     * Validation errors from GlobalExceptionHandler.
     */
    if (
        data.validationErrors &&
        typeof data.validationErrors === "object"
    ) {

        return Object.values(
            data.validationErrors
        ).join(" ");
    }


    if (data.message) {

        return data.message;
    }


    if (data.error) {

        return data.error;
    }


    return "Request failed. Please try again.";
}


/*
 * =========================================================
 * NORMALIZE ROLE
 * =========================================================
 */

function normalizeRole(role) {

    if (!role) {
        return "";
    }


    return String(role)
        .replace("ROLE_", "")
        .trim()
        .toUpperCase();
}


/*
 * =========================================================
 * REDIRECT USER BASED ON ROLE
 * =========================================================
 */

function redirectByRole(role) {

    const normalizedRole =
        normalizeRole(role);


    switch (normalizedRole) {

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


        case "ADMIN":

            window.location.href =
                "/admin/dashboard.html";

            break;


        default:

            showMessage(
                "Your account role could not be identified."
            );
    }
}


/*
 * =========================================================
 * PASSWORD VISIBILITY
 * =========================================================
 */

const togglePassword =
    document.getElementById(
        "togglePassword"
    );


if (togglePassword) {

    togglePassword.addEventListener(
        "click",
        () => {

            const password =
                document.getElementById(
                    "password"
                );


            if (!password) {
                return;
            }


            const isHidden =
                password.type ===
                "password";


            password.type =
                isHidden
                    ? "text"
                    : "password";


            togglePassword.textContent =
                isHidden
                    ? "Hide"
                    : "Show";
        }
    );
}


/*
 * =========================================================
 * LOGIN
 * =========================================================
 */

const loginForm =
    document.getElementById(
        "loginForm"
    );


if (loginForm) {

    loginForm.addEventListener(
        "submit",
        async event => {

            event.preventDefault();

            clearMessage();


            const username =
                document
                    .getElementById(
                        "username"
                    )
                    .value
                    .trim();


            const password =
                document
                    .getElementById(
                        "password"
                    )
                    .value;


            const button =
                document.getElementById(
                    "loginButton"
                );


            button.disabled = true;
            button.textContent =
                "Signing In...";


            try {

                const response =
                    await apiRequest(
                        "/auth/login",
                        {
                            method: "POST",

                            body:
                                JSON.stringify({
                                    username,
                                    password
                                })
                        }
                    );


                const data =
                    await readResponse(
                        response
                    );


                if (!response.ok) {

                    showMessage(
                        extractErrorMessage(
                            data
                        )
                    );

                    return;
                }


                /*
                 * Your backend login response
                 * already returns the JWT token.
                 */
                if (!data || !data.token) {

                    showMessage(
                        "Login succeeded but no authentication token was returned."
                    );

                    return;
                }


                saveToken(
                    data.token
                );


                /*
                 * Save useful information returned
                 * by LoginResponse.
                 */
                saveUser({
                    id: data.id,
                    username:
                        data.username
                        || username,

                    fullName:
                        data.fullName
                        || "",

                    email:
                        data.email
                        || "",

                    role:
                        normalizeRole(
                            data.role
                        )
                });


                /*
                 * Redirect according to backend role.
                 */
                redirectByRole(
                    data.role
                );


            } catch (error) {

                console.error(
                    "Login error:",
                    error
                );


                showMessage(
                    "Unable to connect to the server. Please make sure the application is running."
                );

            } finally {

                button.disabled = false;

                button.textContent =
                    "Sign In";
            }
        }
    );
}


/*
 * =========================================================
 * REGISTRATION
 * =========================================================
 */

const registerForm =
    document.getElementById(
        "registerForm"
    );


if (registerForm) {

    registerForm.addEventListener(
        "submit",
        async event => {

            event.preventDefault();

            clearMessage();


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


            const phone =
                document
                    .getElementById(
                        "phone"
                    )
                    .value
                    .trim();


            const password =
                document
                    .getElementById(
                        "password"
                    )
                    .value;


            const button =
                document.getElementById(
                    "registerButton"
                );


            button.disabled = true;

            button.textContent =
                "Creating Account...";


            try {

                const response =
                    await apiRequest(
                        "/auth/register",
                        {
                            method: "POST",

                            body:
                                JSON.stringify({
                                    username,
                                    fullName,
                                    email,
                                    phone,
                                    password
                                })
                        }
                    );


                const data =
                    await readResponse(
                        response
                    );


                if (!response.ok) {

                    showMessage(
                        extractErrorMessage(
                            data
                        )
                    );

                    return;
                }


                showMessage(
                    "Account created successfully. Redirecting to login...",
                    "success"
                );


                registerForm.reset();


                /*
                 * Return to login after successful
                 * customer registration.
                 */
                setTimeout(
                    () => {

                        window.location.href =
                            "/";

                    },
                    1500
                );


            } catch (error) {

                console.error(
                    "Registration error:",
                    error
                );


                showMessage(
                    "Unable to connect to the server. Please make sure the application is running."
                );

            } finally {

                button.disabled = false;

                button.textContent =
                    "Create Account";
            }
        }
    );
}