<script lang="ts">
    import type { TaskItem } from "../../../dto/circuit/module/task";
    import { hasEditorRights } from "../../../logic/authorisation.svelte";
    import { TaskIcons } from "../../../dto/task_icons.js";
    import { getAdditionalIcons } from "../../../logic/preferences.svelte";
    import { isTaskCompleted } from "../../../logic/circuit/skill_state/completion";

    let { task }: { task: TaskItem } = $props();

    let taskCompleted = $derived(
        task.taskType === "regular"
            ? task.completed && !hasEditorRights()
            : isTaskCompleted(task) && !hasEditorRights(),
    );

    let showCheckmark = $derived(taskCompleted && !hasEditorRights() && getAdditionalIcons());
</script>

<div class="task_container" data-completed={taskCompleted}>
    {#if task.taskType === "regular"}
        <span class="task fa-solid fa-{TaskIcons[task.type]}">
            {#if showCheckmark}
                <span class="checkmark fa-solid fa-check"></span>
            {/if}
        </span>
    {:else}
        <span class="task fa-solid fa-shapes">
            {#if showCheckmark}
                <span class="checkmark fa-solid fa-check"></span>
            {/if}
        </span>
    {/if}
</div>

<style>
    .task_container {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 0.15em;
    }

    .task_container[data-completed="true"] {
        color: var(--on-block-task-completed-colour);
    }

    .checkmark {
        font-size: 70%;
        position: relative;
        float: right;
        margin-left: 0.2em;
        margin-top: -0.4em;
    }
</style>
