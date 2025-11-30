<script lang="ts">

import PageLayout from "./PageLayout.svelte";
import ThemeSelectComponent from "../components/ThemeSelectComponent.svelte";
import {getBlurBlocks, getTheme, setBlurBlocks} from "../logic/preferences.svelte";
import ExampleSkillComponent from "../components/circuit/block/ExampleSkillComponent.svelte";

async function updateBlurBlocks(event: Event) {
    await setBlurBlocks((event.target as HTMLInputElement).checked);
}

</script>

<PageLayout>
    <div class="content">
        <h1>Settings</h1>

        <h2>Theme</h2>
        <p>Configure your theme for Skill Circuits.</p>
        <p class="theme-selection"><b>Selected theme:</b> { getTheme().displayName }</p>
        <ThemeSelectComponent></ThemeSelectComponent>

        <h2>Unreached content</h2>
        <p>Configure whether blocks (e.g., skills and submodules) you have not yet reached should be blurred, unless hovered over.</p>
        <div>
            <input type="checkbox" checked={getBlurBlocks()} onchange={e => updateBlurBlocks(e)} />

            Enable block blurring (see example below)
        </div>

        <ExampleSkillComponent locked={getBlurBlocks()}></ExampleSkillComponent>
    </div>
</PageLayout>

<style>
    h1 {
        font-size: var(--font-size-700);
        font-weight: 700;
    }
    h2 {
        font-size: var(--font-size-500);
        font-weight: 500;
        margin-bottom: .2em;
        margin-top: .8em;
    }
    p {
        margin-bottom: .5em;
    }
    p > b {
        font-weight: 700;
    }
    div {
        margin-bottom: .5em;
    }
    .content {
        margin-bottom: 7em;
    }
</style>