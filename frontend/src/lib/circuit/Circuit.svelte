<script lang="ts">

    import {type Level, ModuleLevel} from "../data/level";
    import Lines from "./Lines.svelte";
    import type {CircuitData} from "../data/circuit";
    import type {BlockData} from "../data/block";
    import {onMount} from "svelte";
    import Block from "./Block.svelte";
    import type {Point} from "../util/point";
    import {type BackgroundGroup, createBackgroundGroup} from "../model/background_group";
    import Group from "./Group.svelte";
    import Column from "./Column.svelte";
    import {BlockRowChange, CircuitUpdates} from "../model/circuit_updates";
    import {BlockModel} from "../model/block";
    import {CircuitModel} from "../model/circuit";
    import {fetchAuth, getAuth} from "../data/auth.svelte";
    import Checkpoints from "./Checkpoints.svelte";

    let { level, columns = 5 }: { level: Level, columns: number } = $props();

    let circuit: CircuitModel | undefined = $state(undefined);
    let lines: Lines;
    let element: HTMLElement | undefined;
    let groups: BackgroundGroup[] = $state([]);
    let blocks: BlockModel[] = $state([]);
    let blockByPos: Map<String, BlockModel>;
    let height: number = $state(0);
    let updates: CircuitUpdates = new CircuitUpdates();

    onMount(async () => {
        let response: Response = await fetch(`/api/${level.circuit}/3`);
        circuit = new CircuitModel(await response.json());

        blocks = topoSort(circuit!.groups.flatMap(g => g.blocks));
        blockByPos = new Map();
        blocks.forEach(b => calculateHeight(b));
        height = Math.max(0, ...blocks.map(b => b.row!)) + 1;
        calculateGroups();

        window.addEventListener("resize", () => drawLines());
        setTimeout(() => drawLines(), 20);

        updates.subscribe("blockColumnChange", columnChange => {
            blockByPos = new Map();
            blocks.forEach(b => calculateHeight(b));
            height = Math.max(0, ...blocks.map(b => b.row!)) + 1;
            calculateGroups();
            setTimeout(() => drawLines(), 20);
        });

        updates.subscribe("blockCompletion", completion => {
            if (completion.completed) {
                completion.block.children.forEach(cId => {
                    drawLine(completion.block, circuit!.getBlock(cId)!, true);
                });
            } else {
                setTimeout(() => drawLines(), 20);
            }
        });

        updates.subscribe("highlightBlock", highlight => {
            if (highlight.block.locked) {
                if (highlight.highlighted) {
                    let drawToParents = highlight.block.parents.map(pId => circuit!.getBlock(pId)!).filter(p => !p.completed);
                    drawToParents.forEach(p => {
                        drawLine(p, highlight.block, false, true);
                    });
                } else {
                    lines.getIncomingLines(highlight.block).filter(l => l.locked).forEach(l => lines.removeLine(l));
                }
            }
        });
    });

    function topoSort(blocks: BlockModel[]): BlockModel[] {
        let roots: BlockModel[] = blocks.filter(b => b.parents.length === 0);
        let visited: Set<string> = new Set();
        let result: BlockModel[] = [];
        while (roots.length > 0) {
            let block = roots.shift()!;
            result.push(block);
            for (let childId of block.children) {
                visited.add(`${block.id}->${childId}`);
                let child = circuit!.getBlock(childId)!;
                if (child.parents.every(parentId => visited.has(`${parentId}->${childId}`))) {
                    roots.push(child);
                }
            }
        }
        return result;
    }

    function calculateHeight(block: BlockModel): number {

        let lowestParentRow = Math.max(-1, ...block.parents.map(parentId => circuit!.getBlock(parentId)!.row));
        let row = lowestParentRow + 1;
        while (blockByPos.has(`${block.column},${row}`)) {
            row++;
        }
        let oldRow = block.row;
        block.row = row;
        blockByPos.set(`${block.column},${row}`, block);
        updates.update(new BlockRowChange(block, level, oldRow, row))

        return row;

    }

    function drawLines(grid?: HTMLElement) {
        if (element === undefined) return;

        lines.refresh();

        for (let blockElement of document.querySelectorAll(".block")) {
            let block = circuit!.getBlock(parseInt(blockElement.dataset.id))!;
            for (let parentId of block.parents) {
                let parentElement = document.querySelector(`.block[data-id='${parentId}']`)!;

                if (parentElement.getAttribute("data-completed") !== "true" && !getAuth().canEditBlocks) continue;

                drawLine(circuit!.getBlock(parentId)!, block, false);
            }
        }
    }

    function drawLine(fromBlock: BlockData, toBlock: BlockData, animate: boolean = false, locked: boolean = false) {
        if (element === undefined) return;

        let circuitBoundingBox = element.getBoundingClientRect();
        let circuitPos: Point = { x: circuitBoundingBox.left, y: circuitBoundingBox.top + parseInt(getComputedStyle(element).getPropertyValue("padding-top")) };

        let parentElement = document.querySelector(`.block[data-id='${fromBlock.id}']`)!;
        let childElement = document.querySelector(`.block[data-id='${toBlock.id}']`)!;

        let childBoundingBox = childElement.getBoundingClientRect();
        let parentBoundingBox = parentElement.getBoundingClientRect();

        let from: Point = { x: parentBoundingBox.left + parentBoundingBox.width / 2 - circuitPos.x, y: parentBoundingBox.bottom - circuitPos.y };
        let to: Point = { x: childBoundingBox.left + childBoundingBox.width / 2 - circuitPos.x, y: childBoundingBox.top - circuitPos.y };

        lines.drawLine(from, to, fromBlock.id, toBlock.id, animate, locked);
    }

    function calculateGroups() {
        function getGroupByPosition(column: number, row: number): BackgroundGroup | undefined {
            return groups.find(elem => elem.column === column && elem.row === row);
        }
        function connect(group: BackgroundGroup | undefined, other: BackgroundGroup | undefined) {
            if (group === undefined || other === undefined) {
                return false;
            }
            return group.group.id === other.group.id;
        }

        groups = circuit!.groups.flatMap(g => g.blocks.map(b => {
            let block = circuit!.getBlock(b.id)!;
            return createBackgroundGroup(g, block.column, block.row!);
        }));

        let newGroups: BackgroundGroup[] = [];
        for (let group1 of groups) {
            for (let group2 of groups) {
                if (group1 === group2 || group1.group.id !== group2.group.id) {
                    continue;
                }
                let minX = Math.min(group1.column, group2.column);
                let minY = Math.min(group1.row, group2.row);
                let maxX = Math.max(group1.column, group2.column);
                let maxY = Math.max(group1.row, group2.row);
                if ((maxX - minX + 1) * (maxY - minY + 1) == 2) {
                    continue;
                }
                let allPositions: Point[] = [...Array(maxX - minX + 1).keys()].flatMap(column =>
                    [...Array(maxY - minY + 1).keys()].map(row => {
                        return {x: column + minX, y: row + minY};
                    }));
                if (allPositions.some(pos => {
                    let groupId: number | undefined = (getGroupByPosition(pos.x, pos.y) ?? groups.find(group => group.column === pos.x && group.row === pos.y))?.group?.id;
                    return groupId !== undefined && groupId !== group1.group.id;
                })) {
                    continue;
                }
                for (let pos of allPositions) {
                    newGroups.push(createBackgroundGroup(group1.group, pos.x, pos.y));
                }
            }
        }
        newGroups.forEach(group => groups.push(group));

        groups.forEach(group => {
            let left = getGroupByPosition(group.column - 1, group.row);
            let right = getGroupByPosition(group.column + 1, group.row);
            let up = getGroupByPosition(group.column, group.row - 1);
            let down = getGroupByPosition(group.column, group.row + 1);

            if (connect(left, group) ?? false) group.connectLeft = true;
            if (connect(right, group) ?? false) group.connectRight = true;
            if (connect(up, group) ?? false) group.connectTop = true;
            if (connect(down, group) ?? false) group.connectBottom = true;
        });

        groups.forEach(group => {
            if (group.connectTop && group.connectLeft && getGroupByPosition(group.column - 1, group.row - 1)?.group?.id === group.group.id) {
                group.connectTopLeft = true;
            }
            if (group.connectTop && group.connectRight && getGroupByPosition(group.column + 1, group.row - 1)?.group?.id === group.group.id) {
                group.connectTopRight = true;
            }
            if (group.connectBottom && group.connectRight && getGroupByPosition(group.column + 1, group.row + 1)?.group?.id === group.group.id) {
                group.connectBottomRight = true;
            }
            if (group.connectBottom && group.connectLeft && getGroupByPosition(group.column - 1, group.row + 1)?.group?.id === group.group.id) {
                group.connectBottomLeft = true;
            }
        });

        // Where to display titles
        let toProcess: BackgroundGroup[] = [...groups];
        while (toProcess.length > 0) {
            let clusterStart: BackgroundGroup = toProcess[0]!;
            let stack: BackgroundGroup[] = [clusterStart];
            let visited: Set<string> = new Set();

            while (stack.length > 0) {
                let current: BackgroundGroup = stack.pop()!;

                if (visited.has(`${current.column},${current.row}`)) {
                    continue;
                }
                visited.add(`${current.column},${current.row}`);

                if (current.connectLeft) {
                    let left: BackgroundGroup | undefined = getGroupByPosition(current.column - 1, current.row);
                    if (left !== undefined) {
                        stack.push(left);
                    }
                }
                if (current.connectRight) {
                    let right: BackgroundGroup | undefined = getGroupByPosition(current.column + 1, current.row);
                    if (right !== undefined) {
                        stack.push(right);
                    }
                }
                if (current.connectTop) {
                    let up: BackgroundGroup | undefined = getGroupByPosition(current.column, current.row - 1);
                    if (up !== undefined) {
                        stack.push(up);
                    }
                }
                if (current.connectBottom) {
                    let down: BackgroundGroup | undefined = getGroupByPosition(current.column , current.row + 1);
                    if (down !== undefined) {
                        stack.push(down);
                    }
                }
            }

            let cluster = [...toProcess.filter(groupElem => visited.has(`${groupElem.column},${groupElem.row}`))];
            let minY = Math.min(...cluster.map(groupElem => groupElem.row));
            let minX = Math.min(...cluster.filter(groupElem => groupElem.row === minY).map(groupElem => groupElem.column));
            getGroupByPosition(minX, minY)!.showLabel = true;

            toProcess = toProcess.filter(groupElem => !visited.has(`${groupElem.column},${groupElem.row}`));
        }
    }

</script>

<main bind:this={element} id="circuit">
    {#if circuit !== undefined}
        <h1 id="circuit-title">{circuit.name}</h1>

        <div id="circuit-grid" style:grid-template-columns="repeat({columns}, 1fr)" use:drawLines>
            {#each blocks as block}
                <Block {block} {level} {updates}/>
            {/each}
            {#each groups as group}
                <Group group={group.group} column={group.column} row={group.row} showLabel={group.showLabel}
                       connectTop={group.connectTop} connectBottom={group.connectBottom} connectLeft={group.connectLeft} connectRight={group.connectRight}
                       connectTopLeft={group.connectTopLeft} connectTopRight={group.connectTopRight} connectBottomRight={group.connectBottomRight} connectBottomLeft={group.connectBottomLeft}
                />
            {/each}
            {#each {length: columns} as _, i}
                <Column {circuit} {level} column={i} {height} {updates}/>
            {/each}

            {#if level === ModuleLevel && circuit !== undefined}
                <Checkpoints module={circuit} updates={updates}/>
            {/if}
        </div>
    {/if}

    <Lines bind:this={lines}/>
</main>