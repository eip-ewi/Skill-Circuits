import { marked, type RendererObject } from "marked";

export function parseMarkdown(markdown: string): string {
    const renderer: RendererObject<string, string> = {
        heading({ tokens, depth }): string {
            const text = this.parser.parseInline(tokens);
            return `<span class="h${depth}">${text}</span>`;
        },
    };
    marked.use({ renderer: renderer });

    return marked.parse(markdown) as string;
}
