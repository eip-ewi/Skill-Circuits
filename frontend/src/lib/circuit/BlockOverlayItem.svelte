<script lang="ts">
    import type {ItemData} from "../data/item";
    import type {Level} from "../data/level";
    import {type CircuitUpdates, ItemCompletion} from "../model/circuit_updates";
    import {getAuth} from "../data/auth.svelte";

    let { updates, level, item: data }: { updates: CircuitUpdates, level: Level, item: ItemData } = $props();

    let item: ItemData = $state(data);

    function complete() {
        updates.update(new ItemCompletion(data, level));
        item = data;
    }

</script>

<li class="item">
    <!-- svelte-ignore a11y_consider_explicit_label -->
    <button type="button" aria-pressed="{item.completed}" onclick={complete}></button>
    <span class="item__icon fa-solid fa-{item.icon}"></span>
    <div>
        <span class="fa-solid fa-clock"></span>
        <span>{item.time}</span>
    </div>
    <span class="item__name">{item.name}</span>
</li>

<style>
    .item {
        align-items: center;
        display: grid;
        grid-column: span 4;
        grid-template-columns: subgrid;
        gap: .5rem;
        font-size: 1.25rem;
    }
    .item button {
        aspect-ratio: 1 / 1;
        background: white;
        border: 2px solid var(--on-block-divider-colour);
        border-radius: 4px;
        cursor: pointer;
        position: relative;
        width: 1.5rem;
    }
    .item button::after {
        bottom: -0.05rem;
        content: '\2713';
        color: var(--on-checkbox-checked-colour);
        position: absolute;
        left: .25rem;
        transform: scale(0);
        transition: transform ease-in 150ms;
        transform-origin: center;
    }
    .item button[aria-pressed="true"] {
        background-color: var(--checkbox-checked-colour);
        border: none;
    }
    .item button[aria-pressed="true"]::after {
        transform: scale(1);
    }
    .item__icon {
        place-self: center;
    }
</style>
