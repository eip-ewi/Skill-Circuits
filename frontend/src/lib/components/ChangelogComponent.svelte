<script lang="ts">
    import Button from "./util/Button.svelte";
    import {getReleaseDetails, showChangelog, versionUpdate} from "../logic/release_details.svelte.js";

    let dialog: HTMLDialogElement | undefined = $state();

    function closeChangelog(){
        versionUpdate();
        if(dialog !== undefined){
            dialog.close();
        }
    }

    function checkForClose(event: MouseEvent) {
        if (event.target === dialog) {
            closeChangelog();
        }
    }

    $effect(() => {
        if (dialog !== undefined && showChangelog()) {
            dialog.showModal();
            dialog.focus();
        }
    });


</script>

{#if showChangelog()}
    <dialog bind:this={dialog} oncancel={closeChangelog} onclick={checkForClose} id="changelog" class="dialog glass" >
        <div class = content>
            <h1 id="title">What's new</h1>
            <div id="update_text">
                {#each getReleaseDetails() as release}
                    <h2 >{release.title}</h2>
                    <div class="release_description"> {@html release.descriptionHtml}</div>
                {/each}
            </div>
        </div>
        <div id="close_changelog">
            <Button primary type="regular" onclick={closeChangelog}>
                <span>OK</span>
            </Button>
        </div>
    </dialog>
{/if}

<style>
    .dialog {
        border-radius: var(--dialog-border-radius);
        position: fixed;
        left: 50%;
        top: 40%;
        transform: translate(-50%, -50%);
        outline: none;
        padding: 2em 2em 4em;
        max-height: 70vh;
    }

    .content {
        max-height: calc(70vh - 6rem);
        overflow-y: auto;
    }

    .dialog::backdrop {
        backdrop-filter: blur(.15rem);
        background-color: hsla(0deg 0% 0% / .05);
        -ms-transform: translate3d(0,0,0);
        transform: translate3d(0,0,0);
    }

    #close_changelog {
        display: grid;
        place-items: center;
        height: 4em;
    }

    #title {
        font-size: var(--font-size-700);
        text-align: center;
        font-weight: 500;
    }

    h2 {
        font-size: var(--font-size-600);
        font-weight: 700;
    }

    .release_description{
        margin-bottom: 2em;
    }

    :global(.release_description ul) {
        padding-left: 1.5em;
    }

    :global(.release_description h2){
        padding-top: 1em;
        font-size: var(--font-size-500);
        font-weight: 700;
    }

</style>