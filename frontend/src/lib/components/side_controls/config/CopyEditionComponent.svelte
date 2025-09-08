<script lang="ts">


    import type {ManagedEdition} from "../../../dto/edition";
    import Button from "../../util/Button.svelte";
    import Select from "../../util/Select.svelte";
    import {getEdition} from "../../../logic/edition/edition.svelte";
    import {copyEdition} from "../../../logic/updates/edition_updates";

    let { open = $bindable() }: { open: boolean } = $props();

    let dialog: HTMLDialogElement;
    let editions: ManagedEdition[] = $state([]);
    let selectedEdition: ManagedEdition | undefined = $state();
    let showWarning: boolean = $derived(selectedEdition?.hasCircuit ?? false);

    $effect(() => {
        if (open) {
            dialog.showModal();
        } else {
            dialog.close();
        }
    })

    function selectEdition(event: Event) {
        let editionId = parseInt((event.target as HTMLSelectElement).value);
        selectedEdition = editions.find(e => e.id === editionId)!;
    }

    async function fetchEditions() {
        let response = await fetch("/api/editions/managed");
        editions = await response.json();
    }

    async function copy() {
        await copyEdition(selectedEdition!.id);
        open = false;
    }

</script>

<dialog bind:this={dialog} class="scrollable glass dialog">
    <div class="content">
        <h2>Copy circuit</h2>

        <div>
            {#await fetchEditions() then _}
                <span>Copy to course edition</span>
                <Select onchange={selectEdition}>
                    {#each editions as edition}
                        {#if getEdition().id !== edition.id}
                            <option value={edition.id}>{edition.course.name} - {edition.name}</option>
                        {/if}
                    {/each}
                </Select>
            {/await}
        </div>

        {#if showWarning}
            <div class="warning">
                <span class="fa-solid fa-triangle-exclamation"></span>
                <p>
                    The course edition you have selected already contains a circuit.
                    Copying to this course edition will overwrite all its content.
                </p>
            </div>
        {/if}

        <div class="buttons">
            <Button onclick={ () => open = false }>
                <span class="fa-solid fa-xmark"></span>
                <span>Cancel</span>
            </Button>
            <Button primary onclick={copy}>
                <span class="fa-solid fa-copy"></span>
                <span>Copy</span>
            </Button>
        </div>
    </div>
</dialog>

<style>
    .dialog {
        border-radius: 1em;
        left: 50%;
        min-width: min(calc(100vw - 2em), 32em);
        outline: none;
        position: fixed;
        top: 50%;
        transform: translate(-50%, -50%);
        overflow: visible;
    }

    .dialog::backdrop {
        backdrop-filter: blur(.15rem);
        background-color: hsla(0deg 0% 0% / .05);
    }

    h2 {
        font-size: var(--font-size-500);
        font-weight: 700;
    }

    .content {
        align-items: start;
        display: grid;
        gap: 2em;
        padding: 2em;
    }

    .buttons {
        display: flex;
        justify-content: space-between;
    }

    .warning {
        align-items: center;
        backdrop-filter: blur(.25rem);
        background-color: var(--warning-banner-colour);
        border: var(--warning-banner-border);
        border-radius: var(--warning-banner-border-radius);
        color: var(--on-warning-banner-colour);
        display: flex;
        gap: 1em;
        padding: .5em 1em;
    }

    .warning .fa-solid {
        font-size: var(--font-size-500);
    }
</style>
