<script lang="ts">

    import type {Block} from "../../../dto/circuit/block";
    import type {SkillBlock} from "../../../dto/circuit/module/skill";
    import {onDestroy, onMount, tick} from "svelte";
    import type {Graph} from "../../../logic/circuit/graph";
    import type {TaskItem} from "../../../dto/circuit/module/task";
    import {isUnlocked} from "../../../logic/circuit/skill_state/unlock";
    import {canEditCircuit, getAuthorisation} from "../../../logic/authorisation.svelte";
    import {getLevel, isLevel} from "../../../logic/circuit/level.svelte";
    import {isCompleted} from "../../../logic/circuit/skill_state/completion";
    import {loadPage} from "../../../logic/routing.svelte";
    import {EditionLevel, ModuleLevel} from "../../../data/level";
    import TaskIconComponent from "../item/TaskIconComponent.svelte";
    import TaskIconsComponent from "../item/TaskIconsComponent.svelte";
    import BlockActionIndicationComponent from "./BlockActionIndicationComponent.svelte";
    import ExpandedViewOpenButtonComponent from "./ExpandedViewOpenButtonComponent.svelte";
    import ExpandedBlockComponent from "./ExpandedBlockComponent.svelte";
    import {getBlock, getBlocks, getCircuit, getGraph} from "../../../logic/circuit/circuit.svelte";
    import BlockControlsComponent from "./BlockControlsComponent.svelte";
    import {type BlockAction, BlockActions} from "../../../data/block_action";
    import {type BlockState, BlockStates} from "../../../data/block_state";
    import BlockCreateConnectionsComponent from "./BlockManageConnectionsComponent.svelte";
    import BlockManageConnectionsComponent from "./BlockManageConnectionsComponent.svelte";
    import BlockEditComponent from "./BlockEditComponent.svelte";
    import BlockContentComponent from "./BlockContentComponent.svelte";
    import {updateBlockPosition} from "../../../logic/circuit/updates/position_updates.svelte";
    import BlockAssignPathsComponent from "./BlockAssignPathsComponent.svelte";
    import {disableColumns, enableColumns} from "../../../dto/columns.svelte";
    import BookmarkSkillButtonComponent from "./BookmarkSkillButtonComponent.svelte";
    import {isSkillBookmarked} from "../../../logic/bookmarks.svelte";

    let { block }: { block: Block } = $props();

    let locked: boolean = $derived(!canEditCircuit() && !isUnlocked(block));
    let completed: boolean = $derived(!canEditCircuit() && isCompleted(block));
    let clickable: boolean = $derived((!canEditCircuit() || getLevel() !== ModuleLevel) && (block.state !== BlockStates.Editing && block.state !== BlockStates.AssigningPaths));
    let hidden: boolean = $derived(block.state === BlockStates.Inactive && canEditCircuit() && block.blockType === "skill" && !block.external && block.hidden)

    let draggable: boolean = $state(false);
    let action: BlockAction | undefined = $state();

    let connectable:boolean = $state(false);
    let unfocused: boolean = $derived(block.state === BlockStates.WaitingForConnection && !connectable);

    let expanded: boolean = $state(false);

    let element: HTMLElement;

    let pulsingBlockElement: HTMLElement | null = $state(null);
    let scrollToPulsingHovered: boolean = $state(false);

    function onButtonMouseEnter() {
        scrollToPulsingHovered = true;
    }

    function onButtonMouseLeave() {
        scrollToPulsingHovered = false;
    }

    $effect(() => {
        if (block.state === BlockStates.Connecting) {
            pulsingBlockElement = element;
        } else if (pulsingBlockElement === element) {
            pulsingBlockElement = null;
        }
    });

    function scrollToPulsing() {
        if (!pulsingBlockElement) return;
        const rect = pulsingBlockElement.getBoundingClientRect();
        window.scrollTo({
            top: rect.top + window.scrollY - 80,
            behavior: "smooth"
        });
    }

    $effect(() => {
        // Recalculate when any of the following change
        block.column;
        block.row;
        block.state;
        canEditCircuit();
        getBlocks().filter(upperBlock => upperBlock.row! < block.row!).forEach(upperBlock => {
            upperBlock.state;
        });

        recalculateBounds();
    });

    function recalculateBounds() {
        block.boundingRect = () => element?.getBoundingClientRect?.();
    }

    onMount(async () => {
        await tick();
        recalculateBounds();

        if (window.location.hash === `#block-${block.id}`) {
            window.scrollTo(0, element.getBoundingClientRect().top);
        }
    });

    function mouseEnter() {
        if (block.state !== BlockStates.Inactive) {
            return;
        }
        block.state = BlockStates.Hovering;
    }

    function mouseLeave() {
        if (block.state !== BlockStates.Hovering) {
            return;
        }
        block.state = BlockStates.Inactive;
    }

    function mouseEnterBlock() {
        // on every level except module level, the action is go to
        // on module level it is expand, but only in view mode
        if ((block.blockType !== "skill" || (block.external && !canEditCircuit())) && block.state !== BlockStates.Editing) {
            action = BlockActions.Goto;
        } else if (!canEditCircuit()) {
            action = BlockActions.Expand;
        }
    }

    function mouseLeaveBlock() {
        action = undefined;
    }

    $effect(() => {
        block.preview = !canEditCircuit() && block.state === BlockStates.Hovering;
    })

    function click() {
        if (!clickable) {
            return;
        }

        if (block.blockType === "skill" && !block.external) {
            expanded = true;
        } else {
            loadPage(`/${getLevel().blocks}/${block.id}`);
        }
    }

    function dragStart(event: DragEvent) {
        if (!(event.target as HTMLElement).classList.contains("block-wrapper")) {
            return;
        }
        block.state = BlockStates.Dragging;
        event.dataTransfer!.setDragImage(element, 32, 24);
        event.dataTransfer!.effectAllowed = "move";
        event.dataTransfer!.setData("skill-circuits/block", block.id.toString());
        enableColumns();
    }

    function dragEnd(event: DragEvent) {
        if (!(event.target as HTMLElement).classList.contains("block-wrapper")) {
            return;
        }
        block.state = BlockStates.Inactive;
        disableColumns();
    }
