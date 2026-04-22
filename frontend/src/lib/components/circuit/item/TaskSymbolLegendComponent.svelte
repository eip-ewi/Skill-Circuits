<script lang="ts">
    import { TaskIcons } from "../../../dto/task_icons";

    const legendItems = Object.entries(TaskIcons).map(([type, icon]) => ({
        icon,
        label: type.charAt(0) + type.slice(1).toLowerCase(),
    }));
</script>

<section class="task-symbol-legend" aria-label="Task symbol legend">
    <button class="trigger" aria-label="Show task symbol legend" type="button">
        <span class="fa-solid fa-circle-info" aria-hidden="true"></span>
    </button>

    <div class="content" role="tooltip">
        <span class="title">Task symbols</span>
        {#each legendItems as item}
            <span class="legend-item">
                <span class="fa-solid fa-{item.icon}" aria-hidden="true"></span>
                <span>{item.label}</span>
            </span>
        {/each}
        <span class="legend-item">
            <span class="fa-solid fa-shapes" aria-hidden="true"></span>
            <span>Choice task</span>
        </span>
    </div>
</section>

<style>
    .task-symbol-legend {
        position: absolute;
        left: 2em;
        top: 0.5em;
        z-index: 5;
    }

    .trigger {
        background: none;
        border: none;
        color: var(--on-background-colour);
        cursor: help;
        font-size: 1.75em;
        padding: 0.25em;
    }

    .content {
        background-color: var(--block-colour);
        border: 1px solid var(--block-border-colour);
        border-radius: 0.5em;
        color: var(--on-block-colour);
        display: none;
        gap: 0.5em;
        min-width: 13em;
        padding: 0.75em 1em;
        position: absolute;
        left: 0;
        top: calc(100% + 0.25em);
    }

    .task-symbol-legend:hover .content,
    .task-symbol-legend:focus-within .content {
        display: grid;
    }

    .title {
        font-weight: 700;
    }

    .legend-item {
        align-items: center;
        display: inline-flex;
        gap: 0.375em;
        white-space: nowrap;
    }
</style>
