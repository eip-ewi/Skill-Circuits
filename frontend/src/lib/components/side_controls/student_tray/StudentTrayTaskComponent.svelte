<script lang="ts">
    import moment from "moment";
    import type { TaskItem } from "../../../dto/circuit/module/task";
    import { canEditCircuit } from "../../../logic/authorisation.svelte";

    let { task }: { task: TaskItem } = $props();

    let draggable: boolean = $state(false);

    function dragStart(event: DragEvent) {
        event.dataTransfer!.effectAllowed = "move";
        event.dataTransfer!.setData("skill-circuits/item", task.id.toString());
    }

    function dragEnd(event: DragEvent) {}

    function isPastDeadline(deadline: string): boolean {
        return moment().isAfter(moment(deadline));
    }

    // Choice tasks do not display TaskItems in the Tray view
    let overdueInViewer: boolean = $derived(
        !canEditCircuit() &&
            task.taskType === "regular" &&
            !task.completed &&
            task.deadline !== null &&
            isPastDeadline(task.deadline),
    );
</script>

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div class="task" {draggable} ondragstart={dragStart} ondragend={dragEnd}>
    <div style="display: flex; gap: 1rem; flex: 1;">
        <div
            role="button"
            tabindex="0"
            aria-label="Move task to skill"
            class="grip fa-solid fa-grip-vertical"
            onmouseenter={() => (draggable = true)}
            onmouseleave={() => setTimeout(() => (draggable = false), 200)}>
        </div>
        <span class="name" data-overdue={overdueInViewer}>
            {task.name === "" && task.taskType === "choice" ? "Choice task" : task.name}
        </span>
    </div>

    {#if task.taskType === "regular"}
        <div class="task-info" data-overdue={overdueInViewer}>
            <div class="time">
                <span class="fa-solid fa-clock"></span>
                <span>{task.time}</span>
            </div>
            {#if task.deadline}
                <div class="deadline">
                    <span class="fa-solid fa-calendar"></span>
                    <span>{moment(task.deadline).format("DD/MM HH:mm")}</span>
                </div>
            {:else}
                <div class="deadline deadline-placeholder" aria-hidden="true">
                    <span class="fa-solid fa-calendar"></span>
                    <span>00/00 00:00</span>
                </div>
            {/if}
        </div>
    {/if}
</div>

<style>
    .task {
        display: flex;
        gap: 0.5em;
    }

    .name {
        font-size: var(--font-size-400);
    }

    .grip {
        cursor: grab;
        color: var(--drag-icon-color);
        display: flex;
        align-items: center;
    }

    .task-info {
        display: flex;
        gap: 1em;
    }

    .deadline {
        align-items: center;
        display: flex;
        font-variant-numeric: tabular-nums;
        gap: 0.5em;

        text-align: right;
    }

    .deadline span:last-child {
        inline-size: 11ch;
        white-space: nowrap;
    }

    .deadline-placeholder {
        visibility: hidden;
    }

    .time {
        align-items: center;
        display: flex;
        gap: 0.25em;
    }

    .time span:last-child {
        min-width: 3ch;
        text-align: right;
    }

    .name[data-overdue="true"],
    .task-info[data-overdue="true"] .time span:last-child,
    .task-info[data-overdue="true"] .deadline span:last-child {
        text-decoration: line-through;
    }
</style>
