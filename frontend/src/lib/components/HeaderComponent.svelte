<script lang="ts">

    import {getAuthenticatedPerson} from "../logic/authentication.svelte";
    import {getLevel, isLevel, isOnCircuit} from "../logic/circuit/level.svelte";
    import {type ModuleCircuit} from "../dto/circuit/module/module";
    import {EditionLevel, ModuleLevel, ProgrammeLevel, TrackLevel} from "../data/level";
    import {getCircuit} from "../logic/circuit/circuit.svelte";
    import {cubicInOut, linear} from "svelte/easing";
    import type {Attachment} from "svelte/attachments";
    import {canEditCircuit, getAuthorisation, isEditorForCircuit, setViewMode, toggleViewMode} from "../logic/authorisation.svelte";
    import Csrf from "./Csrf.svelte";
    import {loadHomePage, loadPage, pageMatches} from "../logic/routing.svelte";
    import {fade} from "svelte/transition";
    import {getEdition} from "../logic/edition/edition.svelte";

    let userMenuOpen: boolean = $state(false);

    async function returnToLastLeftOf() {
        let response = await fetch("/api/skills/last-active");
        if (response.ok) {
            loadPage(`/skills/${parseInt(await response.text())}`);
        }
    }

    function shrinkLeftMargin(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 200,
            easing: cubicInOut,
            css: (t: number) => `
                margin-left: ${(1-t) * 4 + 8}rem;
            `,
        };
    }

    function growLeftMargin(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 200,
            easing: cubicInOut,
            css: (t: number) => `
                margin-left: ${t * 4.25 + 7.75}rem;
                opacity: ${t};
            `,
        };
    }

    function moveRight(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 200,
            easing: cubicInOut,
            css: (t: number) => `
                transform: translateX(calc(${(1-t) * -100}% - ${(1-t) * 2}rem));
            `,
        };
    }

    function becomeSurface(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 200,
            easing: cubicInOut,
            css: (t: number) => `
                backdrop-filter: blur(${t * 0.5}rem) saturate(${t * 80 + 100}%);
                background: color-mix(in srgb, white ${t * 25}%, transparent);
                box-shadow:
                        .75rem 1.25rem 1.9rem 0 color-mix(in srgb, var(--shadow-colour) ${t * 4}%, transparent),
                        inset 0.125rem 0.125rem 0.0625rem 0 rgba(255 255 255 / ${t * 0.5}),
                        inset -0.0625rem -0.0625rem 0.0625rem rgba(255 255 255 / ${t * 0.5});
            `,
        };
    }

    function growVertical(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 200,
            easing: cubicInOut,
            css: (t: number) => `
                transform: scaleY(${t});
            `,
        };
    }
</script>

