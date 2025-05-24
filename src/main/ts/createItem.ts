/*
 * Skill Circuits
 * Copyright (C) 2025 - Delft University of Technology
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
 * Creates a new regular or choice task.
 *
 * @param button    The button that was clicked.
 * @param type      The type of task to add ("RegularTask" or "ChoiceTask").
 */
function createItem(button: JQuery, type: String): void {
    $(".item__create *").trigger("blur");

    const parentElement = button.closest(".item__create");
    const blockId: number = parentElement.data("block");
    const taskList = parentElement.siblings(".items").first();
    if (type === "RegularTask") {
        const elem = createRegularTask(blockId, createUniqueNewTaskIdPostfix(taskList));
        appendTask(taskList, elem);
        elem.find("input[name='time']").trigger("focus");
    } else if (type == "ChoiceTask") {
        const elem = createChoiceTask(blockId, taskList);
        appendTask(taskList, elem);
        elem.find("input[name='name']").trigger("focus");
    }
}

/**
 * Creates a new choice task, but does not yet add it to the task list.
 *
 * @param blockId   The id of the block the task is in.
 * @param taskList  The task list that the task will be added to.
 */
function createChoiceTask(blockId: number, taskList: JQuery): JQuery {
    // Create a new task
    const elem = $("#create-choice-task").clone(true);
    elem.removeClass("hidden");
    elem.children("input[name='skill.id']").val(blockId);

    // Create unique ids for the new task element for event handling
    const idPostfix: number = createUniqueNewTaskIdPostfix(taskList);
    elem.attr("id", `new-task-${blockId}-${idPostfix}`);

    // Add two regular tasks to the choice task
    const choiceTaskList = elem.find("ul");
    const regularTask1 = createRegularTask(blockId, idPostfix + 1);
    const regularTask2 = createRegularTask(blockId, idPostfix + 2);
    choiceTaskList.append(createTaskSeparation());
    choiceTaskList.append(regularTask1);
    choiceTaskList.append(createTaskSeparation());
    choiceTaskList.append(regularTask2);
    choiceTaskList.append(createTaskSeparation());

    return elem;
}

/**
 * Creates a new regular task, but does not yet add it to the task list.
 *
 * @param blockId       The id of the block the task is in.
 * @param idPostfix     The postfix for the task id, making it a unique new id.
 */
function createRegularTask(blockId: number, idPostfix: number): JQuery {
    // Create a new task
    const elem = $("#create-task").clone(true);
    elem.removeClass("hidden");
    elem.children("input[name='skill.id']").val(blockId);

    // Create a unique id for the new task element for event handling
    elem.attr("id", `new-task-${blockId}-${idPostfix}`);

    return elem;
}

/**
 * Appends a task and the necessary task separations to the given task list.
 *
 * @param taskList  The task list to which the task should be added.
 * @param task      The task that should be added.
 */
function appendTask(taskList: JQuery, task: JQuery): void {
    // Add the task and separation to the task list
    // Creating a task is always in edit mode, so the separation is necessary for drag and drop handling
    // If it is the first task, two separations need to be added
    if (taskList.find(".task:not(.hidden)").length === 0) {
        taskList.prepend(createTaskSeparation());
    }

    // As the <ul> has flex-direction: column-reverse we prepend the new task to have it at the end of the list
    taskList.prepend(task);
    taskList.prepend(createTaskSeparation());
}

/**
 * Creates a unique id postfix for the id of a new task. The tasks in the task list need to have the task class
 * and the data-new attribute set to true if the task was newly added.
 *
 * @param taskList  The task list.
 */
function createUniqueNewTaskIdPostfix(taskList: JQuery): number {
    // All new task ids are assigned a postfix, unique per skill
    // The new generated postfix will be the max. postfix + 1 (or 0, if none exist)
    let idPostfix: number = 0;
    taskList.find(".task[data-new=true]").each(function () {
        const split: string[] = this.id.split("-");
        const num: number = Number(split[split.length - 1]);
        if (num >= idPostfix) {
            idPostfix = num + 1;
        }
    });
    return idPostfix;
}

if (typeof module !== "undefined" && typeof module.exports !== "undefined") {
    module.exports.createUniqueNewTaskIdPostfix = createUniqueNewTaskIdPostfix;
    module.exports.appendTask = appendTask;
}
