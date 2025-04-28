<script lang="ts">

    import type {BlockData} from "../data/block";
    import type {Level} from "../data/level";
    import BlockOverlay from "./BlockOverlay.svelte";
    import {withCsrf} from "../util/csrf";
    import type {CircuitData} from "../data/circuit";
    import {BlockCompletion, type CircuitUpdates, EditBlock, HighlightBlock} from "../model/circuit_updates";
    import {onMount} from "svelte";
    import Item from "./Item.svelte";
    import type {BlockModel} from "../model/block";
    import {getAuth} from "../data/auth.svelte";
    import type {BlockPatch} from "../data/block_patch";
    import type {ItemPatch} from "../data/item_patch";

    let { updates, block: data, level }: { updates: CircuitUpdates, block: BlockModel, level: Level } = $props();

    let block: BlockModel = $state(data);
    let completed: boolean = $state(data.completed);
    let locked: boolean = $state(data.locked);
    let editing: boolean = $state(false);

    let element: HTMLElement;
    let overlay: BlockOverlay;

    let dragging: boolean = $state(false);

    let itemPatches: ItemPatch[] = $derived(block.items.map(item => { return { id: item.id, name: item.name, time: item.time }; }));

    onMount(() => {
        updates.subscribe("blockColumnChange", columnChange => {
            block = data;
        });
        updates.subscribe("itemCompletion", completion => {
            if (data.items.some(item => item.id === completion.item.id)) {
                let completedItems = data.items.filter(item => item.completed).length;
                if (data.completed && completion.completed) {
                    updates.update(new BlockCompletion(data, true));
                } else if (completedItems == data.items.length - 1 && !completion.completed) {
                    updates.update(new BlockCompletion(data, false));
                }
                block = data;
                completed = data.completed;
                locked = data.locked;
            }
        });
        updates.subscribe("blockCompletion", completion => {
            if (data.parents.some(pId => pId === completion.block.id)) {
                if (data.locked) {
                    block = data;
                    locked = data.locked;
                } else {
                    setTimeout(() => { block = data; locked = data.locked }, 2000);
                }
            }
        });
    });

    function dragStart(event: DragEvent) {
        if (!getAuth().canEditBlocks) return;
        // chrome security does not allow getData during dragOver, this is a workaround
        event.dataTransfer!.setData(`id-${element.dataset["id"]!}`, "");
        // dropEffect is broken in chrome, this is a workaround
        event.dataTransfer!.setData("move", "");
        event.dataTransfer!.setData("text/plain", element.dataset["id"]!);
        event.dataTransfer!.effectAllowed = "move";
        dragging = true;
    }

    function dragEnd() {
        if (!getAuth().canEditBlocks) return;
        dragging = false;
    }

    function mouseEnter() {
        if (editing) return;
        updates.update(new HighlightBlock(data, true));
    }

    function mouseLeave() {
        if (editing) return;
        updates.update(new HighlightBlock(data, false));
    }

    function openOverlay() {
        if (editing) return;
        overlay.open(element);
    }

    function saveEdit() {
        updates.update(new EditBlock(data, {
            id: block.id,
            items: itemPatches,
        }, level))
        editing = false;
    }

</script>

<div style:grid-column="{block.column+1}" style:grid-row="{(block.row || 0)+1}" class="block__wrapper">
<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div bind:this={element} data-id="{block.id}" class="block" draggable="{getAuth().canEditBlocks}"  data-locked="{locked && !getAuth().canEditBlocks}" data-completed="{completed}"
     data-dragging="{dragging}" onclick={openOverlay} ondragstart={dragStart} ondragend={dragEnd} onmouseenter={mouseEnter} onmouseleave={mouseLeave} data-editing="{editing}">
    <span class="block__title">{block.name}</span>
    <ul class="block__items" data-editing="{editing}">
        {#each block.items as item, index}
            <Item {updates} {item} {editing} patch={itemPatches[index]!}/>
        {/each}
    </ul>
</div>
{#if getAuth().canEditBlocks}
    <div class="block__controls">
        {#if editing}
            <button aria-label="Save skill" onclick="{saveEdit}">
                <span class="fa-solid fa-save"></span>
            </button>
            <button aria-label="Cancel editing" onclick="{() => { editing = false; }}">
                <span class="fa-solid fa-cancel"></span>
            </button>
        {:else}
            <button aria-label="Edit skill" onclick="{() => { editing = true; }}">
                <span class="fa-solid fa-pencil"></span>
            </button>
            <button aria-label="Delete skill">
                <span class="fa-solid fa-trash-alt"></span>
            </button>
        {/if}
    </div>
{/if}
</div>

<BlockOverlay bind:this={overlay} {updates} {block} {level}/>

<style>
    .block {
        background-color: var(--block-colour);
        border-radius: 1rem;
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        display: flex;
        flex-direction: column;
        gap: .5rem;
        padding: 1rem;
        transition: filter ease-in-out 150ms, transform ease-in-out 150ms, box-shadow ease-in-out 150ms;
    }
    .block[data-editing="false"] {
        cursor: pointer;
    }
    .block:hover[data-editing="false"] {
        transform: scale(1.05);
        box-shadow: .75rem 1.25rem 1.8rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
    }
    .block[draggable="true"][data-editing="false"] {
        cursor: grab;
    }
    .block[data-dragging="true"] {
        opacity: 60%;
    }
    .block[data-locked="true"] {
        filter: blur(.375rem);
        &:hover {
            filter: none;
        }
    }
    .block[data-completed="true"] {
        background-color: var(--completed-block-colour);
    }

    .block__title {
        font-size: 1.25rem;
        font-weight: 700;
    }

    .block__items {
        color: var(--on-block-colour);
        display: flex;
        gap: .5rem;
        flex-wrap: wrap;
    }

    .block__items[data-editing="true"] {
        flex-direction: column;
        flex-wrap: initial;
    }

    .block__wrapper {
        position: relative;
        z-index: 3;
    }

    .block__controls {
        display: flex;
        flex-direction: column;
        position: absolute;
        top: 0;
        right: -2.5rem;
        gap: .5rem;
    }

    .block__controls > * {
        aspect-ratio: 1/1;
        background-color: var(--block-colour);
        border: none;
        border-radius: .5rem;
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        cursor: pointer;
        display: grid;
        place-items: center;
        padding: .5rem;
    }
    .block__controls > *:where(:hover, :focus-visible) {
        transform: scale(1.05);
        box-shadow: .75rem 1.25rem 1.8rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
    }
</style>