{#if isOnCircuit() || pageMatches(/\//)}
    <div class="header-wrapper">
        <div class="header surface">
            {#if !isOnCircuit()}
                <button class="button" onclick={returnToLastLeftOf}>
                    <span class="icon fa-solid fa-play"></span>
                    <span>Continue</span>
                </button>
            {/if}
            {#if isLevel(ModuleLevel)}
                <button class="button" onclick={ () => loadPage(`/editions/${getEdition().id}`) }>
                    <span class="icon fa-solid fa-arrow-up"></span>
                    <span>Go to course</span>
                </button>
            {/if}
            {#if isLevel(EditionLevel)}
                <button class="button">
                    <span class="icon fa-solid fa-arrow-up"></span>
                    <span>Go to track</span>
                </button>
            {/if}
            {#if isLevel(TrackLevel)}
                <button class="button">
                    <span class="programme fa-solid fa-arrow-up"></span>
                    <span>Go to track</span>
                </button>
            {/if}

            <button class="button" onclick={loadHomePage}>
                <span class="icon fa-solid fa-house"></span>
                <span>Home</span>
            </button>

            {#if userMenuOpen}
                <div in:shrinkLeftMargin={{}} style="opacity: 0; margin-left: 8rem;" aria-hidden="true">
                    <span class="icon fa-solid fa-user-circle"></span>
                    <span>{getAuthenticatedPerson()!.displayName}</span>
                </div>
            {:else}
                <button in:growLeftMargin={{delay: 350}} class="button" onclick={ () => userMenuOpen = true }>
                    <span class="icon fa-solid fa-user-circle"></span>
                    <span>{getAuthenticatedPerson()!.displayName}</span>
                </button>
            {/if}
        </div>

        {#if userMenuOpen}
            <div class="user-menu" in:moveRight={{}} out:moveRight={{delay: 200}}>
                <div class="surface" in:growVertical={{delay: 200}} out:growVertical={{}}>
                    <form action="/logout" method="post">
                        <Csrf></Csrf>
                        <button class="button">
                            <span class="icon fa-solid fa-right-from-bracket"></span>
                            <span>Log out</span>
                        </button>
                    </form>
                    {#if isOnCircuit() && isEditorForCircuit()}
                        {#if canEditCircuit()}
                            <button class="button" onclick={ () => toggleViewMode() }>
                                <span class="icon fa-solid fa-eye"></span>
                                <span>Student view</span>
                            </button>
                        {:else}
                            <button class="button" onclick={ () => toggleViewMode() }>
                                <span class="icon fa-solid fa-eye"></span>
                                <span>Editor view</span>
                            </button>
                        {/if}
                    {/if}
                    {#if getAuthorisation().isAdmin}
                        <button class="button" onclick={ () => setViewMode("ADMIN") }>
                            <span class="icon fa-solid fa-wrench"></span>
                            <span>Admin view</span>
                        </button>
                    {/if}
                    <button aria-label="Close menu" class="button" onclick={ () => userMenuOpen = false }>
                        <span class="icon fa-solid fa-chevron-down"></span>
                    </button>
                </div>
                <div class="surface" in:becomeSurface={{}} out:becomeSurface={{delay: 200}}>
                    <button class="button">
                        <span class="icon fa-solid fa-user-circle"></span>
                        <span>Profile</span>
                    </button>
                </div>
            </div>
        {/if}
    </div>
{/if}

<style>
    .header-wrapper {
        bottom: 2rem;
        gap: 2rem;
        left: 50%;
        position: fixed;
        translate: -50% 0;
        z-index: 100;
    }

    .header {
        align-items: center;
        display: flex;
    }

    .header > * + * {
        margin-left: 12rem;
    }

    .user-menu {
        bottom: 0;
        display: grid;
        gap: 1rem;
        left: calc(100% + 2rem);
        min-width: 7.75rem;
        position: absolute;
    }

    .user-menu > * {
        display: grid;
    }

    .user-menu :first-child {
        transform-origin: bottom;
    }

    .surface {
        border-radius: 100vh;
        box-shadow:
                .75rem 1.25rem 1.9rem 0 color-mix(in srgb, var(--shadow-colour) 4%, transparent),
                inset 0.125rem 0.125rem 0.0625rem 0 rgba(255 255 255 / 0.6),
                inset -0.0625rem -0.0625rem 0.0625rem rgba(255 255 255 / 0.4);
        padding: 1rem 1rem;
        position: relative;
        overflow: hidden;
    }

    .surface::before {
        backdrop-filter: blur(.5rem) saturate(180%);
        background-color: color-mix(in srgb, white 25%, transparent);
        content: "";
        height: 200%;
        left: -50%;
        position: absolute;
        top: -50%;
        width: 200%;
        z-index: -1;
    }

    .button {
        background: none;
        border: none;
        border-radius: 100vh;
        color: var(--on-header-colour);
        cursor: pointer;
        display: grid;
        justify-items: center;
        gap: 0.5rem;
        padding: 1rem;
        text-decoration: none;
    }
    .button:focus-visible, .button:hover {
        background: color-mix(in srgb, color-mix(in oklab, var(--primary-colour) 40%, white) 25%, transparent);
    }

    .icon {
        font-size: var(--font-size-600);
    }
</style>
