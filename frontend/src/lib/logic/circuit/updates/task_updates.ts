import {withCsrf} from "../../csrf";
import type {ChoiceTaskChoice, ChoiceTaskItem, RegularTaskItem, TaskInfo, TaskItem} from "../../../dto/circuit/module/task";
import type {SkillBlock} from "../../../dto/circuit/module/skill";
import type {Block} from "../../../dto/circuit/block";
import {getLevel} from "../level.svelte";
import type {Item} from "../../../dto/circuit/item";
import {getBlockForItem, getBlocks} from "../circuit.svelte";
import {getBookmarks} from "../../bookmarks.svelte";
import {addRevealedSkills} from "../unlocked_skills.svelte";

export async function toggleTaskCompletion(task: TaskInfo) {
    task.completed = !task.completed;
    let response = await fetch(`/api/task-info/${task.infoId}/complete?completed=${task.completed}`, withCsrf({
        method: "POST",
    }));
    if (response.ok) {
        getBlocks().filter(block => block.blockType === "skill").flatMap(block => block.items)
            .flatMap(t => t.taskType === "choice" ? t.tasks : [t as TaskInfo])
            .filter(t => t.infoId === task.infoId)
            .forEach(i => i.completed = task.completed);
        getBookmarks().flatMap(list => list.tasks)
            .flatMap(t => t.taskType === "choice" ? t.tasks : [t as TaskInfo])
            .filter(t => t.infoId === task.infoId)
            .forEach(i => i.completed = task.completed);
        let body: { revealedSkills: number[] } = await response.json();
        addRevealedSkills(body.revealedSkills);
    } else {
        task.completed = !task.completed;
    }
}

export async function reportClickedLink(task: TaskInfo) {
    await fetch(`/api/task-info/${task.infoId}/click`, withCsrf({
        method: "POST",
    }));
}

export async function createChoiceTask(skill: SkillBlock) {
    let response = await fetch(`/api/tasks/choice`, withCsrf({
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            skill: {
                id: skill.id,
            }
        }),
    }));

    if (response.ok) {
        let newTask: ChoiceTaskItem = await response.json();
        skill.items.push(newTask);
    }
}

export async function editTaskInfoName(task: TaskInfo, newName: string) {
    let oldName = task.name;
    task.name = newName;

    let response = await fetch(`/api/task-info/${task.infoId}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            name: newName,
        }),
    }));

    if (!response.ok) {
        task.link = oldName;
    }
}

export async function editTaskType(task: TaskInfo, newType: string) {
    let oldType = task.type;
    task.type = newType;

    let response = await fetch(`/api/task-info/${task.infoId}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            type: newType,
        }),
    }));

    if (!response.ok) {
        task.type = oldType;
    }
}

export async function editTaskTime(task: TaskInfo, newTime: number) {
    let oldTime = task.time;
    task.time = newTime;

    let response = await fetch(`/api/task-info/${task.infoId}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            time: newTime,
        }),
    }));

    if (!response.ok) {
        task.time = oldTime;
    }
}

export async function editTaskLink(task: TaskInfo, newLink: string) {
    let oldLink = task.link;
    task.link = newLink === "" ? null : newLink;

    let response = await fetch(`/api/task-info/${task.infoId}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            link: newLink,
        }),
    }));

    if (!response.ok) {
        task.link = oldLink;
    }
}

