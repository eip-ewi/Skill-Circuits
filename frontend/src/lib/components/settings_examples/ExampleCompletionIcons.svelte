<script lang="ts">
    import { TaskIcons } from "../../dto/task_icons";
    import { getAdditionalIcons } from "../../logic/preferences.svelte.js";
    import moment from "moment/moment";
</script>

<div class="block-wrapper">
    <div class="block">
        <div class="heading">
            <span class="name">Example Skill</span>
        </div>

        <div class="tasks">
            <span class="task fa-solid fa-{TaskIcons.VIDEO}">
                {#if getAdditionalIcons()}
                    <span class="task-checkmark fa-solid fa-check"></span>
                {/if}
            </span>
            <span class="task fa-solid fa-{TaskIcons.READING}">
                {#if getAdditionalIcons()}
                    <span class="task-checkmark fa-solid fa-check"></span>
                {/if}
            </span>
            <span class="task fa-solid fa-{TaskIcons.EXERCISE}">
                {#if getAdditionalIcons()}
                    <span class="task-checkmark fa-solid fa-check"></span>
                {/if}
            </span>
        </div>

        {#if getAdditionalIcons()}
            <span class="skill-checkmark fa-solid fa-check"></span>
        {/if}
    </div>
</div>

<div class="content">
    <div class="info">
        <span class="label">Example Checkpoint</span>
        <span class="deadline">{moment().format("D MMMM YYYY HH:mm")}</span>
        {#if getAdditionalIcons()}
            <span class="checkpoint-checkmark fa-solid fa-check"></span>
        {/if}
    </div>
</div>

<style>
    .block-wrapper {
        position: relative;
        transition: transform ease-in-out 150ms;

        width: 12em;
        margin: 1em;
        left: -1em;
    }

    .block {
        border: var(--block-border);
        border-radius: var(--block-border-radius);
        box-shadow: 0.75rem 1.25rem 1.625rem 0
            color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        display: flex;
        flex-direction: column;
        gap: 0.5em;
        padding: 1em;
        position: relative;
        background-color: var(--block-completed-colour);
        border: var(--block-completed-border);
        color: var(--on-block-completed-colour);
    }

    .heading {
        display: flex;
        gap: 0;
        flex-direction: column;
    }

    .name {
        font-size: 1.25em;
        font-weight: 700;
    }

    .tasks {
        display: flex;
        gap: 0.25em;
        color: var(--on-block-task-completed-colour);
    }

    .skill-checkmark {
        position: absolute;
        bottom: 0.7em;
        right: 1em;
        color: var(--on-block-task-completed-colour);
    }

    .task-checkmark {
        font-size: 70%;
        position: relative;
        float: right;
        margin-left: 0.2em;
        margin-top: -0.4em;
    }

    .content {
        align-items: flex-start;
        display: flex;
        gap: 1em;
        margin-top: 0.5em;
    }

    .info {
        backdrop-filter: blur(0.25rem);
        border: var(--checkpoint-surface-border);
        border-radius: var(--checkpoint-surface-border-radius);
        display: grid;
        padding: 0.5em 1em;
        background-color: var(--checkpoint-completed-surface-colour);
        color: var(--on-checkpoint-completed-surface-colour);
    }

    .label {
        white-space: nowrap;
        grid-column: 1;
    }

    .deadline {
        font-size: var(--font-size-200);
        white-space: nowrap;
        grid-column: 1;
    }

    .checkpoint-checkmark {
        grid-column: 2;
        margin-left: 1em;
        align-self: end;
    }
</style>
