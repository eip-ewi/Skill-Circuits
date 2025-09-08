import type {BookmarkList} from "../dto/bookmark";
import type {SkillBlock} from "../dto/circuit/module/skill";
import type {ChoiceTaskItem, TaskInfo} from "../dto/circuit/module/task";

let bookmarks: BookmarkList[] = $state([]);

let bookmarkedSkillIds: Set<number> = $derived(new Set((bookmarks ?? []).flatMap(list => list.skills).map(skill => skill.id)));
let bookmarkedTaskInfoIds: Set<number> = $derived(new Set((bookmarks ?? []).flatMap(list => list.tasks).filter(task => task.taskType === "regular").map(task => task.infoId)));
let bookmarkedChoiceTaskIds: Set<number> = $derived(new Set((bookmarks ?? []).flatMap(list => list.tasks).filter(task => task.taskType === "choice").map(task => task.id)));

export async function fetchBookmarks() {
    const response = await fetch("/api/bookmarks");
    bookmarks = await response.json();
}

export function getBookmarks(): BookmarkList[] {
    return bookmarks;
}

export function isSkillBookmarked(skill: SkillBlock) {
    return bookmarkedSkillIds.has(skill.id);
}

export function isTaskInfoBookmarked(task: TaskInfo) {
    return bookmarkedTaskInfoIds.has(task.infoId);
}

export function isChoiceTaskBookmarked(task: ChoiceTaskItem) {
    return bookmarkedChoiceTaskIds.has(task.id);
}
