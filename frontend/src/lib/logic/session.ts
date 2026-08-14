export const SESSION_EXPIRED_EVENT = "skill-circuits:session-expired";

export async function checkSession(): Promise<boolean> {
    let response = await fetch("/api/auth/status");
    return response.ok;
}

// Wrapper around default fetch function
// Guaranteed the session is still active
export function installSessionFetchHandler() {
    let fetchWithoutSessionHandler = window.fetch.bind(window);

    // input - url
    // init - params
    window.fetch = async (input, init) => {
        let response = await fetchWithoutSessionHandler(input, init);

        if (response.status === 401) {
            window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT));
        }

        return response;
    };
}
