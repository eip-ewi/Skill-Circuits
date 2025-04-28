<script lang="ts">

    import type {Item} from "../../../dto/circuit/item";
    import {deleteItem, editItemName} from "../../../logic/circuit/updates/item_updates";
    import TaskTypeEditComponent from "./TaskTypeEditComponent.svelte";
    import TaskTimeEditComponent from "./TaskTimeEditComponent.svelte";
    import TaskLinkEditComponent from "./TaskLinkEditComponent.svelte";
    import TaskPathEditComponent from "./TaskPathEditComponent.svelte";
    import type {Snippet} from "svelte";
    import type {ChoiceTaskItem, RegularTaskItem, TaskItem} from "../../../dto/circuit/module/task";
    import TaskInfoEditComponent from "./TaskInfoEditComponent.svelte";
    import {getBlocks, getItem} from "../../../logic/circuit/circuit.svelte";
    import {editChoiceTaskMinTasks, moveSubtask, moveTaskInsideOfChoiceTask} from "../../../logic/circuit/updates/task_updates";

    let { task }: { task: ChoiceTaskItem } = $props();

    let dragging: boolean = $state(false);

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editItemName(task, newName);
    }

    async function editMinTasks(event: Event) {
        const newMinTasks = parseInt((event.target as HTMLInputElement).value);
        await editChoiceTaskMinTasks(task, newMinTasks);
    }

    function dragEnter(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/regular-task") && !event.dataTransfer!.types.includes("skill-circuits/task-info")) {
            return;
        }
        event.preventDefault();
        event.stopPropagation();
        dragging = true;
    }

    function dragLeave() {
        dragging = false;
    }

    function dragOver(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/regular-task") && !event.dataTransfer!.types.includes("skill-circuits/task-info")) {
            return;
        }
        event.preventDefault();
        event.stopPropagation();
        dragging = true;
    }

    async function drop(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/regular-task") && !event.dataTransfer!.types.includes("skill-circuits/task-info")) {
            return;
        }
        event.preventDefault();

        if (event.dataTransfer!.types.includes("skill-circuits/regular-task")) {
            let taskId = parseInt(event.dataTransfer!.getData("skill-circuits/regular-task"));
            let subtask = getItem(taskId) as RegularTaskItem;
            await moveTaskInsideOfChoiceTask(task, subtask);
        }

        if (event.dataTransfer!.types.includes("skill-circuits/task-info")) {
            let taskInfoId = parseInt(event.dataTransfer!.getData("skill-circuits/task-info"));
            let oldChoiceTask = getBlocks().flatMap(block => block.items.filter(item => item.itemType === "task" && item.taskType === "choice")).find(choiceTask => choiceTask.tasks.some(info => info.infoId === taskInfoId))!;
            let subtask = oldChoiceTask.tasks.find(info => info.infoId === taskInfoId)!;
            await moveSubtask(subtask, task, oldChoiceTask);
        }

        dragging = false;
    }

</script>

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div class="task" ondragenter={dragEnter} ondragleave={dragLeave} ondragover={dragOver} ondrop={drop} data-dragging={dragging}>
    <div class="heading">
        <input name="name" type="text" value={task.name} onchange={editName} placeholder="An optional name..."/>
        <div class="min-tasks">
            <label for="min-tasks-{task.id}">Complete at least</label>
            <input id="min-tasks-{task.id}" name="minTasks" type="number" value={task.minTasks} min="1" max={Math.max(1, task.tasks.length - 1)} onchange={editMinTasks}/>
        </div>
    </div>
    {#each task.tasks as subtask}
        <TaskInfoEditComponent taskInfo={subtask}></TaskInfoEditComponent>
    {/each}
    <div class="drop-indicator"></div>
</div>

<style>

    .task {
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 16px;
        display: grid;
        gap: 0.5rem;
        padding: 1.5rem .5rem .5rem .5rem;
        position: relative;
        margin-bottom: .5rem;
        margin-top: 1rem;
        min-height: 2rem;
        width: 100%;
    }

    .heading {
        display: flex;
        gap: 1rem;
        left: 0;
        padding-inline: 1rem;
        position: absolute;
        justify-content: space-between;
        top: 0;
        transform: translateY(-50%);
        width: 100%;
    }

    input {
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 8px;
        padding: 0.25rem .5rem;
    }

    .min-tasks {
        align-items: center;
        display: flex;
        gap: 0.375rem;
    }

    .min-tasks label {
        background-color: var(--block-colour);
        font-size: var(--font-size-200);
        padding-inline: .25rem;
    }

    .min-tasks input {
        max-width: 3.5rem;
    }

    .drop-indicator {
        background-color: var(--on-background-highlight-colour);
        border-radius: inherit;
        inset: 0;
        position: absolute;
    }
    .task[data-dragging="false"] .drop-indicator {
        display: none;
    }

</style>