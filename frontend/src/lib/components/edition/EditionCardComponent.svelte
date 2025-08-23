<script lang="ts">
    import {loadPage} from "../../logic/routing.svelte";
    import type {EditionCard} from "../../dto/edition";
    import {getAuthorisation} from "../../logic/authorisation.svelte";

    let { edition }: { edition: EditionCard } = $props();
</script>

<button class="edition" onclick={ () => loadPage(`/editions/${edition.id}`) }>
    <span class="role">{getAuthorisation().managedEditions.includes(edition.id) ? 'Editor' : 'Student'}</span>
    <h3>
        <span>{edition.course.name}</span>
        <span>{edition.name}</span>
    </h3>
</button>

<style>
    .edition {
        background-color: var(--block-colour);
        border: var(--block-border);
        border-radius: var(--block-border-radius);
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        cursor: pointer;
        display: grid;
        justify-items: start;
        gap: 0;
        padding: 1em;
        text-decoration: none;
        transition: transform 150ms ease-in-out;
    }
    .edition:where(:hover, :focus-visible) {
        transform: scale(1.05);
        box-shadow: .75rem 1.25rem 1.8rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
    }

    h3 {
        display: grid;
        justify-items: start;
    }

    h3 span:first-child {
        font-size: var(--font-size-500);
        font-weight: 700;
    }

    .role {
        font-style: italic;
        opacity: 35%;
        margin-top: -.25em;
    }
</style>