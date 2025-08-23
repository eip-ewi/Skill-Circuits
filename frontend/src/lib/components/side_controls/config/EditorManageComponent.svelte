<script lang="ts">

    import {getEdition} from "../../../logic/edition/edition.svelte";
    import type {Person} from "../../../dto/person";
    import {addEditor, removeEditor} from "../../../logic/updates/config_updates";
    import Button from "../../util/Button.svelte";

    let query: string = $state("");
    let searchTimeout: number | null = null;
    let searchResults: Person[] = $state([]);

    function search() {
        if (searchTimeout !== null) {
            clearTimeout(searchTimeout);
        }
        searchTimeout = setTimeout(async () => {
            let response = await fetch(`/api/person/search?query=${query}`);
            searchResults = await response.json();
            searchTimeout = null;
        }, 200);
    }

    async function add(editor: Person) {
        query = "";
        searchResults = [];
        await addEditor(editor);
    }

</script>

<div>
    <h3>Editors</h3>

    <ul class="editors">
        {#each getEdition().teachers as teacher}
            <li>
                {teacher.displayName} (Teacher)
            </li>
        {/each}
        {#each getEdition().editors as editor}
            <li>
                <span>{editor.displayName}</span>
                <Button square type="caution" aria-label="Remove {editor.displayName} as editor" onclick={ () => removeEditor(editor) }>
                    <span class="fa-solid fa-trash"></span>
                </Button>
            </li>
        {/each}
    </ul>

    <div class="add-editor" aria-expanded={query !== "" && searchResults.length > 0}>
        <label for="add-editor-search">Add an editor</label>
        <input bind:value={query} placeholder="Search..." onkeydown={search}/>
        <ul class="scrollable glass results">
            {#each searchResults as result}
                {#if !getEdition().teachers.some(editor => editor.id === result.id) && !getEdition().editors.some(editor => editor.id === result.id)}
                    <li><button aria-label="Add {result.displayName} as editor" onclick={ () => add(result) }>{result.displayName}</button></li>
                {/if}
            {/each}
        </ul>
    </div>

</div>

<style>
    h3 {
        font-size: var(--font-size-400);
        font-weight: 500;
        margin-bottom: .5rem;
    }

    .editors {
        display: grid;
        gap: 0.25rem;
        list-style: none;
        margin-bottom: .75rem;
    }

    .editors li {
        align-items: center;
        display: flex;
        gap: 1rem;
    }

    .editors li :first-child {
        flex-grow: 1;
    }

    .add-editor {
        display: grid;
        gap: 0;
        position: relative;
    }

    .add-editor label {
        font-size: var(--font-size-200);
        font-weight: 500;
    }

    .add-editor input {
        border: none;
        border-radius: 8px;
        padding: .5rem 1rem;
    }

    .add-editor .results {
        border-radius: 8px;
        display: grid;
        gap: .5rem;
        min-width: 100%;
        max-height: 24rem;
        overflow-y: auto;
        padding: .5rem .5rem;
        position: absolute;
        overscroll-behavior: contain;
        top: 100%;
        transform-origin: top;
        transition: transform 150ms ease-in-out;
        z-index: 999;
    }
    .add-editor:where(:not(:focus-within), [aria-expanded="false"]) .results {
        transform: scaleY(0);
    }

    .add-editor .results li {
        display: grid;
    }

    .add-editor .results button {
        align-items: center;
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: 8px;
        cursor: pointer;
        display: grid;
        justify-content: start;
        padding: 0.5rem 1rem;
        gap: .5rem;
        white-space: nowrap;
    }
    .add-editor .results button:where(:hover, :focus-visible) {
        background: var(--on-glass-surface-active-colour);
    }
</style>
