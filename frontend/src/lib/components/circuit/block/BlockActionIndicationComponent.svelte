<script lang="ts">
    import {cubicInOut} from "svelte/easing";
    import {EditionLevel, ModuleLevel, ProgrammeLevel, TrackLevel} from "../../../data/level";
    import {getLevel, isLevel} from "../../../logic/circuit/level.svelte";
    import type {Action} from "svelte/action";
    import {type BlockAction, BlockActions} from "../../../data/block_action";
    import type {Block} from "../../../dto/circuit/block";

    let { action, block }: { action: BlockAction | undefined, block: Block } = $props();

    function transition(element: Element) {
        return {
            duration: 100,
            easing: cubicInOut,
            css: (t: number) => `
                transform: scaleY(${t});
            `,
        };
    }
</script>

<div class="indication" transition:transition>
    {#if action === BlockActions.Goto}
        <span class="fa-solid fa-arrow-up-right-from-square" class:rotated={!isLevel(ModuleLevel)}></span>
        {#if isLevel(ModuleLevel)} <span>Go to source</span> {/if}
        {#if isLevel(EditionLevel)} <span>Go to module</span> {/if}
        {#if isLevel(TrackLevel)} <span>Go to course</span> {/if}
        {#if isLevel(ProgrammeLevel)} <span>Go to track</span> {/if}
    {/if}
    {#if action === BlockActions.Expand}
        <span class="fa-solid fa-expand"></span>
        {#if block.blockType === "skill" && !block.external}
            <span>Expand</span>
        {:else}
            <span>Open here</span>
        {/if}
    {/if}
    {#if action === BlockActions.Move}
        <span class="fa-solid fa-up-down-left-right"></span>
        <span>Move</span>
    {/if}
    {#if action === BlockActions.Edit}
        <span class="fa-solid fa-pencil"></span>
        <span>Edit</span>
    {/if}
    {#if action === BlockActions.Delete}
        <span class="fa-solid fa-trash"></span>
        <span>Delete</span>
    {/if}
    {#if action === BlockActions.Link}
        <span class="fa-solid fa-link"></span>
        <span>Edit connections</span>
    {/if}
    {#if action === BlockActions.AddParent}
        <span class="fa-solid fa-arrow-up"></span>
        <span>Add as dependency</span>
    {/if}
    {#if action === BlockActions.AddChild}
        <span class="fa-solid fa-arrow-down"></span>
        <span>Add as dependant</span>
    {/if}
    {#if action === BlockActions.RemoveParent}
        <span class="fa-solid fa-link-slash"></span>
        <span>Remove from dependencies</span>
    {/if}
    {#if action === BlockActions.RemoveChild}
        <span class="fa-solid fa-link-slash"></span>
        <span>Remove from dependants</span>
    {/if}
    {#if action === BlockActions.CancelLink}
        <span class="fa-solid fa-link-slash"></span>
        <span>Stop editing connections</span>
    {/if}
    {#if action === BlockActions.StopEdit}
        <span class="fa-solid fa-check"></span>
        <span>Stop editing</span>
    {/if}
    {#if action === BlockActions.Bookmark}
        <span class="fa-solid fa-bookmark"></span>
        <span>Bookmark</span>
    {/if}
</div>

<style>
    .indication {
        background-color: var(--neutral-surface-colour);
        border: var(--neutral-surface-border);
        border-radius: var(--surface-border-radius);
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        bottom: -3em;
        color: var(--on-neutral-surface-colour);
        cursor: pointer;
        left: 50%;
        padding: .5em 1em;
        position: absolute;
        transform-origin: top;
        translate: -50% 0;
        white-space: nowrap;
    }

    .rotated {
        transform: rotate(90deg);
    }
</style>