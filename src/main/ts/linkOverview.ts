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
 * Opens the link overlay and resets the filters.
 */
function openLinkOverlay(toggleOverlay: (id: string) => void) {
    // Reset filters
    $("#type-filter").val("All types").trigger("change");
    $("#link-searchbar").val("");

    // Show overlay
    toggleOverlay("link-overview-overlay");
}

/**
 * Triggers the 'submit' event on the link form.
 *
 * @param taskId The id of the task for which to submit the form.
 */
function submitForm(taskId: string) {
    $(`#link-form${taskId}`).trigger("submit");
}

/**
 * Sets the visibility of the module blocks on the link overview. If no (visible) links are in a block, the block
 * should be hidden, otherwise it should be shown.
 */
function setVisibilityModuleInfos() {
    $(".link__table").each(function () {
        // Number of visible rows
        const visibleRows = $(this)
            .children()
            .children()
            .filter(function () {
                const hidden = $(this).attr("hidden");

                return typeof hidden === "undefined" || !hidden;
            }).length;

        // First row is the "header" of the table, if no other rows are visible, hide the block
        if (visibleRows === 1) {
            $(this).parent().parent().attr("hidden", "true");
        } else {
            $(this).parent().parent().removeAttr("hidden");
        }
    });
}

/**
 * Creates a message element to inform the user of success or failure of a link change. It is shown in form
 * of a block with the content "(icon) message".
 *
 * @param formElement   The element to which the message will be appended.
 * @param message       The message to display.
 * @param icon          The icon (class) to show in the message block.
 */
function createMsg(formElement, message, icon) {
    const parentDiv: JQuery = $("<div/>", { class: "link__alert" });
    const iconElement: JQuery = $("<i/>", { class: icon });

    iconElement.appendTo(parentDiv);
    parentDiv.append(message);
    parentDiv.appendTo(formElement);

    // Display the message for 1.5 seconds
    setTimeout(function () {
        parentDiv.remove();
    }, 1500);
}

/**
 * Function to handle a link change. It sends the request to change the link in the backend.
 *
 * @param taskId        The task id for which the link is changed.
 * @param prevLink      The previous link.
 * @param newLink       The new link.
 */
function editLink(taskId: string, prevLink: string, newLink: string) {
    const linkInput: JQuery = $(`#link${taskId}`);

    // Only send request if the link is different
    if (prevLink !== newLink) {
        $.ajax({
            url: "/task/change-link",
            method: "PATCH",
            contentType: "application/json",
            data: JSON.stringify({
                taskId: taskId,
                newLink: newLink,
            }),
            success: () => {
                // If it was deleted (set to null), remove the row, otherwise show success message
                if (newLink === null) {
                    $(`#link-row${taskId}`).remove();
                    setVisibilityModuleInfos();
                } else {
                    // Update previous value if successful
                    $(`#prev-link${taskId}`).val(newLink);

                    createMsg(linkInput.parent(), "Success", "fa-regular fa-circle-check");
                }
            },
            error: () => {
                // Reset link field to previous value if unsuccessful
                linkInput.val(prevLink);

                createMsg(linkInput.parent(), "Error", "fa-sharp fa-regular fa-circle-xmark");
            },
        });
    }
}

/**
 * Handle the deletion of a link.
 *
 * @param event The button click event.
 */
function handleLinkDeletion(event: Event) {
    event.preventDefault();
    const target: HTMLButtonElement = event.target as HTMLButtonElement;
    const taskId: string = target.dataset.taskId;

    const newLink: string = null;
    const prevLink: string = $(`#prev-link${taskId}`).val() as string;
    editLink(taskId, prevLink, newLink);
}

/**
 * Handle updating a link.
 *
 * @param event The form submission event.
 */
function handleLinkSubmission(event: Event) {
    event.preventDefault();
    const target: HTMLButtonElement = event.target as HTMLButtonElement;
    const taskId: string = target.dataset.taskId;

    if (!target.checkValidity()) {
        const inputLink: HTMLFormElement = $(`#link${taskId}`)[0] as HTMLFormElement;
        inputLink.reportValidity();
    } else {
        const newLink: string = $(`#link${taskId}`).val() as string;
        const prevLink: string = $(`#prev-link${taskId}`).val() as string;
        editLink(taskId, prevLink, newLink);
    }
}

/**
 * Handle a search bar input event. Filters the links in the tables.
 */
function handleSearchBarInput() {
    const search: string = ($("#link-searchbar").val() as string).toLowerCase();

    $(".link_row").each(function () {
        const taskId: string = $(this).data("taskId");
        const inputLink: string = ($(`#link${taskId}`).val() as string).toLowerCase();

        // TODO check also type condition
        if (!inputLink.includes(search)) {
            $(this).attr("hidden", "true");
        } else {
            $(this).removeAttr("hidden");
        }
    });

    setVisibilityModuleInfos();
}

/**
 * Handle a type filter change event. Filters the links in the tables.
 */
function handleTypeSelectChange() {
    const typeFilter: JQuery = $("#type-filter");
    const selectedType: string = typeFilter.val() as string;

    // Update selection
    typeFilter.children().each(function () {
        if (($(this).val() as string) === selectedType) {
            $(this).attr("selected", "selected");
        } else {
            $(this).removeAttr("selected");
        }
    });

    // Filter tasks by type
    $(".link_row").each(function () {
        const taskId: string = $(this).data("taskId");
        const taskIcon: string = $(`#task-icon${taskId}`).attr("class");

        // TODO check also search condition
        if (!taskIcon.includes(selectedType) && selectedType !== "All types") {
            $(this).attr("hidden", "true");
        } else {
            $(this).removeAttr("hidden");
        }
    });

    // Update visibility
    setVisibilityModuleInfos();
}

/**
 * Adds the event listeners to elements on the link overview page.
 */
function linkOverviewEventListeners() {
    $(".link__input").on("submit", handleLinkSubmission);
    $(".link__delete").on("click", handleLinkDeletion);
    $("#link-searchbar").on("input", handleSearchBarInput);
    $("#type-filter").on("change", handleTypeSelectChange);
}

if (typeof module !== "undefined" && typeof module.exports !== "undefined") {
    module.exports.openLinkOverlay = openLinkOverlay;
    module.exports.setVisibilityModuleInfos = setVisibilityModuleInfos;
    module.exports.createMsg = createMsg;
    module.exports.editLink = editLink;
    module.exports.handleLinkDeletion = handleLinkDeletion;
    module.exports.handleLinkSubmission = handleLinkSubmission;
    module.exports.handleSearchBarInput = handleSearchBarInput;
    module.exports.handleTypeSelectChange = handleTypeSelectChange;
    module.exports.submitForm = submitForm;
    module.exports.linkOverviewEventListeners = linkOverviewEventListeners;
} else {
    // For the browser view, add the event listeners
    document.addEventListener("DOMContentLoaded", function () {
        linkOverviewEventListeners();
    });
}
