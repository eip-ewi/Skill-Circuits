<script lang="ts">
    import type {Point} from "../util/point";
    import {onMount} from "svelte";
    import Line from "./Line.svelte";
    import type {LineModel} from "../model/line";
    import type {BlockData} from "../data/block";
    import type {BlockModel} from "../model/block";

    let svg: SVGElement;
    let lines: LineModel[] = $state([]);

    export function drawLine(from: Point, to: Point, fromBlock: number, toBlock: number, animate: boolean = false, locked: boolean = false): LineModel {
        let line: LineModel = {from: from, to: to, fromBlock: fromBlock, toBlock: toBlock, animated: animate, locked: locked};
        lines.push(line);
        lines = lines;
        return line;
    }

    export function getIncomingLines(block: BlockModel): LineModel[] {
        return lines.filter(l => l.toBlock === block.id);
    }

    export function removeLine(line: LineModel) {
        lines.splice(lines.indexOf(line), 1);
        lines = lines;
    }

    export function refresh() {
        lines = [];
    }

</script>

<svg bind:this={svg} id="circuit-background" xmlns="http://www.w3.org/2000/svg">
    {#each lines as line}
        <Line from={line.from} to={line.to} animated={line.animated} locked={line.locked}></Line>
    {/each}
</svg>