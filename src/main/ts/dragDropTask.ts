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
    if (event.target instanceof Element && event.target.classList.contains("task")) {
        event.dataTransfer.dropEffect = "move";
        $(event.target).css("opacity", "0.5");
        $("#circuit").attr("data-moving", "task");
        event.dataTransfer.setData("text/plain", event.target.id);
    }
}

/**
 * Handles the dragend event for dragging and dropping tasks.
 *
 * @param event The dragend DragEvent.
 */
function handleTaskDragEnd(event: DragEvent): void {
    if (event.target instanceof Element && event.target.classList.contains("task")) {
        $(event.target).css("opacity", "");
        $("#circuit").removeAttr("data-moving");
    }
}

/**
 * Handles the dragenter event for dragging and dropping tasks.
 *
 * @param event The dragenter DragEvent.
 */
function handleTaskDragEnter(event: DragEvent): void {
    if (
        event.target instanceof Element &&
        event.target.classList.contains("item__separation") &&
        $("#circuit").attr("data-moving") === "task"
    ) {
        $(event.target).addClass("drag_over");
    }
}

/**
 * Handles the dragleave event for dragging and dropping tasks.
 *
 * @param event The dragleave DragEvent.
 */
function handleTaskDragLeave(event: DragEvent): void {
    if (
        event.target instanceof Element &&
        event.target.classList.contains("item__separation") &&
        $("#circuit").attr("data-moving") === "task"
    ) {
        event.target.classList.remove("drag_over");
    }
}

/**
 * Handles the dragover event for dragging and dropping tasks.
 *
 * @param event The dragover DragEvent.
 */
function handleTaskDragOver(event: DragEvent): void {
    if (
        event.target instanceof Element &&
        event.target.classList.contains("item__separation") &&
        $("#circuit").attr("data-moving") === "task"
    ) {
        event.preventDefault();
        event.dataTransfer.dropEffect = "move";
    }
}

/**
 * Handles the drop event for dragging and dropping tasks.
 *
 * @param event The drop DragEvent.
 */
function handleTaskDrop(event: DragEvent): void {
    if (
        event.target instanceof Element &&
        event.target.classList.contains("item__separation") &&
        $("#circuit").attr("data-moving") === "task"
    ) {
        event.preventDefault();

        const task = $(`#${event.dataTransfer.getData("text/plain")}`);
        const sepBeforeTask = task.next(".item__separation");
        const targetSep = $(event.target);
        const taskBeforeTarget = targetSep.next(".task");

        const idxPrev: number = task.data("index");
        const idxUpd: number =
            taskBeforeTarget.length === 0 ? 0 : taskBeforeTarget.data("index") + 1;

        if (idxPrev !== idxUpd) {
            // Move task and one task separation element
            targetSep.before(task);
            task.before(sepBeforeTask);

            // Update indices
            const lowerIdx: number = idxPrev < idxUpd ? idxPrev : idxUpd;
            const upperIdx: number = idxPrev < idxUpd ? idxUpd : idxPrev;
            const addOrSubtract: 1 | -1 = idxPrev < idxUpd ? -1 : 1;
            task.data("index", idxUpd);
            task.siblings(".task").each(function () {
                const idx = $(this).data("index");
                if (idx >= lowerIdx && idx <= upperIdx) {
                    $(this).data("index", idx + addOrSubtract);
                }
            });
        }

        // Reset styling
        task.css("opacity", "1");
        targetSep.removeClass("drag_over");
        $("#circuit").removeAttr("data-moving");
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
}

if (typeof module === "undefined" || typeof module.exports === "undefined") {
    // For the browser view, add the event listeners
    document.addEventListener("DOMContentLoaded", function () {
        dragDropTaskEventListeners();
    });
}
