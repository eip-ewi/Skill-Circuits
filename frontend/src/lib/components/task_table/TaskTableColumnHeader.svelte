<script lang="ts">
    import Button from "../util/Button.svelte";

    let { name, sortStatus = $bindable(undefined) } : { name: string, sortStatus: -1 | 0 | 1 | undefined } = $props();
</script>

<th>
    <div class="cell-wrapper">
        {name}
        {#if sortStatus === -1}
            <Button aria-label="Sort ascendingly by {name}" onclick={() => {sortStatus = 1}} square={true} style="margin-left: 1em; font-size: var(--font-size-100)">
                <i class="fa-solid fa-caret-down"></i>
            </Button>
        {:else if sortStatus === 0}
            <Button aria-label="Sort by {name}" onclick={() => {sortStatus = -1}} square={true} style="margin-left: 1em; font-size: var(--font-size-100)">
                <i class="fa-solid fa-sort"></i>
            </Button>
        {:else if sortStatus === 1}
            <Button aria-label="Sort descendingly by {name}" onclick={() => {sortStatus = -1}} square={true} style="margin-left: 1em; font-size: var(--font-size-100)">
                <i class="fa-solid fa-caret-up"></i>
            </Button>
        {/if}
    </div>
</th>

<style>
    th {
        padding: 0.2em 1em;
        border-bottom: 2px solid var(--on-group-colour);
    }

    th:not(:first-child) {
        border-left: 2px solid var(--on-group-colour);
    }

    .cell-wrapper {
        display: flex;
        justify-content: space-between;
    }
</style>