<script lang="ts">

    import type {ChoiceTaskItem, TaskInfo, TaskItem} from "../../../dto/circuit/module/task";
    import {withCsrf} from "../../../logic/csrf";
    import {TaskIcons} from "../../../dto/task_icons";
    import {toggleTaskCompletion} from "../../../logic/circuit/updates/task_updates";
    import TaskComponent from "./TaskComponent.svelte";
    import {getBookmarks, isChoiceTaskBookmarked, isTaskInfoBookmarked} from "../../../logic/bookmarks.svelte";
    import {addChoiceTaskToBookmarkList, addTaskInfoToBookmarkList, removeChoiceTaskFromBookmarkList, removeTaskInfoFromBookmarkList} from "../../../logic/updates/bookmark_updates";
    import BookmarkMenuComponent from "../../bookmark/BookmarkMenuComponent.svelte";
    import Button from "../../util/Button.svelte";
    import {isTaskCompleted} from "../../../logic/circuit/skill_state/completion";

    let { task, hideBookmark, hidePathCustomisation }: { task: ChoiceTaskItem, hideBookmark?: boolean, hidePathCustomisation?: boolean } = $props();

    let bookmarksOpen: boolean = $state(false);
    let draggable: boolean = $state(false);

    function dragStart(event: DragEvent) {
        event.dataTransfer!.effectAllowed = "move";
        event.dataTransfer!.setData("skill-circuits/item", task.id.toString());
    }

    function dragEnd(event: DragEvent) {
    }
</script>

<div class="task" class:with-bookmark={!hideBookmark} data-completed={isTaskCompleted(task)}>
    <div class="heading">
        <div class="left">
            {#if hideBookmark !== true}
                <BookmarkMenuComponent bind:open={bookmarksOpen} onLists={getBookmarks().filter(list => list.tasks.some(t => t.taskType === "choice" && t.id === task.id))}
                                       addToList={ list => addChoiceTaskToBookmarkList(task, list) } removeFromList={ list => removeChoiceTaskFromBookmarkList(task, list) }>
                    <Button square aria-label="Bookmark" onclick={ () => bookmarksOpen = true }>
                        <span class="fa-bookmark" class:fa-regular={!isChoiceTaskBookmarked(task)} class:fa-solid={isChoiceTaskBookmarked(task)}></span>
                    </Button>
                </BookmarkMenuComponent>
            {/if}

            <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
            <div class="interrupt-border" draggable={draggable} ondragstart={dragStart} ondragend={dragEnd}>
                {#if !hidePathCustomisation}
                    <div role="button" tabindex="0" aria-label="Move task to skill"
                         class="grip fa-solid fa-grip-vertical"
                         onmouseenter={ () => draggable = true } onmouseleave={ () => setTimeout(() => draggable = false, 200) }></div>
                {/if}
                {#if task.name.length === 0}
                    <span>Do {task.minTasks} out of {task.tasks.length}</span>
                {:else}
                    <span>{task.name}</span>
                {/if}
            </div>
        </div>

        <div class="right interrupt-border">
            <span>{task.tasks.filter(task => task.completed).length}/{task.minTasks}</span>
        </div>
    </div>
    <div class="tasks" class:with-bookmark={!hideBookmark}>
        {#each task.tasks as subtask}
            <TaskComponent task={subtask} {hideBookmark} hidePathCustomisation={true}></TaskComponent>
        {/each}
    </div>
</div>

<style>
    .task {
        outline: 1px solid var(--choice-task-outline-colour);
        border-radius: var(--choice-task-outline-radius);
        grid-column: 1 / 4;
        margin-top: 1rem;
        margin-bottom: .5rem;
        padding: 1.25rem 1rem 1rem 1rem;
        position: relative;
        min-width: 21em;
    }
    .task.with-bookmark {
        grid-column: 1 / 5;
    }

    .task[data-completed="true"] {
        outline: 2px solid var(--choice-task-completed-outline-colour);
    }

    .heading {
        align-items: center;
        display: flex;
        justify-content: space-between;
        left: 0;
        padding-inline: 1rem;
        position: absolute;
        top: 0;
        transform: translateY(-50%);
        width: 100%;
    }

    .heading .interrupt-border {
        background-color: var(--background-colour);
        border-radius: .5em;
        padding-inline: .5rem;
    }

    .heading .left, .heading .right {
        align-items: center;
        display: flex;
        gap: .5rem;
    }

    .tasks {
        align-items: center;
        column-gap: 0.75rem;
        display: grid;
        grid-template-columns: auto 1fr auto;
        justify-content: start;
        row-gap: 0.25rem;
    }
    .with-bookmark {
        grid-template-columns: auto 1fr auto auto;
    }

    .grip {
        cursor: grab;
        margin-right: .25em;
        opacity: 0.5;
    }
</style>