export async function editTaskPaths(task: TaskItem, newPaths: number[]) {
    let oldPaths = task.paths;
    task.paths = newPaths;

    let response = await fetch(`/api/tasks/${task.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            paths: newPaths.map(pathId => { return {
                id: pathId
            }; }),
        }),
    }));

    if (!response.ok) {
        task.paths = oldPaths;
    }
}

export async function editTaskIndex(task: TaskItem, newIndex: number, tasks: TaskItem[]) {
    let oldIndex = tasks.findIndex(t => t.id === task.id)!;
    tasks.splice(oldIndex, 1);
    tasks.splice(newIndex, 0, task);

    let response = await fetch(`/api/tasks/${task.id}/index?index=${newIndex}`, withCsrf({
        method: "PATCH"
    }));

    if (!response.ok) {
        tasks.splice(newIndex, 1);
        tasks.splice(oldIndex, 0, task);
    }
}

export async function moveTask(task: TaskItem, newSkill: SkillBlock, newIndex: number, oldSkill: SkillBlock) {
    let oldIndex = oldSkill.items.findIndex(t => t.id === task.id)!;
    oldSkill.items.splice(oldIndex, 1);
    newSkill.items.splice(newIndex, 0, task);

    let response = await fetch(`/api/tasks/${task.id}/skill`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            skill: {
                id: newSkill.id,
            },
            index: newIndex,
        })
    }));

    if (!response.ok) {
        newSkill.items.splice(newIndex, 1);
        oldSkill.items.splice(oldIndex, 0, task);
    }
}

export async function editChoiceTaskMinTasks(task: ChoiceTaskItem, newMinTasks: number) {
    let oldMinTasks = task.minTasks;
    task.minTasks = newMinTasks;

    let response = await fetch(`/api/tasks/choice/${task.id}`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            minTasks: newMinTasks,
        }),
    }));

    if (!response.ok) {
        task.minTasks = oldMinTasks;
    }
}

export async function moveTaskInsideOfChoiceTask(choiceTask: ChoiceTaskItem, subtask: RegularTaskItem) {
    let oldSkill = getBlockForItem(subtask) as SkillBlock;
    let oldIndex = oldSkill.items.findIndex(t => t.id === subtask.id)!;
    oldSkill.items.splice(oldIndex, 1);

    let response = await fetch(`/api/tasks/${choiceTask.id}/add-subtask/${subtask.id}`, withCsrf({
        method: "POST",
    }));

    if (response.ok) {
        choiceTask.tasks.push(await response.json());
    } else {
        oldSkill.items.splice(oldIndex, 0, subtask);
    }
}

export async function moveTaskOutsideOfChoiceTask(choiceTask: ChoiceTaskItem, subtask: ChoiceTaskChoice, newIndex: number, newSkill: SkillBlock) {
    let response = await fetch(`/api/task-info/${subtask.infoId}/skill`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            skill: {
                id: newSkill.id,
            },
            index: newIndex,
        })
    }));

    if (response.ok) {
        // Remove from old choice task
        let oldIndex = choiceTask.tasks.findIndex(t => t.infoId === subtask.infoId)!;
        choiceTask.tasks.splice(oldIndex, 1);

        // Add to new skill
        let newTask: RegularTaskItem = await response.json();
        newSkill.items.splice(newIndex, 0, newTask);
    }
}

export async function moveSubtask(subtask: ChoiceTaskChoice, newChoiceTask: ChoiceTaskItem, oldChoiceTask: ChoiceTaskItem) {
    oldChoiceTask.tasks.splice(oldChoiceTask.tasks.findIndex(t => t.infoId === subtask.infoId)!, 1);
    newChoiceTask.tasks.push(subtask);

    let response = await fetch(`/api/task-info/${subtask.infoId}/task`, withCsrf({
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            choiceTask: {
                id: newChoiceTask.id,
            },
        })
    }));

    if (!response.ok) {
        newChoiceTask.tasks.pop();
        oldChoiceTask.tasks.push(subtask);
    }
}

export async function deleteSubtask(subtask: ChoiceTaskChoice) {
    let response = await fetch(`/api/task-info/${subtask.infoId}`, withCsrf({
        method: "DELETE",
    }));

    if (response.ok) {
        let choiceTask = getBlocks()
            .flatMap(block => block.items.filter(item => item.itemType === "task" && item.taskType === "choice"))
            .find(choiceTask => choiceTask.tasks.some(info => info.infoId === subtask.infoId))!;
        choiceTask.tasks.splice(choiceTask.tasks.findIndex(i => i.infoId === subtask.infoId), 1);
    }
}
