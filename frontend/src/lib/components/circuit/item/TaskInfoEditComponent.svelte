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
    import Button from "../../util/Button.svelte";
    import WithConfirmationDialog from "../../util/WithConfirmationDialog.svelte";

    let { taskInfo, children }: { taskInfo: TaskInfo, children?: Snippet } = $props();

    let draggable: boolean = $state(false);

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editTaskInfoName(taskInfo, newName);
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
    <input class="name" name="item-name" value={taskInfo.name} onchange={editName}/>
    <TaskTimeEditComponent {taskInfo}></TaskTimeEditComponent>
    <TaskLinkEditComponent {taskInfo}></TaskLinkEditComponent>
    {@render children?.()}
    {#if taskInfo.taskType === "choice"}
        <WithConfirmationDialog onconfirm={ () => deleteSubtask(taskInfo) } icon="fa-solid fa-trash" action="Delete">
            {#snippet button(showDialog: () => void) }
                <Button square primary type="caution" aria-label="Delete item" onclick={showDialog}>
                    <span class="fa-solid fa-trash"></span>
                </Button>
            {/snippet}
            <p>
                Are you sure you want to delete '{taskInfo.name}'?
            </p>
        </WithConfirmationDialog>
    {/if}
</div>

<style>
    .item {
        align-items: center;
        display: flex;
        gap: 0.5em;
    }

    .grip {
        cursor: grab;
        opacity: 0.5;
    }

    .name {
        background-color: var(--neutral-surface-colour);
        border: 1px solid var(--on-block-divider-colour);
        border-radius: .5em;
        color: var(--on-neutral-surface-colour);
        flex-grow: 1;
        padding: .25em .5em;
    }
</style>