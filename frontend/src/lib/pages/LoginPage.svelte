<script lang="ts">

    import Csrf from "../components/Csrf.svelte";
    import {onMount} from "svelte";
    import PageLayout from "./PageLayout.svelte";

</script>

<PageLayout>
    <form method="post" action="/perform-login">
        <h1>Log In</h1>

        <Csrf></Csrf>

        <div class="groups">
            <div class="group">
                <label for="username">Username</label>
                <input type="text" name="username" id="username"/>
            </div>

            <div class="group">
                <label for="password">Password</label>
                <input type="password" name="password" id="password"/>
            </div>
        </div>

        <button>Log in</button>
    </form>
</PageLayout>

<style>

    form {
        display: grid;
        gap: 1rem;
        place-items: center;
    }

    h1 {
        color: var(--on-background-colour);
        font-size: var(--font-size-700);
        font-weight: 500;
        text-align: center;
    }

    .groups {
        background-color: var(--block-colour);
        border: none;
        border-radius: 16px;
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        display: grid;
        gap: .5rem;
        place-items: center;
        padding: 1rem;
    }

    .group {
        display: grid;
        gap: 0.125rem;
    }

    label {
        font-size: var(--font-size-200);
        color: var(--on-block-colour)
    }

    input {
        background-color: var(--neutral-surface-colour);
        border: 1px solid var(--on-block-divider-colour);
        color: var(--on-neutral-surface-colour);
        border-radius: 8px;
        padding: .5rem 1rem;
    }

    button {
        background-color: var(--block-colour);
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 8px;
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        cursor: pointer;
        display: grid;
        place-items: center;
        padding: .5rem 1rem;
    }
    button:where(:hover, :focus-visible) {
        transform: scale(1.05);
        box-shadow: .75rem 1.25rem 1.8rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
    }
</style>