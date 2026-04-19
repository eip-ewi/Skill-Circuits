<script lang="ts">
    import type {SkillBlock} from "../../dto/circuit/module/skill";
    import {getGroupForBlock, getItem, isBlockVisible} from "../../logic/circuit/circuit.svelte";
    import type {SubmoduleBlock} from "../../dto/circuit/edition/submodule";
    import type {SubmoduleGroup} from "../../dto/circuit/module/submodule";
    import {topologicalSort} from "../../logic/circuit/block_placement";
    import {BlockStates} from "../../data/block_state";
    import SkillNameComponent from "./SkillNameComponent.svelte";
    import SelectedSkillComponent from "./SelectedSkillComponent.svelte";
    import StudentTrayComponent from "../side_controls/student_tray/StudentTrayComponent.svelte";
    import {openExpandedBlockTransition} from "../../logic/transitions";
    import type {ModuleGroup} from "../../dto/circuit/edition/module";
    import type {Item} from "../../dto/circuit/item";
    import {isCompleted} from "../../logic/circuit/skill_state/completion";
    import {isUnlocked} from "../../logic/circuit/skill_state/unlock";
    import {untrack} from "svelte";

    let { submoduleBlock, open = $bindable() }: { submoduleBlock: SubmoduleBlock, open: boolean } = $props();

    let moduleGroup: ModuleGroup = $derived(getGroupForBlock(submoduleBlock) as ModuleGroup);
    let submoduleGroup: SubmoduleGroup = $derived(moduleGroup.moduleCircuit.groups.find(group => group.id === submoduleBlock.id)!);
    let skillBlockToSkillItem: [SkillBlock, Item][] = $derived(submoduleGroup.blocks.map(skillBlock => [skillBlock, getItem(skillBlock.id)]));

    let visibleSkills: SkillBlock[] | undefined = $state();
    let selectedSkill: SkillBlock | undefined = $state();

    let element: HTMLDialogElement | undefined = $state();

    function checkForClose(event: MouseEvent | KeyboardEvent) {
        if (event instanceof MouseEvent && event.target === element){
            open = false;
            return;
        }

        if (event instanceof KeyboardEvent && event.key === "Escape") {
            // prevent default behaviour of instantly closing the dialogue
            event.preventDefault();
            open = false;
        }
    }

    $effect(() => {
        if (element !== undefined && open) {
            element.showModal();
        }
    });

    $effect(() => {
        if (moduleGroup.moduleGraph !== undefined) {
            // Do not call this effect as soon as block visibilities change, by then the update of the moduleGraph
            // will not have propagated from the edition page yet. Only call this effect if the moduleGraph itself changed.

            untrack(() => {
                // Update the skills of the submodule, filtered by visibility and sorted topologically
                let visibleBlocks = submoduleGroup.blocks.filter(block => isBlockVisible(block));
                visibleSkills = topologicalSort(moduleGroup.moduleGraph!, visibleBlocks);
            });
        }
    });

    $effect(() => {
        // Select the first skill, if none is selected and there is at least one skill
        // Set it when the submodule is first opened: This makes it possible for recently revealed hidden skills
        // to be considered in the ordering
        if (visibleSkills !== undefined && open && selectedSkill === undefined && visibleSkills.length > 0) {
            selectedSkill = visibleSkills[0];
        }
    });

    $effect(() => {
        // Update completion and locked states for the skill items according to the state of the
        // skill blocks within the module graph

        skillBlockToSkillItem.forEach(skill => {
            const skillBlock = skill[0];
            const skillItem = skill[1];

            skillItem.completed = isCompleted(skillBlock, moduleGroup.moduleGraph);
            skillItem.locked = !isUnlocked(skillBlock, moduleGroup.moduleGraph);
        });
    });
</script>

{#if open}
    <!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_noninteractive_element_interactions -->
    <dialog bind:this={element} onclick={checkForClose} onkeydown={checkForClose}>
        <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
         <div class="expanded-submodule" transition:openExpandedBlockTransition={{ block: submoduleBlock }}>
             {#if visibleSkills !== undefined && moduleGroup.moduleGraph !== undefined}
                 <div class="content">
                     <h1 class="name">{submoduleBlock.name}</h1>
                     {#if selectedSkill !== undefined}
                         <div class="skill-list">
                             {#each visibleSkills as skill}
                                <SkillNameComponent block={skill} moduleGraph={moduleGroup.moduleGraph} bind:selectedSkill></SkillNameComponent>
                            {/each}
                        </div>
                         <div class="selected-skill-wrapper">
                             <SelectedSkillComponent block={selectedSkill}></SelectedSkillComponent>
                         </div>
                     {:else if visibleSkills.length === 0}
                        There are no skills in this submodule.
                     {/if}
                </div>
             {/if}
        </div>

         {#if visibleSkills !== undefined && selectedSkill !== undefined}
            <StudentTrayComponent block={selectedSkill}></StudentTrayComponent>
         {/if}
    </dialog>
{/if}

<style>
    .expanded-submodule {
        border: var(--expanded-block-border);
        border-radius: var(--expanded-block-border-radius);
        left: 0;
        outline: none;
        overflow: hidden;
        position: fixed;
        top: 0;
        transform: translate(calc(50vw - 50%), calc(50vh - 50%));
        transform-origin: top left;
    }

    dialog::backdrop {
        background-color: var(--expanded-block-background-colour);
        backdrop-filter: blur(var(--expanded-block-background-blur));
    }

    .content {
        --background-colour: var(--block-colour);
        background-color: var(--block-colour);
        box-shadow: 2rem 2rem 4rem color-mix(in srgb, var(--shadow-colour) 8%, transparent);
        color: var(--on-block-colour);
        font-size: var(--font-size-500);

        padding: 2em;
        max-height: calc(100vh - 10em);
        min-width: 40em;
        max-width: calc(100vw - 12em);
        overflow-y: auto;

        display: grid;
        grid-template-columns: fit-content(18em) minmax(25em, auto);
        grid-template-rows: auto 1fr;
        gap: 1em;
    }

    .name {
        font-size: var(--font-size-700);
        font-weight: 700;
        text-align: center;
        grid-column: span 2;
    }

    .skill-list {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        border-right: solid 0.08em var(--submodule-overview-line-colour);
        gap: 0.3em;
        font-size: var(--font-size-400);
        padding: 0 1em 0 0.3em;
        grid-row-start: 2;
        overflow: auto;
    }

    .selected-skill-wrapper {
        grid-row-start: 2;
        overflow: auto;
    }
</style>