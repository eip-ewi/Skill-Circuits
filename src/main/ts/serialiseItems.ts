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
 * Serialise items of a given block.
 *
 * @param block The block for which to serialise the items.
 */
function serialiseBlockItems(block: JQuery) {
    const directChildItems = block.children(".items").children(".items").children(".item, .task");
    const notDeletedItems = directChildItems.filter(function () {
        return !$(this).data("delete");
    });
    return directChildItems
        .map((_, item) => {
            const index = notDeletedItems.length - 1 - notDeletedItems.index(item);
            return serialiseItem($(item), index);
        })
        .toArray();
}

/**
 * Serialise an item.
 *
 * @param item      The item to serialise.
 * @param index     The index that should be assigned to the item.
 */
function serialiseItem(item: JQuery, index: number) {
    // choice tasks need to be handled differently
    const type = item.children("input[name='taskType']");
    if (type.val() === "ChoiceTask") {
        return serialiseChoiceTask(item, index);
    }

    const data = {};
    item.find("input, select").each((_, input) => {
        serialiseInput($(input), data);
    });
    data["new"] = item.data("new") === true;
    data["delete"] = item.data("delete") === true;
    data["index"] = index;
    return data;
}

/**
 * Serialise a choice task.
 *
 * @param item      The choice task to serialise.
 * @param index     The index that should be assigned to the item.
 */
function serialiseChoiceTask(item: JQuery, index: number) {
    const data = {};
    // serialise direct children (hidden fields)
    item.children("input").each((_, input) => {
        serialiseInput($(input), data);
    });
    // serialise header and footer
    item.find(".choice_task__header, .choice_task__footer")
        .find("input, select")
        .each((_, input) => {
            serialiseInput($(input), data);
        });
    data["new"] = item.data("new") === true;
    data["delete"] = item.data("delete") === true;
    data["index"] = index;

    // serialise subtasks
    const subTasks = item.find(".task");
    const notDeletedItems = subTasks.filter(function () {
        return !$(this).data("delete");
    });
    const subTaskData = subTasks
        .map((_, item) => {
            // TODO: also reverse indices?
            const index = notDeletedItems.index(item);
            return serialiseItem($(item), index);
        })
        .toArray();

    data["updatedSubTasks"] = subTaskData.filter(item => !item["new"] && !item["delete"]);
    data["newSubTasks"] = subTaskData.filter(item => item["new"]);

    return data;
}

/**
 * Serialise an input element. Adds a key-value pair to a given data object
 * that corresponds to the name and value of the input element.
 *
 * @param input     The input element to serialise.
 * @param data      The data object to which the key-value pair should be added.
 */
function serialiseInput(input: JQuery, data: {}) {
    const name = input.attr("name");
    const value = input.val();
    if (name.includes(".")) {
        if (!data[name.split(".")[0]]) {
            data[name.split(".")[0]] = {};
        }
        data[name.split(".")[0]][name.split(".")[1]] = value;
    } else data[name] = value;
}

// TODO: tests
if (typeof module !== "undefined" && typeof module.exports !== "undefined") {
    module.exports.serialiseBlockItems = serialiseBlockItems;
    module.exports.serialiseInput = serialiseInput;
    module.exports.serialiseItem = serialiseItem;
    module.exports.serialiseChoiceTask = serialiseChoiceTask;
}
