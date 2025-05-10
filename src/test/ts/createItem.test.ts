// @ts-ignore
import {createUniqueNewTaskIdPostfix, appendTask} from "../../main/ts/createItem.ts";

test("No task exists", () => {
    const taskList = document.createElement("ul");
    document.body.append(taskList);

    expect(createUniqueNewTaskIdPostfix($(taskList))).toBe(0);
});

test("Only old task exists", () => {
    const taskList = document.createElement("ul");
    const task = document.createElement("li");
    task.classList.add("task");
    task.setAttribute("data-new", "false");
    taskList.append(task);
    document.body.append(taskList);

    expect(createUniqueNewTaskIdPostfix($(taskList))).toBe(0);
});

test("New tasks exist", () => {
    const taskList = document.createElement("ul");
    const task1 = document.createElement("li");
    const task2 = document.createElement("li");
    task1.classList.add("task");
    task2.classList.add("task");
    task1.setAttribute("data-new", "true");
    task2.setAttribute("data-new", "true");
    task1.id = "new-task-10-0";
    task2.id = "new-task-10-2";
    taskList.append(task1);
    taskList.append(task2);
    document.body.append(taskList);

    // Postfix should be the highest value + 1 (here: 2 + 1 = 3)
    expect(createUniqueNewTaskIdPostfix($(taskList))).toBe(3);
});

test("Append task to empty list", () => {
    const taskList = document.createElement("ul");
    document.body.append(taskList);
    const taskSeparation = document.createElement("li");
    taskSeparation.setAttribute("id", "task-separation");
    taskSeparation.classList.add("item__separation");
    document.body.append(taskSeparation);

    const task = document.createElement("li");
    task.classList.add("task");
    appendTask($(taskList), $(task));

    const children : JQuery = $(taskList).children();
    expect(children.length).toBe(3);
    expect($(children[0]).hasClass("item__separation")).toBe(true);
    expect($(children[1]).hasClass("task")).toBe(true);
    expect($(children[2]).hasClass("item__separation")).toBe(true);
});

test("Append task to non-empty list", () => {
    const taskSeparation = document.createElement("li");
    taskSeparation.classList.add("item__separation");

    const taskList = document.createElement("ul");
    const taskInList = document.createElement("li");
    taskInList.classList.add("task");

    $(taskList).append($(taskSeparation).clone(true));
    taskList.append(taskInList);
    $(taskList).append($(taskSeparation).clone(true));
    document.body.append(taskList);

    taskSeparation.setAttribute("id", "task-separation");
    document.body.append(taskSeparation);

    const taskToAppend = document.createElement("li");
    taskToAppend.classList.add("task");
    appendTask($(taskList), $(taskToAppend));

    const children : JQuery = $(taskList).children();
    expect(children.length).toBe(5);
    for (let i = 0; i < 5; i++) {
        if (i % 2 === 0) {
            expect($(children[i]).hasClass("item__separation")).toBe(true);
        } else {
            expect($(children[i]).hasClass("task")).toBe(true);
        }
    }
});