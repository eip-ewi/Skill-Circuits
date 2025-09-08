export interface Checkpoint {
    id: number;
    name: string;
    deadline: string;

    editing: boolean | undefined;
}