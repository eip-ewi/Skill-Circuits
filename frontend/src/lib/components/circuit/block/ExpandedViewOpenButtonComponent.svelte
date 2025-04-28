<script lang="ts">
    import {cubicInOut} from "svelte/easing";
    import {type BlockAction, BlockActions} from "../../../data/block_action";
    import type {Block} from "../../../dto/circuit/block";

    let { action = $bindable(), open = $bindable() }: { action: BlockAction | undefined, open: boolean } = $props();

    function transition(element: Element) {
        return {
            duration: 100,
            easing: cubicInOut,
            css: (t: number) => `
                transform: scale(${t});
            `,
        };
    }
</script>

<button aria-label="Expand" onclick={ () => open = true } onmouseenter={ () => action = BlockActions.Expand } onmouseleave={ () => action = undefined } transition:transition>
    <span class="fa-solid fa-expand"></span>
</button>

<style>
    button {
        aspect-ratio: 1 / 1;
        background-color: var(--block-colour);
        color: var(--on-block-colour);
        border: none;
        border-radius: 8px;
        cursor: pointer;
        display: grid;
        place-items: center;
        position: absolute;
        min-width: 2rem;
        right: -.5rem;
        top: -.5rem;
        transform-origin: bottom left;
    }
    button:hover, button:focus-visible {
        background-color: var(--primary-surface-active-colour);
        color: var(--on-primary-surface-colour);
    }
</style>