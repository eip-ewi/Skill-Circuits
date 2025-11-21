<script lang="ts">

import PageLayout from "./PageLayout.svelte";
import ThemeSelectComponent from "../components/ThemeSelectComponent.svelte";
import {getBlurSkills, setBlurSkills} from "../logic/preferences.svelte";
import ExampleSkillComponent from "../components/circuit/block/ExampleSkillComponent.svelte";

async function updateBlurSkills(event: Event) {
    await setBlurSkills((event.target as HTMLInputElement).checked);
}

</script>

<PageLayout>
    <div class="content">
        <h1>Settings</h1>

        <h2>Theme</h2>
        <p>Configure your theme for Skill Circuits.</p>
        <ThemeSelectComponent></ThemeSelectComponent>

        <h2>Block Blurring</h2>
        <p>Configure whether blocks (i.e., skills and submodules) you have not yet reached should be blurred.</p>
        <div>
            <input type="checkbox" checked={getBlurSkills()} onchange={e => updateBlurSkills(e)} />

            Enable block blurring (see example below)
        </div>

        <ExampleSkillComponent locked={getBlurSkills()}></ExampleSkillComponent>
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
    div {
        margin-bottom: .5em;
    }
    .content {
        margin-bottom: 7em;
    }
</style>