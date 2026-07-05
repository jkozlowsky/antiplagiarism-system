import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';

interface SafeCodeViewerProps {
    code: string;
    flaggedLines?: number[];
    language?: string;
}

export default function SafeCodeViewer({ code, flaggedLines = [], language = "java" }: SafeCodeViewerProps) {
    return (
        <div className="rounded-xl overflow-hidden border border-border text-sm shadow-inner w-full">
            <SyntaxHighlighter
                language={language}
                style={vscDarkPlus}
                showLineNumbers={true}
                wrapLines={true}
                customStyle={{
                    margin: 0,
                    padding: '1.5rem',
                    background: '#1e1e1e',
                }}
                lineProps={(lineNumber: number) => {
                    const style: React.CSSProperties = {
                        display: 'block',
                        paddingLeft: '10px'
                    };

                    if (flaggedLines.includes(lineNumber)) {
                        style.backgroundColor = 'rgba(239, 68, 68, 0.15)';
                        style.borderLeft = '4px solid #ef4444';
                    } else {
                        style.borderLeft = '4px solid transparent';
                    }

                    return { style };
                }}
            >
                {code}
            </SyntaxHighlighter>
        </div>
    );
}