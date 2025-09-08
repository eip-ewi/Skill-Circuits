// @ts-ignore
import {serialiseBlockItems, serialiseInput, serialiseItem, serialiseChoiceTask} from "../../main/ts/serialiseItems.ts";

// Load the HTML of the body for the tests
const fs = require("fs");
const path = require("path");
const htmlPath = path.join(path.join(__dirname, "html"), "serialise-items.html");
const defaultInnerHTML = fs.readFileSync(htmlPath, "utf-8");

beforeEach(() => {
    document.body.innerHTML = defaultInnerHTML;
});

function getChoiceTaskData(idx: number): {} {
    return {
        "taskType" : "ChoiceTask",
        "new": true,
        "delete": false,
        "index": idx,
        "skill": {
        "id": "10"
    },
        "minTasks": "1",
        "name": "Choice task name",
        "updatedSubTasks": [
        {
            "taskType" : "RegularTask",
            "new": false,
            "delete": false,
            "index": 1,
            "skill": {
                "id": "10"
            },
            "taskInfo" : {
                "time" : "15",
                "type" : "QUIZ",
                "name": "Old sub-task",
                "link": "http://localhost:8084"
            }
        }
    ],
        "newSubTasks": [
        {
            "taskType" : "RegularTask",
            "new": true,
            "delete": false,
            "index": 0,
            "skill": {
                "id": "10"
            },
            "taskInfo" : {
                "time" : "5",
                "type" : "READING",
                "name": "New sub-task",
                "link": ""
            }
        }
    ]
    };
}

function getDeletedRegularTaskData(idx: number): {} {
    return {
        "taskType" : "RegularTask",
        "new": false,
        "delete": true,
        "index": idx,
        "skill": {
            "id": "10"
        },
        "taskInfo" : {
            "time" : "10",
            "type" : "VIDEO",
            "name": "Deleted regular task",
            "link": ""
        }
    };
}

function getNewRegularTaskData(idx: number): {} {
    return {
        "taskType" : "RegularTask",
        "new": true,
        "delete": false,
        "index": idx,
        "skill": {
            "id": "10"
        },
        "taskInfo" : {
            "time" : "5",
            "type" : "READING",
            "name": "New regular task",
            "link": ""
        }
    };
}

function getOldRegularTaskData(idx: number): {} {
    return {
        "taskType" : "RegularTask",
        "new": false,
        "delete": false,
        "index": idx,
        "skill": {
            "id": "10"
        },
        "taskInfo" : {
            "time" : "15",
            "type" : "QUIZ",
            "name": "Old regular task",
            "link": "http://localhost:8084"
        }
    };
}

test("Test serialise input with nesting", () => {
    const input1 = $("#nested-input-test-1");
    const input2 = $("#nested-input-test-2");
    const data = {};

    serialiseInput(input1, data);
    expect(data).toEqual({
        "taskInfo" : {
            "time" : "5"
        }
    });

    serialiseInput(input2, data);
    expect(data).toEqual({
        "taskInfo" : {
            "time" : "5",
            "name": "New regular task"
        }
    });
});

test("Test serialise input without nesting", () => {
    const input = $("#non-nested-input-test");
    const data = {};

    serialiseInput(input, data);
    expect(data).toEqual({
        "taskType" : "RegularTask"
    });
});

test("Test serialise select", () => {
    const select = $("#select-test");
    const data = {};

    serialiseInput(select, data);
    expect(data).toEqual({
        "taskInfo" : {
            "type" : "READING"
        }
    });
});

test("Test serialise choice task", () => {
    const task = $("#new-choice-task");
    const data = serialiseChoiceTask(task, 0);
    expect(data).toEqual(getChoiceTaskData(0));
});

test("Test serialise item with choice task", () => {
    const task = $("#new-choice-task");
    const data = serialiseItem(task, 0);
    expect(data).toEqual(getChoiceTaskData(0));
});

test("Test serialise item with new regular task", () => {
    const task = $("#new-regular-task");
    const data = serialiseItem(task, 0);
    expect(data).toEqual(getNewRegularTaskData(0));
});

test("Test serialise item with old regular task", () => {
    const task = $("#old-regular-task");
    const data = serialiseItem(task, 0);
    expect(data).toEqual(getOldRegularTaskData(0));
});

test("Test serialise item with deleted regular task", () => {
    const task = $("#deleted-regular-task");
    const data = serialiseItem(task, 0);
    expect(data).toEqual(getDeletedRegularTaskData(0));
});

test("Test serialise block items", () => {
    const list = $("#block");
    const data = serialiseBlockItems(list);

    expect(data).toHaveLength(4);
    expect(data).toContainEqual(getOldRegularTaskData(0));
    expect(data).toContainEqual(getChoiceTaskData(1));
    expect(data).toContainEqual(getNewRegularTaskData(2));

    // Index 3, since: 3 - 1 - (-1) = 3
    expect(data).toContainEqual(getDeletedRegularTaskData(3));
});