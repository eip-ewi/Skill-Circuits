<script lang="ts">

    import type {Item} from "../../../dto/circuit/item";
    import {deleteItem, editItemName} from "../../../logic/circuit/updates/item_updates";
    import TaskTypeEditComponent from "./TaskTypeEditComponent.svelte";
    import TaskTimeEditComponent from "./TaskTimeEditComponent.svelte";
    import TaskLinkEditComponent from "./TaskLinkEditComponent.svelte";
    import TaskPathEditComponent from "./TaskPathEditComponent.svelte";
    import type {TaskItem} from "../../../dto/circuit/module/task";
    import ItemEditComponent from "./ItemEditComponent.svelte";
    import ChoiceTaskEditComponent from "./ChoiceTaskEditComponent.svelte";
    import {editTaskInfoName} from "../../../logic/circuit/updates/task_updates";
    import TaskInfoEditComponent from "./TaskInfoEditComponent.svelte";

    let { task }: { task: TaskItem } = $props();

    let draggable: boolean = $state(false);

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editItemName(task, newName);
    }

    function dragStart(event: DragEvent) {
        event.dataTransfer!.effectAllowed = "move";
        event.dataTransfer!.setData("skill-circuits/item", task.id.toString());
        if (task.taskType === "regular") {
            event.dataTransfer!.setData("skill-circuits/regular-task", task.id.toString());
        } else {
            event.dataTransfer!.setData("skill-circuits/choice-task", task.id.toString());
        }
    }

    function dragEnd() {

    }

    async function remove() {
        await deleteItem(task);
    }

</script>

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div class="task" draggable={draggable === true} ondragstart={dragStart} ondragend={dragEnd}>
    <div role="button" tabindex="0" aria-label="Move item"
         class="grip fa-solid fa-grip-vertical"
         onmouseenter={ () => draggable = true } onmouseleave={ () => setTimeout(() => draggable = false, 200) }></div>
    {#if task.taskType === "regular"}
        <TaskInfoEditComponent taskInfo={task}>
            <TaskPathEditComponent task={task}></TaskPathEditComponent>
        </TaskInfoEditComponent>
        <button aria-label="Remove item" class="button danger fa-solid fa-trash" onclick={remove}></button>
    {:else}
        <ChoiceTaskEditComponent {task}></ChoiceTaskEditComponent>
        <TaskPathEditComponent task={task}></TaskPathEditComponent>
        <button aria-label="Remove item" class="button danger fa-solid fa-trash" onclick={remove}></button>
    {/if}
</div>

<style>
    .task {
        align-items: center;
        display: flex;
        gap: 0.5rem;
    }

    .grip {
        background: none;
        border: none;
        color: grey;
        cursor: grab;
    }

    .button {
        aspect-ratio: 1 / 1;
        background-color: var(--primary-surface-colour);
        border: none;
        border-radius: 8px;
        color: var(--on-primary-surface-colour);
        cursor: pointer;
        display: grid;
        min-width: 2rem;
        place-items: center;
    }

    .button:where(:hover, :focus-visible) {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }

    .button.danger {
        background-color: var(--primary-error-colour);
        color: var(--on-error-surface-colour);
    }
    .button.danger:where(:hover, :focus-visible) {
        background-color: var(--primary-error-active-colour);
        color: var(--on-error-surface-colour);
    }
</style>