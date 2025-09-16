<script lang="ts">
    import type {Warning} from "../data/warning";
    import {getAuthorisation} from "../logic/authorisation.svelte";

    let { warnings }: { warnings: Warning[] } = $props();
</script>

{#each warnings as warning}
    {#if warning.viewModes.includes(getAuthorisation().viewMode)}
        <div class="warning">
            <span class="fa-solid fa-triangle-exclamation"></span>
            <p>{warning.message}</p>
        </div>
    {/if}
{/each}

<style>
    .warning {
        align-items: center;
        background-color: hsla(30deg 100% 50% / 0.25);
        border-radius: 16px;
        color: hsla(30deg 100% 20%);
        display: flex;
        font-weight: 700;
        gap: 1rem;
        margin-bottom: 1rem;
        padding: 1rem 1.5rem;
    }
    .warning .fa-solid {
        font-size: var(--font-size-500);
    }
</style>