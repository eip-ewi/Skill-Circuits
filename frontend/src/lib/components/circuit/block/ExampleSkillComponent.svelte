<script lang="ts">
    import {TaskIcons} from "../../../dto/task_icons";

    let { locked }: { locked : boolean } = $props();
</script>

<div class="block-wrapper">
    <div class="block" data-locked={locked}>
        <div class="heading">
            <span class="name">Example Skill</span>
        </div>

        <div class="tasks">
            <span class="task fa-solid fa-{TaskIcons.VIDEO}"></span>
            <span class="task fa-solid fa-{TaskIcons.READING}"></span>
            <span class="task fa-solid fa-{TaskIcons.EXERCISE}"></span>
        </div>
    </div>
</div>

<style>
    .block-wrapper {
        position: relative;
        transition: transform ease-in-out 150ms;

        width: 12em;
        margin: 1em;
        left: -1em;
    }

    .block {
        background-color: var(--block-colour);
        border: var(--block-border);
        border-radius: var(--block-border-radius);
        box-shadow: .75rem 1.25rem 1.625rem 0 color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        display: flex;
        flex-direction: column;
        gap: .5em;
        padding: 1em;
        position: relative;
        transition: filter ease-in-out 150ms, box-shadow ease-in-out 150ms;
    }

    .block[data-locked="true"] {
        filter: blur(.375em);
    }

    .heading {
        display: flex;
        gap: 0;
        flex-direction: column;
    }

    .name {
        font-size: 1.25em;
        font-weight: 700;
    }

    .tasks {
        display: flex;
        gap: .25em;
    }
</style>