export interface TaskInTaskList {
    infoId: number;
    name: string;
    type: string;
    time: number;
    link: string | null;
    choiceTaskName: string | null;
    skillName: string;
    submoduleName: string;
    moduleName: string;
    paths: number[];
}