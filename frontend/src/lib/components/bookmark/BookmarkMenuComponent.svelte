<script lang="ts">
    import {getBookmarks, isTaskInfoBookmarked} from "../../logic/bookmarks.svelte";
    import Dropdown from "../util/Dropdown.svelte";
    import type {Snippet} from "svelte";
    import type {BookmarkList} from "../../dto/bookmark";

    let { open = $bindable(), onLists, children, addToList, removeFromList }:
        { open: boolean, onLists: BookmarkList[], children: Snippet, addToList?: (list: BookmarkList) => void, removeFromList?: (list: BookmarkList) => void } = $props();
</script>

<Dropdown bind:open={open}>
    {@render children() }

    {#snippet dropdown() }
        <div class="lists">
            {#each getBookmarks() as list}
                {@const included = onLists.some(l => l.id === list.id)}
                <button class="bookmark" onclick={ () => included ? removeFromList?.(list) : addToList?.(list) }>
                    <span class="fa-bookmark" class:fa-regular={!included} class:fa-solid={included}></span>
                    <span>{list.name}</span>
                </button>
            {/each}
        </div>
    {/snippet}
</Dropdown>

<style>
    .lists {
        display: grid;
        gap: .5em;
    }

    .lists .bookmark {
        align-items: center;
        background: none;
        border: none;
        border-radius: var(--option-border-radius);
        color: var(--on-glass-colour);
        cursor: pointer;
        display: flex;
        padding: 0.5em 1em;
        gap: .5em;
        white-space: nowrap;
    }
    .lists .bookmark:where(:hover, :focus-visible) {
        background-color: var(--option-active-colour);
        color: var(--on-option-active-colour);
    }
</style>