<script lang="ts">

    import {onDestroy, onMount, type Snippet, tick} from "svelte";

    let { open = $bindable(), children, dropdown: dropdownContent }: { open: boolean, children: Snippet, dropdown: Snippet } = $props();

    let anchor: HTMLElement;
    let dropdown: HTMLElement;

    $effect(() => {
        if (open) {
            if (dropdown.showPopover !== undefined) {
                dropdown.showPopover();
                if (anchor.getBoundingClientRect().left + dropdown.getBoundingClientRect().width < window.innerWidth) {
                    dropdown.style.left = `${anchor.getBoundingClientRect().left}px`;
                } else {
                    dropdown.style.left = `${anchor.getBoundingClientRect().right - dropdown.getBoundingClientRect().width}px`;
                }
                dropdown.style.top = `${anchor.getBoundingClientRect().bottom}px`;
            }
        } else {
            dropdown.hidePopover?.();
            dropdown.style.removeProperty("left");
            dropdown.style.removeProperty("top");
        }
    });

</script>

<div class="wrapper">
    <div bind:this={anchor} class="anchor">
        {@render children() }
    </div>
    <div bind:this={dropdown} role="menu" class="scrollable glass dropdown" popover onbeforetoggle={ event => open = event.newState === "open" } data-expanded={open}>
        <div class="content">
            {@render dropdownContent() }
        </div>
    </div>
</div>

<style>
    .wrapper {
        position: relative;
    }

    .dropdown {
        border-radius: var(--dropdown-border-radius);
        display: none;
        inset: auto;
        left: 0;
        padding: .5em .5em;
        position: fixed;
        transition: display 1500ms, transform 150ms ease-in-out;
        transition-behavior: allow-discrete;
        transform: scaleY(0);
        transform-origin: top;
        top: 100%;
    }
    .dropdown[data-expanded="true"] {
        display: block;
        transform: scaleY(1);
        @starting-style {
            transform: scaleY(0);
        }
    }

    .content {
        max-height: 24em;
        overflow-y: auto;
        overscroll-behavior: contain;
    }
</style>