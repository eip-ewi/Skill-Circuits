<script lang="ts">
    import type { Snippet } from "svelte";

    let {
        open = $bindable(),
        children,
        dropdown: dropdownContent,
    }: { open: boolean; children: Snippet; dropdown: Snippet } = $props();

    let anchor: HTMLElement;
    let dropdown: HTMLElement;

    function alignDropdownToAnchor() {
        if (dropdown?.showPopover === undefined) {
            return;
        }
        const a = anchor.getBoundingClientRect();
        dropdown.style.left =
            a.left + dropdown.getBoundingClientRect().width < window.innerWidth
                ? `${a.left}px`
                : `${a.right - dropdown.getBoundingClientRect().width}px`;
        dropdown.style.top = `${a.bottom}px`;
    }

    $effect(() => {
        if (!open) {
            dropdown.hidePopover();
            return;
        }
        dropdown.showPopover();
        alignDropdownToAnchor();

        const viewport = window.visualViewport;
        // Batch bursts of scroll/resize events into one reposition per frame.
        let rafId: number | undefined;
        const scheduleReposition = () => {
            if (rafId !== undefined) {
                return;
            }
            rafId = requestAnimationFrame(() => {
                rafId = undefined;
                alignDropdownToAnchor();
            });
        };

        // Capture phase so scrolling inside nested scroll containers is caught (scroll doesn't bubble).
        window.addEventListener("scroll", scheduleReposition, true);
        window.addEventListener("resize", scheduleReposition);
        viewport?.addEventListener("scroll", scheduleReposition);
        viewport?.addEventListener("resize", scheduleReposition);
        return () => {
            window.removeEventListener("scroll", scheduleReposition, true);
            window.removeEventListener("resize", scheduleReposition);
            viewport?.removeEventListener("scroll", scheduleReposition);
            viewport?.removeEventListener("resize", scheduleReposition);
            if (rafId !== undefined) {
                cancelAnimationFrame(rafId);
            }
        };
    });
</script>

<div class="wrapper">
    <div bind:this={anchor} class="anchor">
        {@render children()}
    </div>
    <div
        bind:this={dropdown}
        role="menu"
        class="scrollable glass dropdown"
        popover
        onbeforetoggle={event => (open = event.newState === "open")}
        data-expanded={open}>
        <div class="content">
            {@render dropdownContent()}
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
        padding: 0.5em 0.5em;
        position: fixed;
        transition:
            display 1500ms,
            transform 150ms ease-in-out;
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
