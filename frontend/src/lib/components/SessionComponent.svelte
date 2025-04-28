<script lang="ts">
    import {onMount} from "svelte";
    import {checkSession} from "../logic/session";

    let dialog: HTMLDialogElement;

    const CHECK_SESSION_TIME = 15 * 60 * 1_000 /*ms*/;
    const CHECK_RELOGIN_TIME = 5_000 /*ms*/;

    function startCheckingExpiry() {
        setTimeout(async () => {
            if (await checkSession()) {
                startCheckingExpiry();
            } else {
                dialog.showModal();
            }
        }, CHECK_SESSION_TIME);
    }

    function startCheckingSuccessfulLogin() {
        setTimeout(async () => {
            if (await checkSession()) {
                dialog.close();
                startCheckingExpiry();
            } else {
                startCheckingSuccessfulLogin();
            }
        }, CHECK_RELOGIN_TIME);
    }

    onMount(async () => {
        if (await checkSession()) {
            startCheckingExpiry();
        }
    });
</script>

<dialog bind:this={dialog} class="dialog">
    <h2>Session expired</h2>
    <p>Your session has expired. Click below to log in again in a new tab.</p>
    <a class="button" href="/auth/login" target="_blank" onclick={startCheckingSuccessfulLogin}>Log in</a>
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
        backdrop-filter: blur(.5rem);
    }

    .dialog h2 {
        font-size: 2rem;
        font-weight: 700;
    }

    .button {
        background-color: var(--primary-surface-colour);
        border-radius: 8px;
        color: var(--on-primary-surface-colour);
        padding: .5rem 1rem;
        text-decoration: none;
    }

    .button:where(:hover, :focus-visible) {
        background-color: var(--primary-surface-active-colour);
    }
</style>