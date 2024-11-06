import type {BlockData} from "../data/block";
import {withCsrf} from "../util/csrf";
import type {Level} from "../data/level";
import type {ItemData} from "../data/item";
import type {BlockModel} from "./block";
import type {BlockPatch} from "../data/block_patch";

export class CircuitUpdates {

    subscriptions: Map<keyof UpdateType, ((update: any) => void)[]>

    constructor() {
        this.subscriptions = new Map();
    }

    subscribe<Type extends keyof UpdateType>(updateType: Type, onUpdate: (update: UpdateType[Type]) => void) {
        let handlers = this.subscriptions.get(updateType) ?? [];
        handlers.push(onUpdate);
        this.subscriptions.set(updateType, handlers);
    }

    update<Type extends keyof UpdateType, U extends UpdateType[Type] & Update>(update: U): void {
        update.execute();
        let handlers = this.subscriptions.get(update.type) ?? [];
        handlers.forEach(handler => handler(update));
    }

}

export type UpdateType = {
    "blockColumnChange": BlockColumnChange,
    "blockRowChange": BlockRowChange,
    "itemCompletion": ItemCompletion,
    "blockCompletion": BlockCompletion,
    "highlightBlock": HighlightBlock,
    "editBlock": EditBlock,
}

interface Update {
    readonly type: keyof UpdateType;
    execute(): void
}

export class BlockColumnChange implements Update {
    readonly type = "blockColumnChange";
    readonly block: BlockData;
    readonly level: Level;
    readonly oldColumn: number;
    readonly newColumn: number;

    constructor(block: BlockData, level: Level, oldColumn: number, newColumn: number) {
        this.block = block;
        this.level = level;
        this.oldColumn = oldColumn;
        this.newColumn = newColumn;
    }

    execute() {
        this.block.column = this.newColumn;
        fetch(`/api/${this.level.block}/${this.block.id}/position?column=${this.newColumn}`, withCsrf({
            method: "PATCH",
            headers: {
                "Content-Type": "application/json"
            }
        }))
            .then(res => res.status)
            .then(status => {
            })
            .catch(e => console.error(e));
    }
}

export class BlockRowChange implements Update {
    readonly type = "blockRowChange";
    readonly block: BlockData;
    readonly level: Level;
    readonly oldRow: number;
    readonly newRow: number;

    constructor(block: BlockData, level: Level, oldRow: number, newRow: number) {
        this.block = block;
        this.level = level;
        this.oldRow = oldRow;
        this.newRow = newRow;
    }

    execute() {}
}

export class ItemCompletion implements Update {

    readonly type = "itemCompletion"
    readonly item: ItemData;
    readonly level: Level;
    readonly completed: boolean;

    constructor(item: ItemData, level: Level) {
        this.item = item;
        this.level = level;
        this.completed = !item.completed;
    }

    execute() {
        this.item.completed = this.completed;
        fetch(`/api/${this.level.item}/${this.item.id}/complete?completed=${this.item.completed}`, withCsrf({
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        }))
            .then(res => res.status)
            .then(status => {
            })
            .catch(e => console.error(e));
    }
}

export class BlockCompletion implements Update {

    readonly type = "blockCompletion"
    readonly block: BlockData;
    readonly completed: boolean;

    constructor(block: BlockData, completed: boolean) {
        this.block = block;
        this.completed = completed;
    }

    execute() {}
}

export class HighlightBlock implements Update {

    readonly type = "highlightBlock"
    readonly block: BlockModel;
    readonly highlighted: boolean;

    constructor(block: BlockModel, highlighted: boolean) {
        this.block = block;
        this.highlighted = highlighted;
    }

    execute() {}

}

export class EditBlock implements Update {

    readonly type = "editBlock";
    readonly block: BlockData;
    readonly patch: BlockPatch;
    readonly level: Level;

    constructor(block: BlockData, patch: BlockPatch, level: Level) {
        this.block = block;
        this.patch = patch;
        this.level = level;
    }

    execute(): void {
        const itemById: Map<number, ItemData> = new Map();
        this.block.items.forEach(i => itemById.set(i.id, i));
        this.patch.items.forEach(itemPatch => {
            const item = itemById.get(itemPatch.id)!;
            item.name = itemPatch.name;
            item.time = itemPatch.time;
        });

        fetch(`/api/${this.level.block}`, withCsrf({
            method: "PATCH",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(this.patch),
        }))
            .then(res => res.status)
            .then(status => {
            })
            .catch(e => console.error(e));
    }

}