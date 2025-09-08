<script lang="ts">

    import {clearLevel} from "../logic/circuit/level.svelte";
    import PageLayout from "./PageLayout.svelte";

</script>

<PageLayout>

    <div class="content">
        <h1>Welcome to Skill Circuits!</h1>

        <a href="/login">Click here to log in</a>
    </div>

</PageLayout>

<style>
    .content {
        display: grid;
        place-items: center;
    }

    h1 {
        color: var(--on-background-colour);
        font-size: var(--font-size-700);
        font-weight: 500;
        text-align: center;
        margin-bottom: 2rem;
    }

    a {
        background-color: var(--block-colour);
        border: none;
        border-radius: 8px;
        color: var(--on-block-colour);
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        cursor: pointer;
        display: grid;
        place-items: center;
        padding: .5rem 1rem;
        text-decoration: none;
    }
    a:where(:hover, :focus-visible) {
        transform: scale(1.05);
        box-shadow: .75rem 1.25rem 1.8rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
    }
</style>