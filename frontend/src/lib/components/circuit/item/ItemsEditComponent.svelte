<script lang="ts">

    import ItemEditComponent from "./ItemEditComponent.svelte";
    import type {Item} from "../../../dto/circuit/item";
    import {getBlock, getBlockForItem, getItem} from "../../../logic/circuit/circuit.svelte";
    import {updateBlockPosition} from "../../../logic/circuit/updates/position_updates.svelte";
    import {createBlock} from "../../../logic/circuit/updates/block_updates";
    import {getLevel} from "../../../logic/circuit/level.svelte";
    import {ModuleLevel} from "../../../data/level";
    import {editTaskIndex, moveTask} from "../../../logic/circuit/updates/task_updates";
    import type {TaskItem} from "../../../dto/circuit/module/task";
    import type {Block} from "../../../dto/circuit/block";
    import type {SkillBlock} from "../../../dto/circuit/module/skill";
    import TaskEditComponent from "./TaskEditComponent.svelte";

    let { block }: { block: Block } = $props();

    let element: HTMLElement;

    let droppingIndex: number | undefined = $state();

    function newIndexFromPosition(event: DragEvent): number {
        let closestItem = (event.target as HTMLElement).closest(".item-wrapper");
        let location: "pre" | "post";
        if (closestItem === null) {
            closestItem = Array.from(element.querySelectorAll(".item-wrapper")).find(item => item.getBoundingClientRect().y > event.clientY)!;
            location = "pre";
        } else {
            location = event.clientY - closestItem.getBoundingClientRect().height / 2 < closestItem.getBoundingClientRect().y ? "pre" : "post";
        }

        let itemIndex = parseInt((closestItem as HTMLElement).dataset["index"]!);
        return location === "pre" ? itemIndex : itemIndex + 1;
    }

    function dragEnter(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item") || block.blockType !== "skill") {
            return;
        }
        event.preventDefault();
    }

    function dragLeave() {
        droppingIndex = undefined;
    }

    function dragOver(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item") || block.blockType !== "skill") {
            return;
        }
        event.preventDefault();

        droppingIndex = newIndexFromPosition(event);
    }

    async function drop(event: DragEvent) {
        if (!event.dataTransfer!.types.includes("skill-circuits/item") || block.blockType !== "skill") {
            return;
        }
        event.preventDefault();

        let itemId = parseInt(event.dataTransfer!.getData("skill-circuits/item"));
        let item = getItem(itemId) as TaskItem;

        let newIndex = newIndexFromPosition(event);

        let fromBlock = getBlockForItem(item);

        if (fromBlock.id === block.id) {

            let currentIndex = block.items.findIndex(i => i.id === item.id)!;
            if (newIndex > currentIndex) {
                newIndex--;
            }

            await editTaskIndex(item, newIndex, block.items);

        } else {

            await moveTask(item, block, newIndex, fromBlock as SkillBlock);

        }

        droppingIndex = undefined;

    }
</script>


<!-- svelte-ignore a11y_no_static_element_interactions -->
<div bind:this={element} class="items"
     ondragenter={dragEnter} ondragover={dragOver} ondragleave={dragLeave} ondrop={drop}
>
    {#each block.items as item, index}
        <div class="item-wrapper" data-index={index} data-dropping={droppingIndex === index ? "pre" : droppingIndex === index + 1 ? "post" : undefined}>
            <div class="pre drop-indicator"></div>
            {#if item.itemType === "task"}
                <TaskEditComponent task={item}></TaskEditComponent>
            {:else}
                <ItemEditComponent {item}></ItemEditComponent>
            {/if}
            <div class="post drop-indicator"></div>
        </div>
    {/each}
</div>

<style>
    .items {
        display: grid;
        gap: 0.5em;
    }

    .item-wrapper {
        position: relative;
    }

    .drop-indicator {
        background: var(--task-drop-indication-colour);
        border: var(--task-drop-indication-border);
        display: none;
        height: calc(.5em + .25em);
        left: -.25em;
        position: absolute;
        pointer-events: none;
        width: calc(100% + 0.5em);
        z-index: 1;
    }

    .pre.drop-indicator {
        border-radius: 0 0 var(--task-drop-indication-border-radius) var(--task-drop-indication-border-radius);
        border-top: none;
        top: -.25em;
    }
    .item-wrapper:first-child .pre.drop-indicator {
        border: var(--task-drop-indication-border);
        border-radius: var(--task-drop-indication-border-radius);
        height: 1.5em;
        top: -1em;
    }
    .item-wrapper[data-dropping="pre"] .pre.drop-indicator {
        display: initial;
    }

    .post.drop-indicator {
        border-radius: var(--task-drop-indication-border-radius) var(--task-drop-indication-border-radius) 0 0;
        border-bottom: none;
        bottom: -.25em;
    }
    .item-wrapper:last-child .post.drop-indicator {
        border: var(--task-drop-indication-border);
        border-radius: var(--task-drop-indication-border-radius);
        bottom: -1em;
        height: 1.5em;
    }
    .item-wrapper[data-dropping="post"] .post.drop-indicator {
        display: initial;
    }
</style>