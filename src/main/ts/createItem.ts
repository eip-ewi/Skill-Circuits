/*
 * Skill Circuits
 * Copyright (C) 2022 - Delft University of Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Creates a new task separation, cloned from the reference element.
 */
function createTaskSeparation(): JQuery {
    const separation = $("#task-separation").clone(true);
    separation.removeAttr("id");
    separation.removeClass("hidden");
    return separation;
}

/**
 * Creates a new item.
 */
function createItem(): void {
    const button = $(this);
    const blockId = button.data("block");

    // Create a new task
    const elem = $("#create-task").clone(true);
    elem.removeClass("hidden");
    elem.children("input[type='hidden']").val(blockId);

    // Create a unique id for the new task element for event handling
    const taskList = button.closest("ul").find("ul").first();
    elem.attr("id", createUniqueNewTaskId(blockId, taskList));

    // Add the task and separation to the task list
    // Creating a task is always in edit mode, so the separation is necessary for drag and drop handling
    // If it is the first task, two separations need to be added
    if (taskList.find(".item").length === 0) {
        taskList.prepend(createTaskSeparation());
    }
    // As the <ul> has flex-direction: column-reverse we prepend the new task to have it at the end of the list
    taskList.prepend(elem);
    taskList.prepend(createTaskSeparation());
    elem.find("input[name='time']").trigger("focus");
}

/**
 * Creates a unique id for the id of the new task. The tasks in the task list need to have the task class
 * and the data-new attribute set to true if the task was newly added.
 *
 * @param blockId   The id of the skill, to make the task id unique over all skills.
 * @param taskList  The element containing the list of tasks in the skill.
 */
function createUniqueNewTaskId(blockId: number, taskList: JQuery): string {
    // All new task ids are assigned a suffix, unique per skill
    // The new generated suffix will be the max. suffix + 1 (or 0, if none exist)
    let idSuffix: number = 0;
    taskList.children(".task[data-new=true]").each(function () {
        const split: string[] = this.id.split("-");
        const num: number = Number(split[split.length - 1]);
        if (num >= idSuffix) {
            idSuffix = num + 1;
        }
    });
    return `new-task-${blockId}-${idSuffix}`;
}

/**
 * Adds the event listeners to elements for the creation of items.
 */
function createItemEventListeners(): void {
    $(".item__create").on("click", createItem);
}

if (typeof module !== "undefined" && typeof module.exports !== "undefined") {
    module.exports.createUniqueNewTaskId = createUniqueNewTaskId;
} else {
    // For the browser view, add the event listeners
    document.addEventListener("DOMContentLoaded", function () {
        createItemEventListeners();
    });
}
