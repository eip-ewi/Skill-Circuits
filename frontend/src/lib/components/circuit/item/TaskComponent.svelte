<script lang="ts">
    import moment from "moment";
    import type { TaskInfo, TaskItem } from "../../../dto/circuit/module/task";
    import { withCsrf } from "../../../logic/csrf";
    import { TaskIcons } from "../../../dto/task_icons";
    import {
        reportClickedLink,
        toggleTaskCompletion,
    } from "../../../logic/circuit/updates/task_updates";
    import { getBookmarks, isTaskInfoBookmarked } from "../../../logic/bookmarks.svelte";
    import {
        addTaskInfoToBookmarkList,
        removeTaskInfoFromBookmarkList,
    } from "../../../logic/updates/bookmark_updates";
    import Dropdown from "../../util/Dropdown.svelte";
    import BookmarkMenuComponent from "../../bookmark/BookmarkMenuComponent.svelte";
    import Button from "../../util/Button.svelte";
    import Link from "../../util/Link.svelte";
    import { canEditCircuit } from "../../../logic/authorisation.svelte";

    let {
        task,
        hideBookmark,
        hidePathCustomisation,
        reserveDeadlineSpace = false,
    }: {
        task: TaskInfo;
        hideBookmark?: boolean | undefined;
        hidePathCustomisation?: boolean;
        reserveDeadlineSpace?: boolean;
    } = $props();

    let draggable: boolean = $state(false);
    let bookmarksOpen: boolean = $state(false);

    async function toggleComplete() {
        await toggleTaskCompletion(task);
    }

    function dragStart(event: DragEvent) {
        if (task.taskType === "regular") {
            event.dataTransfer!.effectAllowed = "move";
            event.dataTransfer!.setData("skill-circuits/item", task.id.toString());
        }
    }

    function dragEnd(event: DragEvent) {}

    function isPastDeadline(deadline: string): boolean {
        return moment().isAfter(moment(deadline));
    }

    let overdueInViewer: boolean = $derived(
        !canEditCircuit() &&
            !task.completed &&
            task.deadline !== null &&
            isPastDeadline(task.deadline),
    );
</script>

<button
    class="checkbox"
    aria-label="Complete task"
    aria-pressed={task.completed}
    onclick={toggleComplete}>
    <span>{"\u2713"}</span>
</button>
<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div
    class="description"
    data-overdue={overdueInViewer}
    {draggable}
    ondragstart={dragStart}
    ondragend={dragEnd}>
    {#if !hidePathCustomisation && task.taskType === "regular"}
        <div
            role="button"
            tabindex="0"
            aria-label="Move task to skill"
            class="grip fa-solid fa-grip-vertical"
            onmouseenter={() => (draggable = true)}
            onmouseleave={() => setTimeout(() => (draggable = false), 200)}>
        </div>
    {/if}
    <span class="icon fa-solid fa-{TaskIcons[task.type]}"></span>
    {#if task.link === null}
        <span class="name">{task.name}</span>
    {:else}
        <Link onclick={() => reportClickedLink(task)} href={task.link} target="_blank">
            {task.name}
        </Link>
    {/if}
</div>
{#if hideBookmark !== true}
    <BookmarkMenuComponent
        bind:open={bookmarksOpen}
        onLists={getBookmarks().filter(list =>
            list.tasks.some(t => t.taskType === "regular" && t.infoId === task.infoId),
        )}
        addToList={list => addTaskInfoToBookmarkList(task, list)}
        removeFromList={list => removeTaskInfoFromBookmarkList(task, list)}>
        <Button square aria-label="Bookmark" onclick={() => (bookmarksOpen = true)}>
            <span
                class="fa-bookmark"
                class:fa-regular={!isTaskInfoBookmarked(task)}
                class:fa-solid={isTaskInfoBookmarked(task)}>
            </span>
        </Button>
    </BookmarkMenuComponent>
{/if}
<div class="task-info" data-overdue={overdueInViewer}>
    <span class="time">
        <span class="fa-solid fa-clock"></span>
        <span>{task.time}</span>
    </span>

    {#if task.deadline}
        <span class="deadline">
            <span class="fa-solid fa-calendar"></span>
            <span>{moment(task.deadline).format("DD/MM HH:mm")}</span>
        </span>
    {:else if reserveDeadlineSpace}
        <span class="deadline deadline-placeholder" aria-hidden="true">
            <span class="fa-solid fa-calendar"></span>
            <span>00/00 00:00</span>
        </span>
    {/if}
</div>

<style>
    .checkbox {
        aspect-ratio: 1 / 1;
        background: none;
        border: var(--checkbox-border);
        border-radius: var(--checkbox-border-radius);
        cursor: pointer;
        outline: none;
        position: relative;
        width: 1.5em;
    }
    .checkbox[aria-pressed="true"] {
        background-color: var(--checkbox-checked-colour);
        border: none;
    }

    .checkbox span {
        content: "\2713";
        color: var(--on-checkbox-checked-colour);
        left: 50%;
        position: absolute;
        transition: transform ease-in 150ms;
        translate: -50% -50%;
        transform-origin: center;
        top: 50%;
    }
    .checkbox[aria-pressed="false"] span {
        transform: scale(0);
    }

    .description {
        align-items: center;
        display: flex;
        gap: 0.375em;
    }

    .time {
        align-items: center;
        display: flex;
        gap: 0.25em;
    }

    .deadline {
        align-items: center;
        display: flex;
        font-variant-numeric: tabular-nums;
        gap: 0.5em;
        margin-right: 1em;
    }

    .deadline span:last-child {
        inline-size: 11ch;
        white-space: nowrap;
    }

    .deadline-placeholder {
        visibility: hidden;
    }

    .grip {
        cursor: grab;
        color: var(--drag-icon-color);
    }

    .task-info {
        display: flex;
        gap: 0.5em;
    }

    /*The global(a) is necessary for tasks that use links*/
    .description[data-overdue="true"] .name,
    .description[data-overdue="true"] :global(a),
    .task-info[data-overdue="true"] .time span:last-child,
    .task-info[data-overdue="true"] .deadline span:last-child {
        text-decoration: line-through;
    }
</style>
