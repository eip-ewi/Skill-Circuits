import {cubicInOut, linear} from "svelte/easing";
import type {Point} from "../data/point";
import type {Block} from "../dto/circuit/block";

export function openExpandedBlockTransition(element: Element, params: { block: Block }) {
    return {
        duration: 300,
        easing: linear,
        css: (t: number) => {
            let t3 = cubicInOut(t);
            let start: Point = {
                x: params.block.boundingRect!().left + params.block.boundingRect!().width / 2,
                y: params.block.boundingRect!().top + params.block.boundingRect!().height / 2,
            };
            let position = {
                x: `calc(${start.x * (1 - t3)}px + ${50 * t3}vw - ${50 * t3}%)`,
                y: `calc(${start.y * (1 - t3)}px + ${50 * t3}vh - ${50 * t3}%)`,
            }
            return `
                   --blur: ${t * 0.5}rem;
                   opacity: ${t3};
                   transform: translate(${position.x}, ${position.y}) scale(${t3 * 0.9 + 0.1}) ;
                `;
        }
    }
}