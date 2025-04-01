<script lang="ts">
    import {getAuth} from "../data/auth";

    let expanded = $state(true);

    function toggle() {
        expanded = !expanded;
    }
</script>

{#if getAuth().canEditModule}
    <div class="sidebar" aria-expanded="{expanded}">
        {#if expanded}
            <button>
                <span class="fa-solid fa-book"></span>
                <span>Enter student mode</span>
            </button>
            <button onclick={toggle}>
                <span class="fa-solid fa-chevron-left"></span>
                <span>Collapse</span>
            </button>
        {:else }
            <button onclick={toggle}>
                <span class="fa-solid fa-chevron-right"></span>
                <span>Expand</span>
            </button>
        {/if}
    </div>
{/if}

<style>
    .sidebar {
        backdrop-filter: blur(.25rem);
        background-color: var(--header-colour);
        border-radius: 100vw;
        display: grid;
        gap: 2rem;
        left: 2rem;
        max-width: 8rem;
        padding: 3rem 1rem;
        position: fixed;
        top: 50%;
        transform: translateY(-50%);
        z-index: 100;
    }
    .sidebar[aria-expanded="false"] {
        bottom: 1rem;
        padding: 2rem 1rem;
        top: initial;
        transform: initial;
    }
    button {
        background: none;
        border: none;
        cursor: pointer;
        display: grid;
        gap: .25rem;
    }
    button > :first-child {
        font-size: 1.5rem;
        transition: transform ease-in 80ms;
        transform-origin: bottom;
    }
    button:focus-visible, button:hover {
        color: var(--on-header-active-colour);
    }
    button:focus-visible > :first-child, button:hover > :first-child {
        transform: scale(1.1);
    }
</style>