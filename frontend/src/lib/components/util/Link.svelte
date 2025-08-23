<script lang="ts">

    import type {Snippet} from "svelte";

    interface Properties {
        children: Snippet;
        href?: string;

        target?: string;

        onclick?: (event: MouseEvent) => void;
    }

    let props: Properties = $props();

</script>

<a href={props.href} target={props.target} onclick={props.onclick}>
    {@render props.children() }
</a>

<style>
    a {
        color: var(--link-colour);
        cursor: pointer;
        text-decoration: none;
    }

    a:hover, a:focus-visible {
        color: var(--link-active-colour);
    }
</style>