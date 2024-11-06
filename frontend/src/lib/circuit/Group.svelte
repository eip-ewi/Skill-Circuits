<script lang="ts">

   import type {GroupData} from "../data/group";

   let { group, column, row, showLabel = false,
       connectLeft = false, connectRight = false, connectTop = false, connectBottom = false,
       connectTopLeft = false, connectTopRight = false, connectBottomRight = false, connectBottomLeft = false } :
       { group: GroupData, column: number, row: number, showLabel: boolean,
       connectLeft: boolean, connectRight: boolean, connectTop: boolean, connectBottom: boolean,
       connectTopLeft: boolean, connectTopRight: boolean, connectBottomRight: boolean, connectBottomLeft: boolean } = $props();

</script>

<div class="group" style:grid-row="{row+1}" style:grid-column="{column+1}"
     data-connect-bottom="{connectBottom}" data-connect-top="{connectTop}" data-connect-left="{connectLeft}" data-connect-right="{connectRight}"
     data-connect-top-left="{connectTopLeft}" data-connect-top-right="{connectTopRight}" data-connect-bottom-right="{connectBottomRight}" data-connect-bottom-left="{connectBottomLeft}"
>
    {#if showLabel} <span class="group__name">{group.name}</span> {/if}
    {#if connectTopLeft || connectTop && connectLeft} <div class="group__corner" data-corner-vertical="top" data-corner-horizontal="left"></div> {/if}
    {#if connectTopRight || connectTop && connectRight} <div class="group__corner" data-corner-vertical="top" data-corner-horizontal="right"></div> {/if}
    {#if connectBottomRight || connectBottom && connectRight} <div class="group__corner" data-corner-vertical="bottom" data-corner-horizontal="right"></div> {/if}
    {#if connectBottomLeft || connectBottom && connectLeft} <div class="group__corner" data-corner-vertical="bottom" data-corner-horizontal="left"></div> {/if}
</div>

<style>
    .group {
        position: relative;
    }
    .group::before, .group::after {
        background-color: var(--group-colour);
        border-radius: 24px;
        content: "";
        height: calc(100% + 4rem);
        left: 0;
        margin: -2rem;
        position: absolute;
        top: 0;
        width: calc(100% + 4rem);
        z-index: 0;
    }

    .group__name {
        color: var(--on-group-colour);
        display: block;
        font-size: 1.125rem;
        font-weight: 500;
        transform: translate(-2rem, -2rem);
        z-index: 10;
        top: .25rem;
        left: 1.25rem;
        position: absolute;
        text-wrap: nowrap;
    }

    .group:where([data-connect-left="true"])::before {
        border-bottom-left-radius: 0;
        border-top-left-radius: 0;
        margin-left: -3rem;
        width: calc(100% + 5rem);
    }
    .group:where([data-connect-right="true"])::before {
        border-bottom-right-radius: 0;
        border-top-right-radius: 0;
        margin-right: -3rem;
        width: calc(100% + 5rem);
    }
    .group:where([data-connect-left="true"][data-connect-right="true"])::before {
        width: calc(100% + 6rem);
    }

    .group:where([data-connect-top="true"])::after {
        border-top-left-radius: 0;
        border-top-right-radius: 0;
        margin-top: -4rem;
        height: calc(100% + 6rem);
    }
    .group:where([data-connect-bottom="true"])::after {
        border-bottom-left-radius: 0;
        border-bottom-right-radius: 0;
        margin-bottom: -4rem;
        height: calc(100% + 6rem);
    }
    .group:where([data-connect-top="true"][data-connect-bottom="true"])::after {
        height: calc(100% + 8rem);
    }

    .group__corner {
        aspect-ratio: 1 / 1;
        background-image: radial-gradient(
                circle at 100% 100%,
                transparent 1.5rem,
                var(--group-colour) calc(1.5rem + 1px)
        );
        content: "";
        position: absolute;
        width: 1.5rem;
    }
    .group__corner:where([data-corner-vertical="top"][data-corner-horizontal="left"]) {
        left: -3.5rem;
        top: -3.5rem;
        transform: rotate(180deg);
    }
    .group__corner:where([data-corner-vertical="top"][data-corner-horizontal="right"]) {
        right: -3.5rem;
        top: -3.5rem;
        transform: rotate(270deg);
    }
    .group__corner:where([data-corner-vertical="bottom"][data-corner-horizontal="right"]) {
        bottom: -3.5rem;
        right: -3.5rem;
        transform: rotate(0deg);
    }
    .group__corner:where([data-corner-vertical="bottom"][data-corner-horizontal="left"]) {
        bottom: -3.5rem;
        left: -3.5rem;
        transform: rotate(90deg);
    }

    .group:where([data-connect-top-left="true"]) .group__corner:where([data-corner-vertical="top"][data-corner-horizontal="left"]),
    .group:where([data-connect-top-right="true"]) .group__corner:where([data-corner-vertical="top"][data-corner-horizontal="right"]) {
        aspect-ratio: 1.5 / 2;
        background-color: var(--group-colour);
        top: -4rem;
        transform: none;
    }
    .group:where([data-connect-bottom-right="true"]) .group__corner:where([data-corner-vertical="bottom"][data-corner-horizontal="right"]),
    .group:where([data-connect-bottom-left="true"]) .group__corner:where([data-corner-vertical="bottom"][data-corner-horizontal="left"]) {
        aspect-ratio: 1.5 / 2;
        background-color: var(--group-colour);
        bottom: -4rem;
        transform: none;
    }
</style>