<script lang="ts">

import {getEdition} from "../../../logic/edition/edition.svelte";
</script>

<div>
    <h3>Editors</h3>

    <ul>
        {#each getEdition().teachers as teacher}
            <li>{teacher.displayName} (Teacher)</li>
        {/each}
        {#each getEdition().editors as editor}
            <li>{editor.displayName}</li>
        {/each}
    </ul>
</div>

<style>
    h3 {
        font-size: var(--font-size-400);
        font-weight: 500;
    }
</style>
