<script lang="ts">
    import { TaskIcons } from "../../../dto/task_icons";

    let { open = $bindable() }: { open: boolean } = $props();

    const legendItems = Object.entries(TaskIcons).map(([type, icon]) => ({
        icon,
        label: type.charAt(0) + type.slice(1).toLowerCase(),
    }));
</script>

<div class="scrollable glass panel" aria-expanded={open}>
    <div class="heading">
        <h2>Task symbols</h2>
        <div class="controls">
            <button class="button" aria-label="Close panel" onclick={() => (open = false)}>
                <span class="fa-solid fa-arrow-right"></span>
            </button>
        </div>
    </div>
    <div class="legend">
        {#each legendItems as item}
            <div class="legend-item">
                <span class="fa-solid fa-{item.icon}" aria-hidden="true"></span>
                <span>{item.label}</span>
            </div>
        {/each}
        <div class="legend-item">
            <span class="fa-solid fa-shapes" aria-hidden="true"></span>
            <span>Choice task</span>
        </div>
    </div>
</div>

<style>
    .panel {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
        inset-block: 2em;
        max-width: 32em;
        overflow-y: auto;
        overscroll-behavior: contain;
        position: fixed;
        right: 0;
        top: 2em;
        transform-origin: right;
        transition: transform ease-in-out 150ms;
        z-index: 91;
    }

    .panel[aria-expanded="false"] {
        transform: scaleX(0);
    }
    .panel[aria-expanded="true"] {
        transition-delay: 150ms;
    }

    .heading {
        align-items: center;
        display: flex;
        justify-content: space-between;
        gap: 2rem;
        padding: 2em 2em 1em 2em;
    }

    .heading h2 {
        font-size: var(--font-size-500);
        font-weight: 700;
    }

    .controls {
        display: flex;
        gap: 0.25em;
    }

    .legend {
        display: grid;
        gap: 1rem;
        padding: 0 2rem 2rem 2rem;
    }

    .legend-item {
        align-items: center;
        display: flex;
        gap: 0.75em;
    }

    .button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: 0.5em;
        color: var(--on-glass-colour);
        cursor: pointer;
        display: grid;
        justify-items: center;
        padding: 0.5em;
        text-decoration: none;
    }
    .button:focus-visible,
    .button:hover {
        background: var(--on-glass-surface-active-colour);
    }
</style>
