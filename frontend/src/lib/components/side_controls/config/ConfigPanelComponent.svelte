<script lang="ts">

    import {canEditCircuit, getAuthorisation} from "../../../logic/authorisation.svelte";
    import EditorManageComponent from "./EditorManageComponent.svelte";
    import {getEdition} from "../../../logic/edition/edition.svelte";
    import Button from "../../util/Button.svelte";
    import {downloadTeacherStats, setEditionVisibility} from "../../../logic/updates/edition_updates";
    import CopyEditionComponent from "./CopyEditionComponent.svelte";

    let { open = $bindable() }: { open: boolean } = $props();

    let copyDialogOpen: boolean = $state(false);

</script>

{#if canEditCircuit()}
    <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
    <div class="scrollable glass panel" aria-expanded={open}>
        <div class="heading">
            <h2>Course configuration</h2>
            <div class="controls">
                <button class="button" aria-label="Close panel" onclick={ () => open = false }>
                    <span class="fa-solid fa-arrow-right"></span>
                </button>
            </div>
        </div>

        <div class="content">
            <div class="section">
                <h3>Visibility</h3>
                {#if getEdition().published}
                    <p>This course edition is visible to students.</p>
                    <div>
                        <Button type="caution" onclick={ () => setEditionVisibility(false) }>
                            <span class="fa-solid fa-eye-slash"></span>
                            <span>Unpublish</span>
                        </Button>
                    </div>
                {:else}
                    <p>This course edition is not visible to students.</p>
                    <div>
                        <Button onclick={ () => setEditionVisibility(true) } >
                            <span class="fa-solid fa-eye"></span>
                            <span>Publish</span>
                        </Button>
                    </div>
                {/if}
            </div>

            <EditorManageComponent></EditorManageComponent>

            <div class="section">
                <h3>Copy</h3>
                <p>
                    You can copy this course edition to another course edition that you manage.
                </p>
                <div>
                    <Button onclick={ () => copyDialogOpen = true }>
                        <span class="fa-solid fa-copy"></span>
                        <span>Copy</span>
                    </Button>
                </div>
            </div>

            <div class="section">
                <h3>Download statistics</h3>
                <p>
                    You can download statistics for tasks and students in this edition.
                </p>
                <div>
                    <Button onclick={ () => downloadTeacherStats() }>
                        <span class="fa-solid fa-download"></span>
                        <span>Download</span>
                    </Button>
                </div>
            </div>
        </div>
    </div>

    <CopyEditionComponent bind:open={copyDialogOpen}></CopyEditionComponent>
{/if}

<style>
    .panel {
        border-radius: var(--panel-border-radius) 0 0 var(--panel-border-radius);
        inset-block: 2rem;
        max-width: 32rem;
        overflow-y: auto;
        overscroll-behavior: contain;
        position: fixed;
        right: 0;
        top: 2rem;
        transform-origin: right;
        transition: transform ease-in-out 150ms;
        z-index: 91;
    }

    .panel[aria-expanded="false"] {
        transform: scaleX(0);
    }
    .panel[aria-expanded="true"] {
        transition-delay: 150ms;
    }

    .heading {
        align-items: center;
        display: flex;
        justify-content: space-between;
        gap: 2rem;
        padding: 2rem 2rem 1rem 2rem;
    }

    .heading h2 {
        font-size: var(--font-size-500);
        font-weight: 700;
    }

    h3 {
        font-size: var(--font-size-400);
        font-weight: 500;
    }

    .controls {
        display: flex;
        gap: .25rem;
    }

    .section {
        display: grid;
        gap: .5em;
    }

    .button {
        background: var(--on-glass-surface-colour);
        border: none;
        border-radius: 8px;
        cursor: pointer;
        display: grid;
        justify-items: center;
        padding: 0.5rem;
        text-decoration: none;
    }
    .button:focus-visible, .button:hover {
        background: var(--on-glass-surface-active-colour);
    }

    .content {
        display: grid;
        gap: 2rem;
        padding: 0 2rem 2rem 2rem;
    }
</style>
