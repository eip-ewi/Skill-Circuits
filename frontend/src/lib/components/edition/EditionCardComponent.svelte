<script lang="ts">
    import type {Role} from "../../dto/role";
    import {roleToName} from "../../logic/role";
    import {loadPage} from "../../logic/routing.svelte";
    import type {EditionCard} from "../../dto/edition";

    let { edition, role }: { edition: EditionCard, role: Role } = $props();
</script>

<button class="edition" onclick={ () => loadPage(`/editions/${edition.id}`) }>
    <h3>
        <span>{edition.course.name}</span>
        <span>{edition.name}</span>
    </h3>
    <span class="role">{roleToName(role)}</span>
</button>

<style>
    .edition {
        background-color: var(--block-colour);
        border: none;
        border-radius: 8px;
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        cursor: pointer;
        display: grid;
        justify-items: start;
        gap: 1rem;
        padding: .5rem 1rem;
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
</style>