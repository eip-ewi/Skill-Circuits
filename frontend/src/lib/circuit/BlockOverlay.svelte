<script lang="ts">
    import type {BlockData} from "../data/block";
    import type {Level} from "../data/level";
    import type {ItemData} from "../data/item";
    import {withCsrf} from "../util/csrf";
    import BlockOverlayItem from "./BlockOverlayItem.svelte";
    import type {CircuitUpdates} from "../model/circuit_updates";
    import {onMount} from "svelte";

    let { updates, level, block }: { updates: CircuitUpdates, level: Level, block: BlockData } = $props();

    let element: HTMLDialogElement | undefined;
    let content: HTMLElement;
    let show: boolean = $state(false);

    onMount(() => {
        updates.subscribe("blockCompletion", completion => {
            if (completion.block.id === block.id && completion.completed) {
                setTimeout(() => element?.setAttribute("data-state", "closed"), 300);
                setTimeout(() => show = false, 500);
            }
        });
    });

    export function open(from: HTMLElement) {
        show = true;

        setTimeout(() => {
            let contentBounds: DOMRect = content.getBoundingClientRect();
            let blockBounds: DOMRect = from.getBoundingClientRect();
            let dx = blockBounds.x - window.innerWidth / 2 + blockBounds.width / 2;
            let dy = blockBounds.y - window.innerHeight / 2 + blockBounds.height / 2;
            let sx = blockBounds.width / contentBounds.width;
            let sy = blockBounds.height / contentBounds.height;
            content.style.setProperty("transform", `translate(${dx}px, ${dy}px) scale(${sx}, ${sy})`);
        }, 10);

        setTimeout(() => {
            element!.setAttribute("data-state", "opening");
            content.style.removeProperty("transform");
        }, 30);

        setTimeout(() => {
            element!.setAttribute("data-state", "open");
        }, 50);
    }

    function checkForClose(event: MouseEvent) {
        if (event.target === element) {
            show = false;
        }
    }
</script>

{#if show}
    <!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_noninteractive_element_interactions -->
    <dialog bind:this={element} class="expanded-block" onclick={checkForClose} open>
        <div bind:this={content} class="expanded-block__content">
            <h2 class="expanded-block__title">{block.name}</h2>
            <ul class="expanded-block__items">
                {#each block.items as item}
                    <BlockOverlayItem {updates} {level} {item}/>
                {/each}
            </ul>
        </div>
    </dialog>
{/if}

<style>
    .expanded-block {
        display: grid;
        place-items: center;
        background: none;
        border: none;
        position: fixed;
        width: 100vw;
        height: 100vh;
        top: 0;
        left: 0;
        transition: backdrop-filter ease-in 150ms;
        z-index: 90;
    }
    .expanded-block:global(:where([data-state="open"], [data-state="opening"])) {
        backdrop-filter: blur(.5rem);
    }

    .expanded-block__content {
        background: var(--block-colour);
        padding: 2rem;
        border-radius: 16px;
        display: flex;
        flex-direction: column;
        gap: 1rem;
        max-height: calc(100vh - 12rem);
        overflow-y: scroll;
        box-shadow: 2rem 2rem 4rem color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        transform: translateY(-3rem);
        opacity: 0;
    }
    .expanded-block__content > * {
        opacity: 0;
        transition: opacity ease-in 150ms;
    }
    .expanded-block:global(:where([data-state="opening"]) .expanded-block__content) {
        transition: transform ease-in 150ms;
        opacity: 1;
    }
    .expanded-block:global(:where([data-state="open"]) .expanded-block__content) {
        opacity: 1;
    }
    .expanded-block:global(:where([data-state="open"]) .expanded-block__content > *) {
        opacity: 1;
    }

    .expanded-block__title {
        font-size: 2rem;
        font-weight: 700;
    }

    .expanded-block__items {
        list-style: none;
        display: grid;
        gap: .25rem;
        grid-template-columns: repeat(3, max-content) auto;
    }
</style>