<script lang="ts">

import PageLayout from "./PageLayout.svelte";
import ThemeSelectComponent from "../components/ThemeSelectComponent.svelte";
import {setBlurSkills} from "../logic/preferences.svelte";

// TODO: maybe handle this differently
let blurSkillsSetting = true;

async function blurSkillsClicked() {
    await setBlurSkills(blurSkillsSetting);
}

</script>

<PageLayout>
    <div class="content">
        <h1>Settings</h1>

        <h2>Theme</h2>
        <p>Configure your theme for Skill Circuits.</p>
        <ThemeSelectComponent></ThemeSelectComponent>

        <h2>Skill Blurring</h2>
        <p>Configure whether or not skills you have not yet reached should be blurred.</p>
        <span>
            <input type="checkbox" bind:value={blurSkillsSetting} on:click={blurSkillsClicked} checked />

            Enable skill blurring
        </span>
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
    .content {
        margin-bottom: 7em;
    }
</style>