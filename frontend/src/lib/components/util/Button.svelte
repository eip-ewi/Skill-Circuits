<script lang="ts">

    import type {Snippet} from "svelte";
    import type {HTMLStyleAttributes} from "svelte/elements";

    interface Properties {
        children: Snippet;

        primary?: boolean;
        type?: "regular" | "caution";
        square?: boolean;

        style?: string;
        href?: string;
        target?: string;
        "aria-label"?: string;
        "aria-pressed"?: boolean;

        onclick?: (event: MouseEvent) => void;
        onmouseenter?: (event: MouseEvent) => void;
        onmouseleave?: (event: MouseEvent) => void;
        onmousedown?: (event: MouseEvent) => void;
        onfocus?: (event: FocusEvent) => void;
        onblur?: (event: FocusEvent) => void;
    }

    let props: Properties = $props();

</script>

{#if props.href !== undefined}
    <a class="button" href={props.href} target={props.target} style={props.style}
       data-primary={props.primary} data-type={props.type ?? "regular"} data-square={props.square}
       onclick={ e => props?.onclick?.(e) } onmouseenter={ e => props.onmouseenter?.(e) } onmouseleave={ e => props.onmouseleave?.(e) }
       onmousedown={ e => props?.onmousedown?.(e) }
       onfocus={ e => props?.onfocus?.(e) } onblur={ e => props?.onblur?.(e) }
    >
        {@render props.children() }
    </a>
{:else}
    <button class="button" style={props.style} data-primary={props.primary} data-type={props.type ?? "regular"} data-square={props.square}
            aria-label={props["aria-label"]} aria-pressed={props["aria-pressed"]}
            onclick={ e => props?.onclick?.(e) } onmouseenter={ e => props.onmouseenter?.(e) } onmouseleave={ e => props.onmouseleave?.(e) }
            onmousedown={ e => props?.onmousedown?.(e) }
            onfocus={ e => props?.onfocus?.(e) } onblur={ e => props?.onblur?.(e) }
    >
        {@render props.children() }
    </button>
{/if}

<style>
    .button {
        align-items: center;
        background-color: var(--inactive-colour);
        border: var(--inactive-border);
        border-radius: var(--surface-border-radius);
        cursor: pointer;
        color: var(--on-inactive-colour);
        display: flex;
        gap: .25em;
        padding: .5em 1em;
        text-decoration: none;
    }
    .button:hover, .button:focus-visible {
        background-color: var(--active-colour);
        border-color: var(--active-border-colour);
        color: var(--on-active-colour);
    }

    .button[data-square="true"] {
        aspect-ratio: 1 / 1;
        display: grid;
        min-width: 2em;
        padding: initial;
        place-items: center;
    }

    .button {
        --inactive-colour: var(--neutral-surface-colour);
        --on-inactive-colour: var(--on-neutral-surface-colour);
        --inactive-border: var(--neutral-surface-border);
    }
    .button[data-type="regular"] {
        --active-colour: var(--neutral-surface-active-colour);
        --on-active-colour: var(--on-neutral-surface-active-colour);
        --active-border-colour: var(--neutral-surface-active-border-colour);
    }
    .button[data-type="caution"] {
        --active-colour: var(--error-surface-active-colour);
        --on-active-colour: var(--on-error-surface-active-colour);
        --active-border-colour: var(--error-surface-active-border-colour);
    }

    .button[data-type="regular"][data-primary="true"] {
        --inactive-colour: var(--primary-surface-colour);
        --on-inactive-colour: var(--on-primary-surface-colour);
        --inactive-border: var(--primary-surface-border);
        --active-colour: var(--primary-surface-active-colour);
        --on-active-colour: var(--on-primary-surface-active-colour);
        --active-border-colour: var(--primary-surface-active-border-colour);
    }
    .button[data-type="caution"][data-primary="true"] {
        --inactive-colour: var(--error-surface-colour);
        --on-inactive-colour: var(--on-error-surface-colour);
        --inactive-border: var(--error-surface-border);
        --active-colour: var(--error-surface-active-colour);
        --on-active-colour: var(--on-error-surface-active-colour);
        --active-border-colour: var(--error-surface-active-border-colour);
    }
</style>