<script lang="ts">
    import {cubicInOut} from "svelte/easing";
    import {type BlockAction, BlockActions} from "../../../data/block_action";
    import type {Block} from "../../../dto/circuit/block";
    import Button from "../../util/Button.svelte";

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

<div class="expand-button" transition:transition>
    <Button square aria-label="Expand" onclick={ () => open = true } onmouseenter={ () => action = BlockActions.Expand } onmouseleave={ () => action = undefined }>
        <span class="fa-solid fa-expand"></span>
    </Button>
</div>

<style>
    .expand-button {
        position: absolute;
        right: -.5em;
        top: -.5em;
        transform-origin: bottom left;
    }
</style>