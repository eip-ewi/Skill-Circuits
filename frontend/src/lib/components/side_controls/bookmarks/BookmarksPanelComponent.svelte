<script lang="ts">

    import {fetchBookmarks, getBookmarks} from "../../../logic/bookmarks.svelte";
    import {onMount} from "svelte";
    import {addBookmarkList} from "../../../logic/updates/bookmark_updates";
    import BookmarkListComponent from "./BookmarkListComponent.svelte";

    let { open = $bindable() }: { open: boolean } = $props();

    onMount(() => {
        fetchBookmarks().then(() => {});
    })
</script>

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div class="scrollable glass panel" aria-expanded={open} ondragleave={ () => open = false }>
    <div class="heading">
        <h2>Bookmarks</h2>
        <div class="controls">
            <button class="button" aria-label="Add bookmark list" onclick={addBookmarkList}>
                <span class="fa-solid fa-plus"></span>
            </button>
            <button class="button" aria-label="Close panel" onclick={ () => open = false }>
                <span class="fa-solid fa-arrow-right"></span>
            </button>
        </div>
    </div>
    <div class="lists">
        {#each getBookmarks() as list}
            <BookmarkListComponent {list}></BookmarkListComponent>
        {/each}
    </div>
</div>

<style>
    .panel {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
        inset-block: 2em;
        max-width: 32em;
        overflow-y: auto;
        overscroll-behavior: contain;
        position: fixed;
        right: 0;
        top: 2em;
        transform-origin: right;
        transition: transform ease-in-out 150ms;
        z-index: 91;
    }

    .panel[aria-expanded="false"] {
        transform: scaleX(0);
    }
    .panel[aria-expanded="true"] {
        transition-delay: 150ms;
    }

    .heading {
        align-items: center;
        display: flex;
        justify-content: space-between;
        gap: 2rem;
        padding: 2em 2em 1em 2em;
    }

    .heading h2 {
        font-size: var(--font-size-500);
        font-weight: 700;
    }

    .controls {
        display: flex;
        gap: .25em;
    }

    .lists {
        display: grid;
        gap: 2rem;
        padding: 0 2rem 2rem 2rem;
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
