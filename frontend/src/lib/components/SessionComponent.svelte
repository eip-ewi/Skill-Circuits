<script lang="ts">
    import { onMount } from "svelte";
    import { checkSession, SESSION_EXPIRED_EVENT } from "../logic/session";
    import Button from "./util/Button.svelte";

    let dialog: HTMLDialogElement;

    const CHECK_SESSION_TIME = 15 * 60 * 1_000; /*ms*/
    const CHECK_RELOGIN_TIME = 5_000; /*ms*/

    let checkingSession = false;
    let reloginInterval = 0;

    function showExpiredDialog() {
        if (!dialog.open) {
            dialog.showModal();
        }
    }

    // Necessary for browser-based events, these can trigger more often
    // This was implemented defensively, all of the 4 events mounted fire
    // sparcely. Check their respective MDN pages
    async function recheckSession() {
        if (checkingSession || document.visibilityState !== "visible") {
            return;
        }

        checkingSession = true;

        try {
            if (!(await checkSession())) {
                showExpiredDialog();
            }
        } finally {
            checkingSession = false;
        }
    }

    function startCheckingSuccessfulLogin() {
        clearInterval(reloginInterval);

        reloginInterval = setInterval(async () => {
            if (await checkSession()) {
                clearInterval(reloginInterval);
                //Closing the dialog is not enough,
                // there is stale state, for example the CSRF token
                //TODO: Check issue #268
                window.location.reload();
            }
        }, CHECK_RELOGIN_TIME);
    }

    onMount(() => {
        let expiryInterval = setInterval(() => void recheckSession(), CHECK_SESSION_TIME);

        //Guaranteed to trigger as soon as a backend call is made with expired auth
        window.addEventListener(SESSION_EXPIRED_EVENT, showExpiredDialog);

        //Nice to haves, handling laptop going to sleep, not using the tab, etc.
        //Complementary to the 15-minute check as that is still neeeded when
        //the tab is in focus but not used for a longer period
        window.addEventListener("focus", recheckSession);
        window.addEventListener("pageshow", recheckSession);
        window.addEventListener("online", recheckSession);
        document.addEventListener("visibilitychange", recheckSession);

        return () => {
            clearInterval(expiryInterval);
            clearInterval(reloginInterval);
            window.removeEventListener(SESSION_EXPIRED_EVENT, showExpiredDialog);
            window.removeEventListener("focus", recheckSession);
            window.removeEventListener("pageshow", recheckSession);
            window.removeEventListener("online", recheckSession);
            document.removeEventListener("visibilitychange", recheckSession);
        };
    });
</script>

<dialog bind:this={dialog} class="dialog">
    <h2>Session expired</h2>
    <p>Your session has expired. Click below to log in again in a new tab.</p>
    <Button primary href="/login" target="_blank" onclick={startCheckingSuccessfulLogin}>
        <span class="fa-solid fa-right-to-bracket"></span>
        <span>Log in</span>
    </Button>
</dialog>

<style>
    .dialog {
        justify-items: start;
        background-color: var(--block-colour);
        border: none;
        border-radius: 16px;
        box-shadow: 2rem 2rem 4rem color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        display: grid;
        gap: 1rem;
        left: 50%;
        padding: 2rem;
        position: fixed;
        top: 50%;
        transform: translate(-50%, -50%);
    }

    .dialog:not([open]) {
        display: none;
    }

    .dialog::backdrop {
        background: none;
        backdrop-filter: blur(0.5rem);
    }

    .dialog h2 {
        font-size: 2rem;
        font-weight: 700;
    }
</style>
