<script lang="ts">

    import type {Item} from "../../../dto/circuit/item";
    import {deleteItem, editItemName} from "../../../logic/circuit/updates/item_updates";
    import TaskTypeEditComponent from "./TaskTypeEditComponent.svelte";
    import TaskTimeEditComponent from "./TaskTimeEditComponent.svelte";
    import TaskLinkEditComponent from "./TaskLinkEditComponent.svelte";
    import TaskPathEditComponent from "./TaskPathEditComponent.svelte";
    import type {Snippet} from "svelte";
    import type {ChoiceTaskChoice, TaskInfo} from "../../../dto/circuit/module/task";
    import {deleteSubtask, editTaskInfoName, editTaskLink} from "../../../logic/circuit/updates/task_updates";

    let { taskInfo, children }: { taskInfo: TaskInfo, children?: Snippet } = $props();

    let draggable: boolean = $state(false);

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editTaskInfoName(taskInfo, newName);
    }

    async function remove() {
        await deleteSubtask(taskInfo as ChoiceTaskChoice);
    }

    function dragStart(event: DragEvent) {
        event.dataTransfer!.effectAllowed = "move";
        event.dataTransfer!.setData("skill-circuits/task-info", taskInfo.infoId.toString());
    }

    function dragEnd() {

    }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div class="item" draggable={draggable} ondragstart={dragStart} ondragend={dragEnd}>
    {#if taskInfo.taskType === "choice"}
        <div role="button" tabindex="0" aria-label="Move item"
             class="grip fa-solid fa-grip-vertical"
             onmouseenter={ () => draggable = true } onmouseleave={ () => setTimeout(() => draggable = false, 200) }></div>
    {/if}
    <TaskTypeEditComponent {taskInfo}></TaskTypeEditComponent>
    <input class="name" value={taskInfo.name} onchange={editName}/>
    <TaskTimeEditComponent {taskInfo}></TaskTimeEditComponent>
    <TaskLinkEditComponent {taskInfo}></TaskLinkEditComponent>
    {@render children?.()}
    {#if taskInfo.taskType === "choice"}
        <button aria-label="Remove item" class="button danger fa-solid fa-trash" onclick={remove}></button>
    {/if}
</div>

<style>
    .item {
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

    .name {
        border: 1px solid var(--on-block-divider-colour);
        border-radius: 8px;
        flex-grow: 1;
        padding: .25rem .5rem;
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