<script lang="ts">
    import {getLevel, isLevel} from "../../../logic/circuit/level.svelte";
    import BlockContentComponent from "../../circuit/block/BlockContentComponent.svelte";
    import type {Block} from "../../../dto/circuit/block";
    import {BlockStates} from "../../../data/block_state";
    import {disableColumns, enableColumns} from "../../../dto/columns.svelte";
    import type {SkillBlock} from "../../../dto/circuit/module/skill";
    import {canEditCircuit} from "../../../logic/authorisation.svelte";
    import ExpandedViewOpenButtonComponent from "../../circuit/block/ExpandedViewOpenButtonComponent.svelte";
    import {ModuleLevel} from "../../../data/level";
    import ExpandedBlockComponent from "../../circuit/block/ExpandedBlockComponent.svelte";
    import {onMount} from "svelte";
    import {loadPage} from "../../../logic/routing.svelte";
    import {isCompleted} from "../../../logic/circuit/skill_state/completion";
    import {getBlocks} from "../../../logic/circuit/circuit.svelte";

    let { skill }: { skill: SkillBlock } = $props();

    let element: HTMLElement | undefined;
    let expanded: boolean = $state(false);
    let hovering: boolean = $state(false);

    $effect(() => {
        if (element !== undefined) {
            skill.boundingRect = () => element!.getBoundingClientRect();
        }
    });

    function click() {
        if (!canEditCircuit()) {
            loadPage(`/skills/${skill.id}`);
        }
    }

    function dragStart(event: DragEvent) {
        event.dataTransfer!.effectAllowed = "link";
        event.dataTransfer!.setData("skill-circuits/block", skill.id.toString());
        enableColumns();
    }

    function dragEnd(event: DragEvent) {
        disableColumns();
    }
</script>

<!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
<div class="block-wrapper" onmouseenter={ () => hovering = true } onmouseleave={ () => hovering = false }>
    <div bind:this={element} class="block" draggable={canEditCircuit() && isLevel(ModuleLevel) && !getBlocks().some(b => b.id === skill.id)} ondragstart={dragStart} ondragend={dragEnd}
         data-clickable={!canEditCircuit()}
         onclick={click}
         data-completed={!canEditCircuit() && isCompleted(skill)}>
        <BlockContentComponent block={skill}></BlockContentComponent>
    </div>
    {#if hovering && !canEditCircuit()}
        <ExpandedViewOpenButtonComponent action={undefined} bind:open={expanded}></ExpandedViewOpenButtonComponent>
    {/if}
    <ExpandedBlockComponent block={skill} bind:open={expanded}></ExpandedBlockComponent>
</div>

<style>
    .block-wrapper {
        position: relative;
    }

    .block {
        background-color: var(--block-colour);
        border: var(--block-border);
        border-radius: var(--block-border-radius);
        box-shadow: 0.25rem 0.25rem 0.5rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        display: flex;
        flex-direction: column;
        gap: .5rem;
        padding: 1rem;
    }
    .block[data-completed="true"] {
        background-color: var(--block-completed-colour);
        border-color: var(--block-completed-border-colour);
        color: var(--on-block-completed-colour);
    }
    .block[data-clickable="true"] {
        cursor: pointer;
    }
    .block[draggable="true"] {
        cursor: grab;
    }
</style>