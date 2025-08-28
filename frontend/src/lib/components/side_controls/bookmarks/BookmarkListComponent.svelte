<script lang="ts">

    import moment from "moment";
    import type {BookmarkList, BookmarkListSkill} from "../../../dto/bookmark";
    import TaskComponent from "../../circuit/item/TaskComponent.svelte";
    import {editBookmarkListName, loadBookmarkListCollapsed, removeBookmarkList, removeChoiceTaskFromBookmarkList, removeSkillFromBookmarkList, removeTaskInfoFromBookmarkList, toggleBookmarkListCollapse} from "../../../logic/updates/bookmark_updates";
    import {groupBookmarkListItems} from "../../../logic/bookmark_list.js";
    import ChoiceTaskComponent from "../../circuit/item/ChoiceTaskComponent.svelte";
    import Button from "../../util/Button.svelte";
    import {editCheckpointName} from "../../../logic/updates/checkpoint_updates";
    import BookmarkedSkillComponent from "./BookmarkedSkillComponent.svelte";
    import {cubicInOut} from "svelte/easing";
    import WithConfirmationDialog from "../../util/WithConfirmationDialog.svelte";

    let { list }: { list: BookmarkList } = $props();

    let collapsed: boolean = $state(true);

    $effect(() => {
        if (list.collapsed === undefined) {
            loadBookmarkListCollapsed(list);
        }
        collapsed = list.collapsed!;
    });

    async function editName(event: Event) {
        const newName = (event.target as HTMLInputElement).value;
        await editBookmarkListName(list, newName);
    }

    function stopEditing() {
        list.editing = false;
    }

    function expand(element: Element) {
        return {
            duration: 150,
            easing: cubicInOut,
            css: (t: number) => `
               transform: scaleY(${t});
            `
        }
    }
</script>

<div class="list">
    <div class="heading">
        {#if list.editing === true }
            <div class="edit">
                <input aria-label="Name" type="text" name="name" value={list.name} onchange={editName}/>
            </div>
        {:else}
            <div class="name">
                <h3>
                    {#if list.skill !== null}
                        <span>[Hidden skill]</span>
                    {/if}
                    <span>{list.name}</span>
                </h3>
                <button class="button" aria-label="Toggle bookmark list collapse" onclick={ () => toggleBookmarkListCollapse(list) }>
                    <span class="fa-solid" class:fa-chevron-down={collapsed} class:fa-chevron-up={!collapsed}></span>
                </button>
            </div>
        {/if}
        <div class="controls">
            {#if list.editing === true }
                <Button square aria-label="Stop editing" onclick={stopEditing}>
                    <span class="fa-solid fa-check"></span>
                </Button>
            {:else}
                {#if list.skill === null}
                    <Button square aria-label="Edit bookmark list" onclick={ () => list.editing = true }>
                        <span class="fa-solid fa-pencil"></span>
                    </Button>
                    <WithConfirmationDialog onconfirm={ () => removeBookmarkList(list) } icon="fa-solid fa-trash" action="Delete">
                        {#snippet button(showDialog: () => void) }
                            <Button square type="caution" aria-label="Delete bookmark list" onclick={showDialog}>
                                <span class="fa-solid fa-trash"></span>
                            </Button>
                        {/snippet}
                        <p>
                            Are you sure you want to delete '{list.name}'?
                        </p>
                    </WithConfirmationDialog>
                {/if}
            {/if}
        </div>
    </div>
    {#if !collapsed}
        <div class="content" transition:expand>
            {#if list.skills.length === 0 && list.tasks.length === 0}
                <p>There are no bookmarks on this list.</p>
            {/if}
            {#if list.skills.length > 0}
                <div class="skills">
                    {#each groupBookmarkListItems(list.skills) as group}
                        <span class="group-name">{group.title}</span>
                        {#each group.items as skill}
                            <div>
                                <BookmarkedSkillComponent {skill}></BookmarkedSkillComponent>
                            </div>
                            <div class="controls">
                                <Button square type="caution" aria-label="Remove from {list.name}" onclick={ () => removeSkillFromBookmarkList(skill, list) }>
                                    <span class="fa-solid fa-trash"></span>
                                </Button>
                            </div>
                        {/each}
                    {/each}
                </div>
            {/if}
            {#if list.tasks.length > 0}
                <div class="tasks">
                    {#each groupBookmarkListItems(list.tasks) as group}
                        <span class="group-name">{group.title}</span>
                        {#each group.items as task}
                            {#if task.taskType === "regular"}
                                <TaskComponent {task} hideBookmark={true} hidePathCustomisation={true}></TaskComponent>
                                <div class="controls">
                                    <Button square type="caution" aria-label="Remove from {list.name}" onclick={ () => removeTaskInfoFromBookmarkList(task, list) }>
                                        <span class="fa-solid fa-trash"></span>
                                    </Button>
                                </div>
                            {/if}
                            {#if task.taskType === "choice"}
                                <ChoiceTaskComponent {task} hideBookmark={true}></ChoiceTaskComponent>
                                <div class="controls">
                                    <Button square type="caution" aria-label="Remove from {list.name}" onclick={ () => removeChoiceTaskFromBookmarkList(task, list) }>
                                        <span class="fa-solid fa-trash"></span>
                                    </Button>
                                </div>
                            {/if}
                        {/each}
                    {/each}
                </div>
            {/if}
        </div>
    {/if}
</div>

<style>
    .list {
        display: grid;
        gap: 1rem;
    }

    .list .heading {
        align-items: center;
        display: flex;
        gap: 1rem;
        justify-content: space-between;
    }

    .list .heading > :first-child {
        flex-grow: 1;
    }

    .list .name {
        align-items: center;
        display: flex;
        gap: .5em;
    }

    .list h3 {
        font-size: var(--font-size-400);
        font-weight: 500;
    }

    .controls {
        display: flex;
        gap: 0.5em;
    }

    .list .content {
        display: grid;
        gap: 1rem;
        transform-origin: top;
    }

    .skills {
        align-items: center;
        column-gap: 0.75em;
        display: grid;
        grid-template-columns: 1fr auto;
        row-gap: 0.25em;
    }

    .tasks {
        align-items: center;
        column-gap: 0.75em;
        display: grid;
        grid-template-columns: auto 1fr auto auto;
        justify-content: start;
        row-gap: 0.25em;
    }

    .group-name {
        font-size: var(--font-size-100);
        grid-column: 1 / -1;
        margin-top: .5em;
        opacity: .5;
    }

    .tasks .controls {
        justify-content: flex-end;
    }

    input {
        border: none;
        border-radius: .5em;
        padding: .5em 1em;
    }

    .button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: .5em;
        color: var(--on-glass-colour);
        cursor: pointer;
        display: grid;
        justify-items: center;
        padding: 0.5em;
        text-decoration: none;
    }
    .button:focus-visible, .button:hover {
        background: var(--on-glass-surface-active-colour);
    }
</style>
