<script lang="ts">
    import type {SkillBlock} from "../../dto/circuit/module/skill";
    import {isBlockVisible} from "../../logic/circuit/circuit.svelte";
    import type {SubmoduleBlock} from "../../dto/circuit/edition/submodule";
    import type {SubmoduleGroup} from "../../dto/circuit/module/submodule";
    import {Graph} from "../../logic/circuit/graph";
    import {topologicalSort} from "../../logic/circuit/block_placement";
    import {cubicInOut, linear} from "svelte/easing";
    import type {Point} from "../../data/point";
    import {BlockStates} from "../../data/block_state";

    let { submoduleBlock, open = $bindable() }: { submoduleBlock: SubmoduleBlock, open: boolean } = $props();
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
        const submoduleGroup: SubmoduleGroup = await response.json();

        let graph = new Graph(submoduleGroup.blocks.filter(block => isBlockVisible(block)));
        // TODO: check if function has desired behavior for this component
        skills = topologicalSort(graph, submoduleGroup.blocks);

        // Select the first skill, if there is at least one
        if (skills.length > 0) {
            selectedSkill = skills[0];
        }
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

    // TODO: move duplicated code
    function transition(element: Element) {
        return {
            duration: 300,
            easing: linear,
            css: (t: number) => {
                let t3 = cubicInOut(t);
                let start: Point = {
                    x: submoduleBlock.boundingRect!().left + submoduleBlock.boundingRect!().width / 2,
                    y: submoduleBlock.boundingRect!().top + submoduleBlock.boundingRect!().height / 2,
                };
                let position = {
                    x: `calc(${start.x * (1 - t3)}px + ${50 * t3}vw - ${50 * t3}%)`,
                    y: `calc(${start.y * (1 - t3)}px + ${50 * t3}vh - ${50 * t3}%)`,
                }
                return `
                   --blur: ${t * 0.5}rem;
                   opacity: ${t3};
                   transform: translate(${position.x}, ${position.y}) scale(${t3 * 0.9 + 0.1}) ;
                `;
            }
        }
    }
</script>

{#if open}
    <!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_noninteractive_element_interactions -->
    <dialog bind:this={element} onclick={checkForClose}>
        {#if skills !== undefined}
            <!-- svelte-ignore a11y_no_static_element_interactions, a11y_click_events_have_key_events -->

            <div class="expanded-block" transition:transition>
                <!-- TODO drag and drop -->
                <div class="content">
                    <h1 class="name">{submoduleBlock.name}</h1>
                    <div class="wrapper">
                        <div class="skill-list">
                            {#each skills as skill}
                                <span>{skill.name}</span>
                            {/each}
                        </div>
                        <div class="selected-skill-container">
                            <h2>{selectedSkill.name}</h2>
                        </div>
                    </div>
                </div>
            </div>
            <!-- TODO: link tray to active skill <StudentTrayComponent {block}></StudentTrayComponent> -->
        {/if}
    </dialog>
{/if}

<style>
    .expanded-block {
        font-size: clamp(.75rem, calc(16 / 1732 * 100vw), 1rem);

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
        max-height: calc(100vh - 12em);
        overflow-y: auto;
        padding: 2em;
        place-items: center;
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
        padding-right: 2em;
        border-right: solid 0.25em white;
    }

    .selected-skill-container {
        margin-left: 2em;
    }
</style>