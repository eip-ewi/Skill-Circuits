<script lang="ts">

    import type {Snippet} from "svelte";
    import Button from "./Button.svelte";

    let { button, children, icon, action, onconfirm }: { button: Snippet<[ () => void ]>, children: Snippet, icon: string, action: string, onconfirm: () => void } = $props();

    let dialog: HTMLDialogElement;

</script>

{@render button(() => dialog.showModal()) }

<dialog bind:this={dialog} class="glass dialog">
    <div class="content">
        {@render children()}

        <div class="buttons">
            <Button primary onclick={ () => dialog.close() }>
                <span class="fa-solid fa-xmark"></span>
                <span>Cancel</span>
            </Button>
            <Button primary type="caution" onclick={ () => { onconfirm(); dialog.close(); } }>
                <span class={icon}></span>
                <span>{action}</span>
            </Button>
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

    .content {
        display: grid;
        gap: 2em;
        padding: 2em;
    }

    .buttons {
        display: flex;
        justify-content: space-between;
    }
</style>