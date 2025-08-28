<script lang="ts">
    import {cubicInOut} from "svelte/easing";
    import {type BlockAction, BlockActions} from "../../../data/block_action";
    import type {Block} from "../../../dto/circuit/block";
    import type {SkillBlock} from "../../../dto/circuit/module/skill";
    import {getBookmarks, isSkillBookmarked, isTaskInfoBookmarked} from "../../../logic/bookmarks.svelte";
    import type {BookmarkList} from "../../../dto/bookmark";
    import {addSkillToBookmarkList, addTaskInfoToBookmarkList, removeSkillFromBookmarkList, removeTaskInfoFromBookmarkList} from "../../../logic/updates/bookmark_updates";
    import BookmarkMenuComponent from "../../bookmark/BookmarkMenuComponent.svelte";
    import Button from "../../util/Button.svelte";

    let { skill, action = $bindable() }: { skill: SkillBlock, action: BlockAction | undefined } = $props();

    let open: boolean = $state(false);

    function transition(element: Element) {
        return {
            duration: 100,
            easing: cubicInOut,
            css: (t: number) => `
                transform: scale(${t});
            `,
        };
    }
</script>

<div class="bookmarks" transition:transition>
    <BookmarkMenuComponent bind:open={open} onLists={getBookmarks().filter(list => list.skills.some(s => s.id === skill.id))}
                           addToList={ list => addSkillToBookmarkList(skill, list) } removeFromList={ list => removeSkillFromBookmarkList(skill, list) }>
        <Button square aria-label="Bookmark" onclick={ () => open = true } onmouseenter={ () => action = BlockActions.Bookmark } onmouseleave={ () => action = undefined }>
            <span class="fa-bookmark" class:fa-regular={!isSkillBookmarked(skill)} class:fa-solid={isSkillBookmarked(skill)}></span>
        </Button>
    </BookmarkMenuComponent>
</div>

<style>
    .bookmarks {
        position: absolute;
        right: -.5em;
        top: -.5em;
        transform-origin: bottom left;
    }
</style>