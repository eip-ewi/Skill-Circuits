<script lang="ts">
    import {availableThemes, type Theme} from "../data/theme";
    import Select from "./util/Select.svelte";
    import {getTheme, setTheme} from "../logic/theme.svelte";
    import Button from "./util/Button.svelte";

    let dialog: HTMLDialogElement;

    function click(event: MouseEvent) {
        if (event.target === dialog) {
            dialog.close();
        }
    }

    function keyDown(event: KeyboardEvent) {
        if (event.key === "Escape") {
            event.preventDefault();
            dialog.close();
        }
    }
</script>

<dialog bind:this={dialog} id="theme-select" class="dialog glass" onclick={click} onkeydown={keyDown}>
    <div class="content">
        <h2 class="title">Select your theme</h2>
        <div class="themes">
            {#each availableThemes as theme}
                <div class="theme" data-theme={theme.name} data-colour-scheme={theme.colourScheme}>
                    <Button style="justify-content: center;" primary onclick={ () => setTheme(theme) }>{theme.displayName}</Button>
                </div>
            {/each}
        </div>
    </div>
</dialog>

<style>
    .dialog {
        border-radius: 1em;
        left: 50%;
        outline: none;
        position: fixed;
        top: 50%;
        transform: translate(-50%, -50%);
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
        display: grid;
        gap: 2em;
        min-width: 20em;
        padding: 2em;
    }

    .themes {
        display: grid;
        gap: 1em;
        font-size: var(--font-size-400);
    }

    .theme {
        display: grid;
    }
</style>