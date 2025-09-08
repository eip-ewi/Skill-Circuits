<script lang="ts">

    import type {Snippet} from "svelte";
    import {getPage, loadPage} from "../../logic/routing.svelte";

    let { page, children }: { page: string, children: Snippet } = $props();

</script>

<button role="tab" aria-selected={ getPage() === page } onclick={ () => loadPage(page) }>
    {@render children() }
</button>

<style>
    button {
        border: none;
        background: none;
        border-radius: 100vw;
        cursor: pointer;
        padding: .5em 1em;
    }

    button:where([aria-selected="true"], :hover, :focus-visible) {
        background-color: var(--on-glass-surface-active-colour);
    }
</style>