<script lang="ts">
    import {onMount, type Snippet} from "svelte";
    import type {ClassValue} from "svelte/elements";

    interface Properties {
        children: Snippet;

        multiple?: boolean;
        button?: Snippet<[ (event: MouseEvent) => void, () => void, () => void ]>;
        style?: string;
        onchange?: (event: Event) => void;
    }

    let { button, children, onchange, multiple = false, ...properties }: Properties = $props();

    let select: HTMLSelectElement;
    let element: HTMLElement;

    let options: { html: string, value: string }[] = $state([]);

    let open: boolean = $state(false);
    let selected: number[] = $state([]);
    let focusedIndex: number | undefined = $state();

    let enableOpenOnFocus: boolean = true;

    onMount(() => {
        selected = Array.from(select.options).filter(option => option.hasAttribute("selected")).map(option => option.index);
        options = Array.from(select.options).map(option => { return { html: option.innerHTML, value: option.value }; });

        if (onchange !== undefined) {
            select.addEventListener("change", onchange);
        }
    });

    $effect(() => {
        if (focusedIndex !== undefined) {
            (element.querySelectorAll(".option")[focusedIndex]! as HTMLButtonElement).focus();
        }
    });

    $effect(() => {
        let selectedSet: Set<number> = new Set(selected);
        let changed: boolean = Array.from(select.options).some(option => option.selected !== selectedSet.has(option.index));

        if (changed) {
            if (selected.length === 0) {
                select.value = "";
            } else {
                select.value = options[selected.at(-1)!]!.value;
            }

            for (let option of Array.from(select.options)) {
                option.selected = selectedSet.has(option.index);
            }

            select.dispatchEvent(new Event("change"));
        }

        if (!multiple) {
            closeDropdown();
        }
    })

    function click(event: MouseEvent) {
        event.preventDefault();
        if (open) {
            closeDropdown();
        } else {
            openDropdown();
        }
    }

    function focus() {
        if (!enableOpenOnFocus) {
            enableOpenOnFocus = true;
            return;
        }
        if (!open) {
            openDropdown();
        }
    }

    function blur() {
        requestClose();
    }

    function selectOption(index: number) {
        if (!multiple) {
            selected = [index];
            return;
        }
        if (selected.includes(index)) {
            selected.splice(selected.indexOf(index)!, 1);
        } else {
            selected.push(index);
        }
    }

    function blurOption() {
        requestClose();
    }

    function openDropdown() {
        open = true;
        focusedIndex = selected.length === 0 ? 0 : Math.min(...selected);
    }

    function closeDropdown() {
        open = false;
        focusedIndex = undefined;
        enableOpenOnFocus = false;
        (element.querySelector(".button")! as HTMLButtonElement).focus();
    }

    function requestClose() {
        setTimeout(() => {
            if (document.activeElement?.closest(".select") !== element) {
                open = false;
                focusedIndex = undefined;
            }
        }, 50);
    }

    function keyDown(event: KeyboardEvent) {
        if (options.length === 0 || !open) {
            return;
        }
        switch (event.key) {
            case "ArrowDown":
                event.preventDefault();
                focusedIndex = (focusedIndex! + 1) % options.length;
                break;
            case "ArrowUp":
                event.preventDefault();
                focusedIndex = (focusedIndex! - 1 + options.length) % options.length;
                break;
            case "Enter":
            case "Space":
                event.preventDefault();
                selectOption(focusedIndex!);
                break;
            case "Escape":
                event.preventDefault();
                closeDropdown();
                break;
            case "Home":
                event.preventDefault();
                focusedIndex = 0;
                break;
            case "End":
                event.preventDefault();
                focusedIndex = options.length - 1;
                break;
            case "a":
                if (event.ctrlKey && multiple) {
                    event.preventDefault();
                    if (selected.length === options.length) {
                        selected = [];
                    } else {
                        selected = options.map((_, i) => i);
                    }
                }
                break;
        }
    }
</script>

<select bind:this={select} multiple={multiple}>
    {@render children()}
</select>

<!-- svelte-ignore a11y_interactive_supports_focus -->
<div bind:this={element} class="select" role="listbox" aria-expanded={open} onkeydown={keyDown}>
    {#if button === undefined}
        <button class="button" onmousedown={click} onfocus={focus} onblur={blur} {...properties}>
            {#if selected.length === 0}
                <span class="placeholder">Select...</span>
            {:else}
                {#each selected as index}
                    <span>{@html options[index]?.html}</span>
                {/each}
            {/if}
            <span class="arrow fa-solid fa-chevron-down"></span>
        </button>
    {:else}
        {@render button(click, focus, blur)}
    {/if}

    <div class="scrollable glass options" tabindex="-1">
        {#each options as option, i}
            <button tabindex="-1" class="option" role="option" aria-selected={selected.includes(i)} value={option.value} onclick={ () => selectOption(i) } onblur={blurOption}>
                <span>{@html option.html}</span>
                <span class="check">{'\u2713'}</span>
            </button>
        {/each}
    </div>
</div>

<style>
    select {
        display: none;
    }

    .select {
        display: grid;
        position: relative;
    }

    .button {
        align-items: center;
        background: var(--block-colour);
        border: 1px solid var(--on-block-divider-colour);
        border-radius: .5em;
        color: var(--on-block-colour);
        cursor: pointer;
        display: flex;
        gap: .5em;
        justify-content: space-between;
        padding: .5em 1em;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .options {
        border-radius: var(--option-border-radius);
        display: grid;
        gap: .5em;
        min-width: 100%;
        max-height: 24em;
        overflow-y: auto;
        padding: .5em .5em;
        position: absolute;
        overscroll-behavior: contain;
        top: 100%;
        transform-origin: top;
        transition: transform 150ms ease-in-out;
        z-index: 999;
    }
    .select[aria-expanded="false"] .options {
        transform: scaleY(0);
    }

    .option {
        align-items: center;
        background: none;
        border: none;
        border-radius: var(--option-border-radius);
        color: var(--on-glass-colour);
        cursor: pointer;
        display: flex;
        justify-content: space-between;
        padding: 0.5em 1em;
        gap: .5em;
        white-space: nowrap;
    }
    .option:hover, .option:focus-visible {
        background-color: var(--option-active-colour);
        color: var(--on-option-active-colour);
    }

    .option[aria-selected="false"] .check {
        opacity: 0;
    }
</style>