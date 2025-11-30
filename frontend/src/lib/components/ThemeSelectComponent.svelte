<script lang="ts">
    import {availableThemes} from "../data/theme";
    import Button from "./util/Button.svelte";
    import {getThemeColorScheme, getThemeName, setTheme} from "../logic/preferences.svelte";
</script>

<div class="content">
    <div class="themes">
        {#each availableThemes as theme}
            <div class="theme" data-theme={getThemeName(theme)} data-colour-scheme={getThemeColorScheme(theme)}>
                <Button style="justify-content: center; height: 3em; width: 100%;" primary onclick={ () => setTheme(theme) }>{theme.displayName}</Button>
            </div>
        {/each}
    </div>
</div>

<style>
    .content {
        display: grid;
        gap: 2em;
        min-width: 20em;
    }

    .themes {
        display: grid;
        gap: 1em;
        font-size: var(--font-size-400);
        grid-auto-flow: column;
        grid-template-columns: 10em 10em 10em 10em;
        background-color: var(--block-colour);
        border: var(--block-border);
        border-radius: var(--surface-border-radius);
        padding: 0.8em;
        width: fit-content;
    }

    .theme {
        display: flex;
        align-items: center;
        flex-direction: column;
        justify-content: flex-end;
    }
</style>