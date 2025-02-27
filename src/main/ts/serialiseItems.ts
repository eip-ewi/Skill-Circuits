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
    // TODO: handle patching

    const data = {};
    // serialise direct children (hidden fields)
    item.children("input").each((_, input) => {
        serialiseInput($(input), data);
    });
    // serialise header
    item.find(".choice_task__header")
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
    data["subTasks"] = subTasks
        .map((_, item) => {
            // TODO: also reverse indices?
            const index = notDeletedItems.index(item);
            return serialiseItem($(item), index);
        })
        .toArray();

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