</script>

<svelte:window onresize={recalculateBounds}/>

{#if pulsingBlockElement}
    <div class="scroll-to-pulse-container">
        <button class="scroll-to-pulse-button"
                onclick={scrollToPulsing}
                onmouseenter={onButtonMouseEnter}
                onmouseleave={onButtonMouseLeave}>
            <span class="icon fa-solid fa-location-dot"></span>
            <span class="text" class:expanded={scrollToPulsingHovered}>
                Go to skill{scrollToPulsingHovered ? ` "${block.name}"` : ""}
            </span>
        </button>
    </div>
{/if}

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div id="block-{block.id}" class="block-wrapper" style:grid-column={block.column! + 1} style:grid-row={block.row === undefined ? undefined : block.row + 1}
     draggable={draggable} ondragstart={dragStart} ondragend={dragEnd} data-clickable={clickable}
     data-editing={block.state === BlockStates.Editing}
     onmouseenter={mouseEnter} onmouseleave={mouseLeave}>
    <div bind:this={element} class="block"
         data-locked={locked} data-completed={completed} data-clickable={clickable} data-wiggle={block.state === BlockStates.Dragging}
         data-unfocus={unfocused} data-pulse={block.state === BlockStates.Connecting}
         data-hidden={hidden}
         onclick={click} onmouseenter={mouseEnterBlock} onmouseleave={mouseLeaveBlock}>
        {#if block.state === BlockStates.Editing}
            <BlockEditComponent {block}></BlockEditComponent>
        {:else if block.state === BlockStates.AssigningPaths && block.blockType === "skill"}
            <BlockAssignPathsComponent skill={block}></BlockAssignPathsComponent>
        {:else}
            <BlockContentComponent {block}></BlockContentComponent>
        {/if}
    </div>

    <div class="controls">
        {#if block.blockType === "skill" && !block.external && (block.state === BlockStates.Hovering || isSkillBookmarked(block))}
            <BookmarkSkillButtonComponent bind:action skill={block}></BookmarkSkillButtonComponent>
        {/if}
        {#if block.state === BlockStates.Hovering && (block.blockType !== "skill" || block.external) && !canEditCircuit()}
            <ExpandedViewOpenButtonComponent bind:action bind:open={expanded}></ExpandedViewOpenButtonComponent>
        {/if}
        {#if canEditCircuit() && (draggable || block.state === BlockStates.Hovering || block.state === BlockStates.Connecting || block.state === BlockStates.Editing)}
            <BlockControlsComponent {block} bind:action bind:draggable></BlockControlsComponent>
        {/if}
        {#if block.state === BlockStates.WaitingForConnection}
            <BlockManageConnectionsComponent skill={block as SkillBlock} bind:action bind:connectable></BlockManageConnectionsComponent>
        {/if}
        {#if action !== undefined}
            <BlockActionIndicationComponent {action} {block}></BlockActionIndicationComponent>
        {/if}
    </div>

    {#if isLevel(ModuleLevel) && !canEditCircuit()}
        <ExpandedBlockComponent {block} bind:open={expanded}></ExpandedBlockComponent>
    {/if}
</div>

<style>
    .block-wrapper {
        position: relative;
        transition: transform ease-in-out 150ms;
    }

    .block {
        background-color: var(--block-colour);
        border: var(--block-border);
        border-radius: var(--block-border-radius);
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        display: flex;
        flex-direction: column;
        gap: .5em;
        padding: 1em;
        position: relative;
        transition: filter ease-in-out 150ms, box-shadow ease-in-out 150ms;
    }

    .block[data-wiggle="true"] {
        animation: wiggle 250ms linear infinite;
    }

    .block[data-pulse="true"] {
        animation: pulse 2s linear infinite;
    }

    .block[data-unfocus="true"] {
        opacity: .25;
    }

    .block[data-hidden="true"] {
        opacity: .5;
    }

    .block[data-locked="true"] {
        filter: blur(.375em);
    }
    .block-wrapper:hover .block[data-locked="true"] {
        filter: none;
    }

    .block[data-completed="true"] {
        background-color: var(--block-completed-colour);
        border: var(--block-completed-border);
        color: var(--on-block-completed-colour);
    }

    .block-wrapper[data-clickable="true"]:hover .block:not([data-pulse="true"]) {
        transform: scale(1.05);
        box-shadow: .75rem 1.25rem 1.8rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
    }
    .block[data-clickable="true"] {
        cursor: pointer;
    }

    .scroll-to-pulse-container {
        position: fixed;
        bottom: 2rem;
        right: 2rem;
        z-index: 99;
        border: var(--glass-border);
        border-radius: var(--header-border-radius);
        padding: 1em;
        backdrop-filter: blur(.5rem) saturate(180%);
        background: var(--glass-colour);
        box-shadow:
                .75rem 1.25rem 1.9rem 0 color-mix(in srgb, var(--shadow-colour) 4%, transparent),
                inset 0.125em 0.125em 0.0625em 0 color-mix(in srgb, var(--glass-glint-colour) 60%, transparent),
                inset -0.0625em -0.0625em 0.0625em color-mix(in srgb, var(--glass-glint-colour) 40%, transparent);
    }

    .scroll-to-pulse-button {
        display: flex;
        flex-direction: column;
        justify-items: center;
        gap: 0.5em;
        background: var(--on-glass-surface-colour);
        border-radius: var(--header-border-radius);
        color: var(--on-glass-colour);
        border: none;
        cursor: pointer;
        padding: 1em;
    }

    .scroll-to-pulse-button:hover {
        background: var(--on-glass-surface-active-colour);
    }

    .scroll-to-pulse-button .icon {
        font-size: 1.5em;
    }

    @keyframes wiggle {
        0% {
            transform: rotate(0deg);
        }
        25% {
            transform: rotate(1.5deg);
        }
        75% {
            transform: rotate(-1.5deg);
        }
        100% {
            transform: rotate(0deg);
        }
    }

    @keyframes pulse {
        0% {
            transform: scale(1);
        }
        25% {
            transform: scale(1.03);
        }
        75% {
            transform: scale(0.97);
        }
        100% {
            transform: scale(1);
        }
    }

    @keyframes pulseReduced {
        0% {
            transform: scale(1);
        }
        25% {
            transform: scale(1.015);
        }
        75% {
            transform: scale(0.985);
        }
        100% {
            transform: scale(1);
        }
    }

    @media (prefers-reduced-motion: reduce) {
        .block[data-pulse="true"] {
            animation: pulseReduced 2s linear infinite;
        }
    }
</style>