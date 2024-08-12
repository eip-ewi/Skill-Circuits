// @ts-ignore
import {createUniqueNewTaskId} from "../../main/ts/createItem.ts";

test("No task exists", () => {
    const taskList = document.createElement("ul");
    document.body.append(taskList);

    expect(createUniqueNewTaskId(10, $(taskList))).toBe("new-task-10-0");
});

test("Only old task exists", () => {
    const taskList = document.createElement("ul");
    const task = document.createElement("li");
    task.classList.add("task");
    task.setAttribute("data-new", "false");
    taskList.append(task);
    document.body.append(taskList);

    expect(createUniqueNewTaskId(10, $(taskList))).toBe("new-task-10-0");
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

    // Suffix should be the highest value + 1 (here: 2 + 1 = 3)
    expect(createUniqueNewTaskId(10, $(taskList))).toBe("new-task-10-3");
});