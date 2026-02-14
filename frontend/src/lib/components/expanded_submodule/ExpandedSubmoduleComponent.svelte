<script lang="ts">
    import type {RegularSkillBlock, SkillBlock} from "../../dto/circuit/module/skill";
    import {isBlockVisible} from "../../logic/circuit/circuit.svelte";
    import type {SubmoduleBlock} from "../../dto/circuit/edition/submodule";
    import type {SubmoduleGroup} from "../../dto/circuit/module/submodule";
    import {Graph} from "../../logic/circuit/graph";
    import {topologicalSort} from "../../logic/circuit/block_placement";
    import {BlockStates} from "../../data/block_state";
    import SkillNameComponent from "./SkillNameComponent.svelte";
    import SelectedSkillComponent from "./SelectedSkillComponent.svelte";
    import StudentTrayComponent from "../side_controls/student_tray/StudentTrayComponent.svelte";
    import {openExpandedBlockTransition} from "../../logic/transitions";

    let { submoduleBlock, open = $bindable() }: { submoduleBlock: SubmoduleBlock, open: boolean } = $props();

    let submoduleGroup: SubmoduleGroup | undefined = $state();
    let skills: SkillBlock[] | undefined = $state();

    let selectedSkill: SkillBlock | undefined = $state();

    let element: HTMLDialogElement | undefined = $state();

    function checkForClose(event: MouseEvent) {
        if (event.target === element) {
            submoduleBlock.state = BlockStates.Inactive;
            open = false;
        }
    }

    async function fetchSubmoduleData() {
        const response = await fetch(`/api/submodules/${submoduleBlock.id}`);
        submoduleGroup = await response.json();
    }

    $effect(() => {
        if (element === undefined) {
            return;
        }
        if (open) {
            if (skills === undefined) {
                fetchSubmoduleData();
            }
            element.showModal();
        }
    });

    function setSubmoduleSkills() {
        let visibleBlocks = submoduleGroup!.blocks.filter(block => isBlockVisible(block));
        let graph = new Graph(visibleBlocks);

        // TODO: check if function has desired behavior for this component
        // TODO: use ModuleGraph here instead
        skills = topologicalSort(graph, visibleBlocks);
    }

    $effect(() => {
        // Update the skills of the submodule, filtered by visibility
        if (submoduleGroup !== undefined) {
            setSubmoduleSkills();
        }
    });

    $effect(() => {
        // Select the first skill, if none is selected and there is at least one skill
        if (skills !== undefined && selectedSkill === undefined && skills.length > 0) {
            selectedSkill = skills[0];
        }
    });
</script>

{#if open}
    <!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_noninteractive_element_interactions -->
    <dialog bind:this={element} onclick={checkForClose}>
        <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->
         <div class="expanded-submodule" transition:openExpandedBlockTransition={{ block: submoduleBlock }}>
             {#if skills !== undefined}
                 <div class="content">
                     <h1 class="name">{submoduleBlock.name}</h1>
                     {#if selectedSkill !== undefined}
                         <div class="wrapper">
                             <div class="skill-list">
                                 {#each skills as skill}
                                     <!-- TODO: handle scrolling correctly -->
                                    <SkillNameComponent block={skill} bind:selectedSkill></SkillNameComponent>
                                {/each}
                            </div>
                            <SelectedSkillComponent block={selectedSkill}></SelectedSkillComponent>
                        </div>
                     {:else if skills.length === 0}
                        There are no skills in this submodule.
                     {/if}
                </div>
             {/if}
        </div>

         {#if skills !== undefined && selectedSkill !== undefined}
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
        display: grid;
        gap: 1em;
        min-width: 40em;
        max-height: calc(100vh - 12em);
        max-width: calc(100vw - 12em);
        overflow-y: auto;
        padding: 2em;
        place-items: center;

        font-size: var(--font-size-500);
    }

    .name {
        font-size: var(--font-size-700);
        font-weight: 700;
        text-align: center;
    }

    .wrapper {
        display: flex;
        flex-direction: row;
        width: 100%;
    }

    .skill-list {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        padding: 0.4em 1em 0.4em 0;
        border-right: solid 0.08em var(--submodule-overview-line-colour);
        gap: 0.3em;
        font-size: var(--font-size-400);
    }
</style>