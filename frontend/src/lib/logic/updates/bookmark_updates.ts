import {withCsrf} from "../csrf";
import {getBookmarks} from "../bookmarks.svelte";
import type {SkillBlock} from "../../dto/circuit/module/skill";
import type {BookmarkList} from "../../dto/bookmark";
import type {ChoiceTaskItem, TaskInfo, TaskItem} from "../../dto/circuit/module/task";

export async function addBookmarkList() {
    let response = await fetch(`/api/bookmarks`, withCsrf({
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            name: "New list"
        }),
    }));

    if (response.ok) {
        let list: BookmarkList = await response.json();
        list.editing = true;
        getBookmarks().unshift(list);
    }
}

export async function editBookmarkListName(list: BookmarkList, newName: string) {
    let oldName = list.name;
    list.name = newName;

    let response = await fetch(`/api/bookmarks/${list.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            name: newName
        }),
    }));

    if (!response.ok) {
        list.name = oldName;
    }
}

export async function removeBookmarkList(list: BookmarkList) {
    let response = await fetch(`/api/bookmarks/${list.id}`, withCsrf({
        method: "DELETE",
    }));

    if (response.ok) {
        getBookmarks().splice(getBookmarks().findIndex(l => l.id === list.id)!, 1);
    }
}

export async function addSkillToBookmarkList(skill: SkillBlock, list: BookmarkList) {
    let response = await fetch(`/api/bookmarks/${list.id}/skills/${skill.id}`, withCsrf({
        method: "POST",
    }));

    if (response.ok) {
        list.skills.push(await response.json());
    }
}

export async function removeSkillFromBookmarkList(skill: SkillBlock, list: BookmarkList) {
    let removed = list.skills.splice(list.skills.findIndex(s => s.id === skill.id)!)[0]!;

    let response = await fetch(`/api/bookmarks/${list.id}/skills/${skill.id}`, withCsrf({
        method: "DELETE",
    }));

    if (!response.ok) {
        list.skills.push(removed);
    }
}

export async function addTaskInfoToBookmarkList(task: TaskInfo, list: BookmarkList) {
    let response = await fetch(`/api/bookmarks/${list.id}/tasks/${task.infoId}`, withCsrf({
        method: "POST",
    }));

    if (response.ok) {
        list.tasks.push(await response.json());
    }
}

export async function removeTaskInfoFromBookmarkList(task: TaskInfo, list: BookmarkList) {
    let removed = list.tasks.splice(list.tasks.findIndex(t => t.taskType === "regular" && t.infoId === task.infoId)!, 1)[0]!;

    let response = await fetch(`/api/bookmarks/${list.id}/tasks/${task.infoId}`, withCsrf({
        method: "DELETE",
    }));

    if (!response.ok) {
        list.tasks.push(removed);
    }
}

export async function addChoiceTaskToBookmarkList(task: ChoiceTaskItem, list: BookmarkList) {
    let response = await fetch(`/api/bookmarks/${list.id}/choice-tasks/${task.id}`, withCsrf({
        method: "POST",
    }));

    if (response.ok) {
        list.tasks.push(await response.json());
    }
}

export async function removeChoiceTaskFromBookmarkList(task: ChoiceTaskItem, list: BookmarkList) {
    let removed = list.tasks.splice(list.tasks.findIndex(t => t.taskType === "choice" && t.id === task.id)!, 1)[0]!;

    let response = await fetch(`/api/bookmarks/${list.id}/choice-tasks/${task.id}`, withCsrf({
        method: "DELETE",
    }));

    if (!response.ok) {
        list.tasks.push(removed);
    }
}

export function loadBookmarkListCollapsed(list: BookmarkList) {
    let storedExpandedBookmarks = localStorage.getItem("expandedBookmarks");
    let expandedBookmarks: number[];
    if (storedExpandedBookmarks === null) {
        expandedBookmarks = [];
        localStorage.setItem("expandedBookmarks", JSON.stringify([]));
    } else {
        expandedBookmarks = JSON.parse(storedExpandedBookmarks);
    }
    list.collapsed = !expandedBookmarks.includes(list.id);
}

export async function toggleBookmarkListCollapse(list: BookmarkList) {
    let expandedBookmarks: number[] = JSON.parse(localStorage.getItem("expandedBookmarks")!);
    list.collapsed = !list.collapsed;
    if (list.collapsed) {
        if (expandedBookmarks.includes(list.id)) {
            expandedBookmarks.splice(expandedBookmarks.indexOf(list.id)!, 1);
        }
    } else {
        expandedBookmarks.push(list.id);
    }
    localStorage.setItem("expandedBookmarks", JSON.stringify(expandedBookmarks));
}