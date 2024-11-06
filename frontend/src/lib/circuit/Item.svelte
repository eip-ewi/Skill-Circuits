<script lang="ts">
    import type {ItemData} from "../data/item";
    import type {CircuitUpdates} from "../model/circuit_updates";
    import {onMount} from "svelte";
    import type {ItemPatch} from "../data/item_patch";

    let { updates, item: data, editing, patch }: { updates: CircuitUpdates, item: ItemData, editing: boolean, patch: ItemPatch } = $props();

    let item: ItemData = $state(data);

    onMount(() => {
        updates.subscribe("itemCompletion", completion => {
            item = data;
        });
        updates.subscribe("editBlock", completion => {
            item = data;
        });
    });

    function changeName(e: Event) {
        patch.name = (e.target as HTMLInputElement).value;
    }
    function changeTime(e: Event) {
        patch.time = parseInt((e.target as HTMLInputElement).value);
    }
</script>

{#if editing}
    <div class="item" data-editing="true">
        <span class="item__icon fa-solid fa-{item.icon}"></span>
        <input type="number" class="time_edit" value="{item.time}" onchange="{changeTime}"/>
        <input type="text" value="{item.name}" onchange="{changeName}"/>
    </div>
{:else}
    <span class="item fa-solid fa-{item.icon}" data-completed="{item.completed}" data-editing="false"></span>
{/if}

<style>
    .item {
        display: flex;
        align-items: center;
    }

    .item[data-editing="false"]{
        font-size: 1.25rem;
    }

    .item[data-completed="false"] {
        color: var(--on-block-faded-colour);
    }

    .time_edit {
        max-width: 3rem;
    }
</style>
