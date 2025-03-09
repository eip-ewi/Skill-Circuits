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
 * Handles the dragstart event for dragging and dropping tasks.
 *
 * @param event The dragstart DragEvent.
 */
function handleTaskDragStart(event: DragEvent): void {
    if (event.target instanceof HTMLElement && event.target.classList.contains("task")) {
        event.dataTransfer.dropEffect = "move";
        $(event.target).css("opacity", "0.5");

        let taskType = "task";
        if ($(event.target).closest(".task").hasClass("choice_task")) {
            taskType = "choice task";
        }
        $("#circuit").attr("data-moving", taskType);

        event.dataTransfer.setData("text/plain", event.target.id);
    }
}

/**
 * Handles the dragend event for dragging and dropping tasks.
 *
 * @param event The dragend DragEvent.
 */
function handleTaskDragEnd(event: DragEvent): void {
    if (event.target instanceof HTMLElement && event.target.classList.contains("task")) {
        $(event.target).css("opacity", "");
        $("#circuit").removeAttr("data-moving");
        event.target.draggable = false;
    }
}

/**
 * Handles the dragenter event for dragging and dropping tasks.
 *
 * @param event The dragenter DragEvent.
 */
function handleTaskDragEnter(event: DragEvent): void {
    if (
        event.target instanceof HTMLElement &&
        event.target.classList.contains("item__separation")
    ) {
        const taskCondition = $("#circuit").attr("data-moving") === "task";
        const choiceTaskCondition =
            $("#circuit").attr("data-moving") === "choice task" &&
            $(event.target).closest(".choice_task").length === 0;
        if (taskCondition || choiceTaskCondition) {
            $(event.target).addClass("drag_over");
        }
    }
}

/**
 * Handles the dragleave event for dragging and dropping tasks.
 *
 * @param event The dragleave DragEvent.
 */
function handleTaskDragLeave(event: DragEvent): void {
    if (
        event.target instanceof HTMLElement &&
        event.target.classList.contains("item__separation")
    ) {
        const taskCondition = $("#circuit").attr("data-moving") === "task";
        const choiceTaskCondition =
            $("#circuit").attr("data-moving") === "choice task" &&
            $(event.target).closest(".choice_task").length === 0;
        if (taskCondition || choiceTaskCondition) {
            event.target.classList.remove("drag_over");
        }
    }
}

/**
 * Handles the dragover event for dragging and dropping tasks.
 *
 * @param event The dragover DragEvent.
 */
function handleTaskDragOver(event: DragEvent): void {
    if (
        event.target instanceof HTMLElement &&
        event.target.classList.contains("item__separation")
    ) {
        const taskCondition = $("#circuit").attr("data-moving") === "task";
        const choiceTaskCondition =
            $("#circuit").attr("data-moving") === "choice task" &&
            $(event.target).closest(".choice_task").length === 0;
        if (taskCondition || choiceTaskCondition) {
            event.preventDefault();
            event.dataTransfer.dropEffect = "move";
        }
    }
}

/**
 * Handles the drop event for dragging and dropping tasks.
 *
 * @param event The drop DragEvent.
 */
function handleTaskDrop(event: DragEvent): void {
    if (
        event.target instanceof HTMLElement &&
        event.target.classList.contains("item__separation") &&
        ($("#circuit").attr("data-moving") === "task" ||
            $("#circuit").attr("data-moving") === "choice task")
    ) {
        event.preventDefault();

        // Move task and one task separation element
        const id = event.dataTransfer.getData("text/plain");
        const task = $(`#${id}`);
        const targetSep = $(event.target);
        // If it is a neighboring separator do not move anything
        if (
            targetSep.next(".task").attr("id") !== id &&
            targetSep.prev(".task").attr("id") !== id
        ) {
            // Move a task separation with the task
            const sepBeforeTask = task.next(".item__separation");
            targetSep.before(task);
            task.before(sepBeforeTask);
        }

        // Reset styling
        task.css("opacity", "1");
        targetSep.removeClass("drag_over");
        $("#circuit").removeAttr("data-moving");
        task.attr("draggable", "false");
    }
}

/**
 * Handles the mousedown event for dragging and dropping tasks, so that tasks are only draggable from the drag handle.
 *
 * @param event The mousedown event.
 */
function handleTaskMouseDown(event: Event): void {
    if (event.target instanceof HTMLElement && event.target.classList.contains("item__move")) {
        const parentElem = $(event.target).closest(".task");
        parentElem.attr("draggable", "true");
    }
}

/**
 * Handles the mouseup event for dragging and dropping tasks, so that tasks are only draggable from the drag handle.
 *
 * @param event The mouseup event.
 */
function handleTaskMouseUp(event: Event): void {
    if (event.target instanceof HTMLElement && event.target.classList.contains("item__move")) {
        const parentElem = $(event.target).closest(".task");
        parentElem.attr("draggable", "false");
    }
}

/**
 * Adds the event listeners to elements for the dragging and dropping of tasks.
 */
function dragDropTaskEventListeners(): void {
    document.addEventListener("dragstart", handleTaskDragStart, false);
    document.addEventListener("dragend", handleTaskDragEnd, false);
    document.addEventListener("dragenter", handleTaskDragEnter, false);
    document.addEventListener("dragleave", handleTaskDragLeave, false);
    document.addEventListener("dragover", handleTaskDragOver, false);
    document.addEventListener("drop", handleTaskDrop, false);
    document.addEventListener("mousedown", handleTaskMouseDown, false);
    document.addEventListener("mouseup", handleTaskMouseUp, false);
}

if (typeof module === "undefined" || typeof module.exports === "undefined") {
    // For the browser view, add the event listeners
    document.addEventListener("DOMContentLoaded", function () {
        dragDropTaskEventListeners();
    });
}
