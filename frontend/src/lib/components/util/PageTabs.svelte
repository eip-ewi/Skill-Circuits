<script lang="ts">

    import type {Snippet} from "svelte";

    let { children }: { children: Snippet } = $props();

</script>

<div class="tabs-wrapper">
    <nav class="glass">
        {@render children() }
    </nav>
</div>

<style>
    .tabs-wrapper {
        display: grid;
        place-items: center;
    }

    nav {
        border-radius: 100vw;
        display: flex;
        padding: 1em;
        gap: 1rem;
    }
</style>