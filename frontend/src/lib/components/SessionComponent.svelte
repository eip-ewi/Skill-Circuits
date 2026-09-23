<script lang="ts">
    import { onMount } from "svelte";
    import {
        checkSession,
        onSessionRestored,
        openReloginWindow,
        RELOGIN_WINDOW_NAME,
        SESSION_EXPIRED_EVENT,
    } from "../logic/session";
    import Button from "./util/Button.svelte";

    let dialog: HTMLDialogElement;

    const CHECK_SESSION_TIME = 15 * 60 * 1_000; /*ms*/

    let checkingSession = false;
    let popupBlocked = $state(false);

    function logInAgain() {
        popupBlocked = !openReloginWindow();
    }

    function showExpiredDialog() {
        if (!dialog.open) {
            dialog.showModal();
        }
    }

    //  browser-based events can trigger more often
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

    onMount(() => {
        let expiryInterval = setInterval(() => void recheckSession(), CHECK_SESSION_TIME);

        //Guaranteed to trigger as soon as a backend call is made with expired auth
        window.addEventListener(SESSION_EXPIRED_EVENT, showExpiredDialog);

        let stopListeningForRestore = onSessionRestored(() => dialog.close());

        //Nice to haves, handling laptop going to sleep, not using the tab, etc.
        //Complementary to the 15-minute check as that is still neeeded when
        //the tab is in focus but not used for a longer period
        window.addEventListener("focus", recheckSession);
        window.addEventListener("pageshow", recheckSession);
        window.addEventListener("online", recheckSession);
        document.addEventListener("visibilitychange", recheckSession);

        return () => {
            clearInterval(expiryInterval);
            stopListeningForRestore();
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
    <p>Your session has expired. Click below to log in again in a separate window.</p>
    <Button primary onclick={logInAgain}>
        <span class="fa-solid fa-right-to-bracket"></span>
        <span>Log in</span>
    </Button>
    {#if popupBlocked}
        <!-- Common for a browser to block pop-ups, workaround for that -->
        <p class="blocked">
            Your browser blocked the login window. Allow pop-ups for this site, or
            <a href="/login" target={RELOGIN_WINDOW_NAME}>log in in a new tab</a>
            instead.
        </p>
    {/if}
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

    .blocked {
        max-width: 30rem;
    }
</style>
