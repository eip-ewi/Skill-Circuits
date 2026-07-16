<script lang="ts">
    import type { ChoiceTaskItem, TaskInfo, TaskItem } from "../../../dto/circuit/module/task";
    import { withCsrf } from "../../../logic/csrf";
    import { TaskIcons } from "../../../dto/task_icons";
    import { toggleTaskCompletion } from "../../../logic/circuit/updates/task_updates";
    import TaskComponent from "./TaskComponent.svelte";
    import {
        getBookmarks,
        isChoiceTaskBookmarked,
        isTaskInfoBookmarked,
    } from "../../../logic/bookmarks.svelte";
    import {
        addChoiceTaskToBookmarkList,
        addTaskInfoToBookmarkList,
        removeChoiceTaskFromBookmarkList,
        removeTaskInfoFromBookmarkList,
    } from "../../../logic/updates/bookmark_updates";
    import BookmarkMenuComponent from "../../bookmark/BookmarkMenuComponent.svelte";
    import Button from "../../util/Button.svelte";
    import { isTaskCompleted } from "../../../logic/circuit/skill_state/completion";

    let {
        task,
        hideBookmark,
        hidePathCustomisation,
        reserveDeadlineSpace = false,
    }: {
        task: ChoiceTaskItem;
        hideBookmark?: boolean | undefined;
        hidePathCustomisation?: boolean | undefined;
        reserveDeadlineSpace?: boolean | undefined;
    } = $props();

    let bookmarksOpen: boolean = $state(false);
    let draggable: boolean = $state(false);

    function dragStart(event: DragEvent) {
        event.dataTransfer!.effectAllowed = "move";
        event.dataTransfer!.setData("skill-circuits/item", task.id.toString());
    }

    function dragEnd(event: DragEvent) {}
</script>

<div class="task" class:with-bookmark={!hideBookmark} data-completed={isTaskCompleted(task)}>
    <div class="heading">
        <div class="left">
            {#if hideBookmark !== true}
                <BookmarkMenuComponent
                    bind:open={bookmarksOpen}
                    onLists={getBookmarks().filter(list =>
                        list.tasks.some(t => t.taskType === "choice" && t.id === task.id),
                    )}
                    addToList={list => addChoiceTaskToBookmarkList(task, list)}
                    removeFromList={list => removeChoiceTaskFromBookmarkList(task, list)}>
                    <Button square aria-label="Bookmark" onclick={() => (bookmarksOpen = true)}>
                        <span
                            class="fa-bookmark"
                            class:fa-regular={!isChoiceTaskBookmarked(task)}
                            class:fa-solid={isChoiceTaskBookmarked(task)}>
                        </span>
                    </Button>
                </BookmarkMenuComponent>
            {/if}

            <div
                role="group"
                class="interrupt-border"
                {draggable}
                ondragstart={dragStart}
                ondragend={dragEnd}>
                {#if !hidePathCustomisation}
                    <div
                        role="button"
                        tabindex="0"
                        aria-label="Move task to skill"
                        class="grip fa-solid fa-grip-vertical"
                        onmouseenter={() => (draggable = true)}
                        onmouseleave={() => setTimeout(() => (draggable = false), 200)}>
                    </div>
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
        {#each task.tasks as subtask (subtask.infoId)}
            <TaskComponent
                task={subtask}
                {hideBookmark}
                hidePathCustomisation={true}
                {reserveDeadlineSpace}></TaskComponent>
        {/each}
    </div>
</div>

<style>
    .task::before {
        content: "";
        position: absolute;
        inset: 0 -0.5rem;
        border-radius: var(--choice-task-outline-radius);
        outline: 1px solid var(--choice-task-outline-colour);
        pointer-events: none;
    }

    .task {
        align-items: center;
        display: grid;
        grid-template-columns: subgrid;
        row-gap: 0.25rem;
        grid-column: 1 / 4;
        margin-top: 1rem;
        margin-bottom: 0.5rem;
        padding-block: 1.25rem 1rem;
        position: relative;
        min-width: 21em;
    }
    .task.with-bookmark {
        grid-column: 1 / 5;
    }

    .task[data-completed="true"]::before {
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
        border-radius: 0.5em;
        padding-inline: 0.5rem;
    }

    .heading .left,
    .heading .right {
        align-items: center;
        display: flex;
        gap: 0.5rem;
    }

    .tasks {
        display: contents;
    }

    .grip {
        cursor: grab;
        margin-right: 0.25em;
        color: var(--drag-icon-color);
    }
</style>
