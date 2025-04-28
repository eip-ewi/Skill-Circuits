<script lang="ts">
    import {cubicInOut} from "svelte/easing";
    import {type BlockAction, BlockActions} from "../../../data/block_action";
    import {type BlockState, BlockStates} from "../../../data/block_state";
    import {getLevel} from "../../../logic/circuit/level.svelte";
    import {ModuleLevel} from "../../../data/level";
    import type {Block} from "../../../dto/circuit/block";
    import {getBlock, getBlocks, getGraph} from "../../../logic/circuit/circuit.svelte";
    import {connectSkills, disconnectSkills} from "../../../logic/circuit/updates/connection_updates.svelte";
    import type {SkillBlock} from "../../../dto/circuit/module/skill";

    let { skill, action = $bindable(), connectable = $bindable() }: { skill: SkillBlock, action: BlockAction | undefined, connectable: boolean } = $props();

    let connectUp: boolean = $state(false);
    let connectDown: boolean = $state(false);
    let disconnectUp: boolean = $state(false);
    let disconnectDown: boolean = $state(false);

    $effect(() => {
        let connectingTo = getBlocks().find(other => other.state === BlockStates.Connecting)!;

        if (skill.state === BlockStates.WaitingForConnection) {
            let alreadyConnected = getGraph().isAncestor(connectingTo, skill) || getGraph().isDescendant(connectingTo, skill);
            connectUp = !alreadyConnected;
            connectDown = !alreadyConnected;

            /*
             * It is allowed to connect if
             *  - for every common ancestor of from and to
             *  - there is at least one block between to and the ancestor
             *  - TODO apparently a similar check is necessary for descendants
             */
            let ancestors = getGraph().commonAncestors(skill, connectingTo);
            if (ancestors.some(ancestor => getGraph().getShortestPathToAncestor(skill, ancestor)!.length == 2)) {
                connectUp = false;
            }
            if (ancestors.some(ancestor => getGraph().getShortestPathToAncestor(connectingTo, ancestor)!.length == 2)) {
                connectDown = false;
            }

            disconnectUp = getGraph().isParent(connectingTo, skill);
            disconnectDown = getGraph().isParent(skill, connectingTo);
        }
    });

    $effect(() => {
        connectable = disconnectUp || disconnectDown || connectUp || connectDown;
    });

    function addParent() {
        let connectingTo = getBlocks().find(other => other.state === BlockStates.Connecting) as SkillBlock;
        connectSkills(connectingTo, skill);
        getBlocks().forEach(b => {
            b.state = BlockStates.Inactive;
        });
    }

    function addChild() {
        let connectingTo = getBlocks().find(other => other.state === BlockStates.Connecting) as SkillBlock;
        connectSkills(skill, connectingTo);
        getBlocks().forEach(b => {
            b.state = BlockStates.Inactive;
        });
    }

    function removeParent() {
        let disconnectingFrom = getBlocks().find(other => other.state === BlockStates.Connecting) as SkillBlock;
        disconnectSkills(disconnectingFrom, skill);
        getBlocks().forEach(b => {
            b.state = BlockStates.Inactive;
        });
    }

    function removeChild() {
        let disconnectingFrom = getBlocks().find(other => other.state === BlockStates.Connecting) as SkillBlock;
        disconnectSkills(skill, disconnectingFrom);
        getBlocks().forEach(b => {
            b.state = BlockStates.Inactive;
        });
    }

    function transition(element: HTMLElement) {
        return {
            duration: 100,
            easing: cubicInOut,
            css: (t: number) => `
                transform: scaleY(${t});
            `,
        };
    }
</script>

<div class="controls" transition:transition>
    {#if connectUp}
        <button aria-label="Add as dependency" class="fa-solid fa-arrow-up" onclick={addParent} onmouseenter={ () => action = BlockActions.AddParent } onmouseleave={ () => action = undefined }></button>
    {:else if disconnectUp}
        <button aria-label="Remove dependency" class="danger fa-solid fa-link-slash" onclick={removeParent} onmouseenter={ () => action = BlockActions.RemoveParent } onmouseleave={ () => action = undefined }></button>
    {:else}
        <div class="placeholder"></div>
    {/if}
    {#if connectDown}
        <button aria-label="Add as dependant" class="fa-solid fa-arrow-down" onclick={addChild} onmouseenter={ () => action = BlockActions.AddChild } onmouseleave={ () => action = undefined }></button>
    {:else if disconnectDown}
        <button aria-label="Remove dependant" class="danger fa-solid fa-link-slash" onclick={removeChild} onmouseenter={ () => action = BlockActions.RemoveChild } onmouseleave={ () => action = undefined }></button>
    {:else}
        <div class="placeholder"></div>
    {/if}
</div>

<style>
    .controls {
        display: grid;
        align-content: space-between;
        height: calc(100% + 2rem);
        left: 50%;
        position: absolute;
        top: 50%;
        transform-origin: center;
        translate: -50% -50%;
    }

    .controls > :not(.placeholder) {
        aspect-ratio: 1 / 1;
        background-color: var(--block-colour);
        border: none;
        border-radius: 8px;
        color: var(--on-block-colour);
        cursor: pointer;
        display: grid;
        min-width: 2rem;
        place-items: center;
    }

    .controls > :not(.placeholder):where(:hover, :focus-visible) {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }

    .controls > :not(.placeholder).danger:where(:hover, :focus-visible) {
        background-color: var(--primary-error-active-colour);
        color: var(--on-error-surface-colour);
    }
</style>