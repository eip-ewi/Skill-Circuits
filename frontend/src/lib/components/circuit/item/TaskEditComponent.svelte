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
    import Button from "../../util/Button.svelte";
    import WithConfirmationDialog from "../../util/WithConfirmationDialog.svelte";
    import {getBookmarks, isChoiceTaskBookmarked, isSkillBookmarked, isTaskInfoBookmarked} from "../../../logic/bookmarks.svelte";
    import {addChoiceTaskToBookmarkList, addSkillToBookmarkList, addTaskInfoToBookmarkList, removeChoiceTaskFromBookmarkList, removeSkillFromBookmarkList, removeTaskInfoFromBookmarkList} from "../../../logic/updates/bookmark_updates";
    import {BlockActions} from "../../../data/block_action";
    import BookmarkMenuComponent from "../../bookmark/BookmarkMenuComponent.svelte";
    import {getPaths} from "../../../logic/edition/edition.svelte";

    let { task }: { task: TaskItem } = $props();

    let draggable: boolean = $state(false);

    let bookmarksOpen: boolean = $state(false);

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

        <WithConfirmationDialog onconfirm={ () => deleteItem(task) } icon="fa-solid fa-trash" action="Delete">
            {#snippet button(showDialog: () => void) }
                <Button square primary type="caution" aria-label="Delete item" onclick={showDialog}>
                    <span class="fa-solid fa-trash" ></span>
                </Button>
            {/snippet}
            <p>
                Are you sure you want to delete '{task.name}'?
            </p>
        </WithConfirmationDialog>
    {:else}
        <ChoiceTaskEditComponent {task}></ChoiceTaskEditComponent>

        <BookmarkMenuComponent bind:open={bookmarksOpen} onLists={getBookmarks().filter(list => list.tasks.some(t => t.taskType === "choice" && t.id === task.id))}
                               addToList={ list => addChoiceTaskToBookmarkList(task, list) } removeFromList={ list => removeChoiceTaskFromBookmarkList(task, list) }>
            <Button square primary aria-label="Bookmark" onclick={ () => bookmarksOpen = true }>
                <span class="fa-bookmark" class:fa-regular={!isChoiceTaskBookmarked(task)} class:fa-solid={isChoiceTaskBookmarked(task)}></span>
            </Button>
        </BookmarkMenuComponent>

        <TaskPathEditComponent task={task}></TaskPathEditComponent>

        <WithConfirmationDialog onconfirm={ () => deleteItem(task) } icon="fa-solid fa-trash" action="Delete">
            {#snippet button(showDialog: () => void) }
                <Button square primary type="caution" aria-label="Delete item" onclick={showDialog}>
                    <span class="fa-solid fa-trash" ></span>
                </Button>
            {/snippet}
            <p>
                Are you sure you want to delete this task?
            </p>
        </WithConfirmationDialog>
    {/if}
</div>

<style>
    .task {
        align-items: center;
        display: flex;
        gap: 0.5em;
    }

    .grip {
        cursor: grab;
        opacity: 0.5;
    }
</style>