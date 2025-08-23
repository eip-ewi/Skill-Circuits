<script lang="ts">

    import {getAuthenticatedPerson} from "../logic/authentication.svelte";
    import {getLevel, isLevel, isOnCircuit} from "../logic/circuit/level.svelte";
    import {type ModuleCircuit} from "../dto/circuit/module/module";
    import {EditionLevel, ModuleLevel, ProgrammeLevel, TrackLevel} from "../data/level";
    import {getCircuit} from "../logic/circuit/circuit.svelte";
    import {cubicInOut, linear} from "svelte/easing";
    import type {Attachment} from "svelte/attachments";
    import {canEditCircuit, getAuthorisation, isEditorForAny, isEditorForCircuit, setViewMode, toggleViewMode} from "../logic/authorisation.svelte";
    import Csrf from "./Csrf.svelte";
    import {loadHomePage, loadPage, pageMatches} from "../logic/routing.svelte";
    import {fade} from "svelte/transition";
    import {getEdition} from "../logic/edition/edition.svelte";
    import ThemeSelectComponent from "./ThemeSelectComponent.svelte";
    import WIthConfirmationDialog from "./util/WithConfirmationDialog.svelte";
    import WithConfirmationDialog from "./util/WithConfirmationDialog.svelte";
    import {resetProgress} from "../logic/updates/edition_updates";

    let userMenuOpen: boolean = $state(false);

    async function returnToLastLeftOf() {
        let response = await fetch("/api/skills/last-active");
        if (response.ok) {
            loadPage(`/skills/${parseInt(await response.text())}`);
        }
    }

    function openThemeDialog() {
        (document.getElementById("theme-select") as HTMLDialogElement).showModal();
    }

    function shrinkLeftMargin(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 200,
            easing: cubicInOut,
            css: (t: number) => `
                margin-left: calc(${(1-t) / 3} * var(--spacing) + var(--spacing) / 3 * 2);
            `,
        };
    }

    function growLeftMargin(node: HTMLElement, params: { delay?: number }) {
        return {
            delay: params.delay || 0,
            duration: 200,
            easing: cubicInOut,
            css: (t: number) => `
                margin-left: calc(${t / 48  * 17} * var(--spacing) + var(--spacing) / 48 * 31);
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
                transform: translateX(calc(${(1-t) * -100}% - ${(1-t) * 2}em));
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
                        inset 0.125em 0.125em 0.0625em 0 color-mix(in srgb, var(--glass-glint-colour) ${t * 60}%, transparent),
                        inset -0.0625em -0.0625em 0.0625em color-mix(in srgb, var(--glass-glint-colour) ${t * 40}%, transparent);
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
        <div class="header glass surface">
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
                <div in:shrinkLeftMargin={{}} style="opacity: 0; margin-left: calc(var(--spacing) / 3 * 2);" aria-hidden="true">
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
                <div class="glass surface" in:growVertical={{delay: 200}} out:growVertical={{}}>
                    <form action="/logout" method="post">
                        <Csrf></Csrf>
                        <button class="button">
                            <span class="icon fa-solid fa-right-from-bracket"></span>
                            <span>Log out</span>
                        </button>
                    </form>
                    <button class="button" onclick={openThemeDialog}>
                        <span class="icon fa-solid fa-palette"></span>
                        <span>Theme</span>
                    </button>
                    {#if !canEditCircuit() && isLevel(EditionLevel)}
                        <WithConfirmationDialog icon="fa-solid fa-repeat" action="Reset progress" onconfirm={ () => resetProgress() }>
                            {#snippet button(openConfirmationDialog: () => void) }
                                <button class="button" onclick={openConfirmationDialog}>
                                    <span class="icon fa-solid fa-repeat"></span>
                                    <span>Reset progress</span>
                                </button>
                            {/snippet}
                            <p>
                                Are you sure you want to reset all your progress in '{getEdition().name}'?
                            </p>
                        </WithConfirmationDialog>
                    {/if}
                    {#if isEditorForAny()}
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
                <div class="glass surface" in:becomeSurface={{}} out:becomeSurface={{delay: 200}}>
                    <button class="button">
                        <span class="icon fa-solid fa-user-circle"></span>
                        <span>Profile</span>
                    </button>
                </div>
            </div>
        {/if}
    </div>

    <ThemeSelectComponent></ThemeSelectComponent>
{/if}


<style>
    .header-wrapper {
        --spacing: calc(min(calc(12em * 4), calc(100vw - 20rem)) / 4);
        font-size: clamp(0.5rem, 0.85vw, 1rem);

        bottom: 2rem;
        gap: 2em;
        left: 50%;
        position: fixed;
        translate: -50% 0;
        z-index: 100;
    }

    .header {
        align-items: center;
        display: flex;
        white-space: nowrap;
    }

    .header > * + * {
        margin-left: var(--spacing);
    }

    .user-menu {
        bottom: 0;
        display: grid;
        gap: 1em;
        left: calc(100% + 2em);
        min-width: 7.75em;
        position: absolute;
    }

    .user-menu > * {
        display: grid;
    }

    .user-menu > :first-child {
        justify-content: center;
        transform-origin: bottom;
    }

    .user-menu > :last-child {
        white-space: nowrap;
    }

    .user-menu form {
        display: grid;
    }

    .surface {
        border-radius: var(--header-border-radius);
        padding: 1em 1em;
    }

    .button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: var(--header-border-radius);
        color: var(--on-glass-colour);
        cursor: pointer;
        display: grid;
        justify-items: center;
        gap: 0.5em;
        padding: 1em;
        text-decoration: none;
    }
    .button:focus-visible, .button:hover {
        background: var(--on-glass-surface-active-colour);
    }

    .icon {
        font-size: 1.5em;
    }
</style>
