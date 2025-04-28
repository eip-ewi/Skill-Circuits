<script lang="ts">

   import type {Group} from "../../../dto/circuit/group";
   import {onMount} from "svelte";
   import {createBlobs} from "../../../logic/circuit/group_placement";
   import type {Blob} from "../../../data/blob";
   import GroupComponent from "./GroupComponent.svelte";

   let { groups }: { groups: Group[] } = $props();

   let blobs: Blob[] = $state([]);

   $effect(() => {
       if (groups.some(group => group.blocks.some(block => block.column !== null && block.row === undefined))) {
           return;
       }
       blobs = createBlobs(groups);
   })

</script>

{#each blobs as blob}
    <GroupComponent {blob}></GroupComponent>
{/each}

<style>
</style>