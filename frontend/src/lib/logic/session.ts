import { checkAuthentication, getAuthenticatedPerson } from "./authentication.svelte";
import { type CsrfToken, getCsrfToken, setCsrfToken } from "./csrf";

export const SESSION_EXPIRED_EVENT = "skill-circuits:session-expired";

// Every tab of the app listens here, so a session restored in one of them
// is picked up by all the others without anyone having to refresh
const SESSION_CHANNEL_NAME = "skill-circuits:session";

// The window opened to log in again recognises itself by this name.
// A browsing context keeps its name across navigations, so it survives
// the redirects of the login flow.
export const RELOGIN_WINDOW_NAME = "skill-circuits-relogin";
const RELOGIN_WINDOW_WIDTH = 520;
const RELOGIN_WINDOW_HEIGHT = 700;

let sessionChannel: BroadcastChannel | undefined;

export async function checkSession(): Promise<boolean> {
    const response = await fetch("/api/auth/status");
    return response.ok;
}

// Wrapper around default fetch function
// Guaranteed the session is still active
export function installSessionFetchHandler() {
    const fetchWithoutSessionHandler = window.fetch.bind(window);

    // input - url
    // init - params
    window.fetch = async (input, init) => {
        const response = await fetchWithoutSessionHandler(input, init);

        if (response.status === 401) {
            window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT));
        }

        return response;
    };
}

function getSessionChannel(): BroadcastChannel {
    //singleton pattern in JS
    //all tabs should be on the same channel
    return (sessionChannel ??= new BroadcastChannel(SESSION_CHANNEL_NAME));
}

// login window: center + small pop up
function reloginWindowFeatures(): string {
    const left = window.screenX + (window.outerWidth - RELOGIN_WINDOW_WIDTH) / 2;
    const top = window.screenY + (window.outerHeight - RELOGIN_WINDOW_HEIGHT) / 2;

    return [
        "popup=yes",
        `width=${RELOGIN_WINDOW_WIDTH}`,
        `height=${RELOGIN_WINDOW_HEIGHT}`,
        `left=${Math.round(left)}`,
        `top=${Math.round(top)}`,
    ].join(",");
}

// false if the pop-up is blocked, use a new tab instead
export function openReloginWindow(): boolean {
    return window.open("/login", RELOGIN_WINDOW_NAME, reloginWindowFeatures()) !== null;
}

export async function closeReloginWindowWhenLoggedIn() {
    if (window.name !== RELOGIN_WINDOW_NAME || !(await checkSession())) {
        return;
    }

    getSessionChannel().postMessage(getCsrfToken());
    window.close();
}

export function onSessionRestored(handler: () => void): () => void {
    const channel = getSessionChannel();

    const listener = async (event: MessageEvent<CsrfToken>) => {
        const csrf: CsrfToken = event.data;
        const previousPersonId = getAuthenticatedPerson()?.id;

        setCsrfToken(csrf);
        await checkAuthentication();

        // Somebody else logged in, nothing on the page is theirs
        if (getAuthenticatedPerson()?.id !== previousPersonId) {
            window.location.reload();
        }

        handler();
    };

    channel.addEventListener("message", listener);
    return () => channel.removeEventListener("message", listener);
}
