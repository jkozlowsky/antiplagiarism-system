import React, {useState, useEffect, useRef, useMemo} from 'react';
import {useTranslation} from 'react-i18next';
import type {TeacherComparison} from '../../types/report.types';
import {Prism as SyntaxHighlighter} from 'react-syntax-highlighter';
import {vs, vscDarkPlus} from 'react-syntax-highlighter/dist/esm/styles/prism';

const MATCH_PALETTE = [
    { bgLight: 'bg-red-100/80', textLight: 'text-red-900', borderLight: 'border-red-500', bgDark: 'bg-[#5a1d1d]', textDark: 'text-[#ff7b72]', borderDark: 'border-red-500' },
    { bgLight: 'bg-blue-100/80', textLight: 'text-blue-900', borderLight: 'border-blue-500', bgDark: 'bg-[#1d3b5a]', textDark: 'text-[#72a4ff]', borderDark: 'border-blue-500' },
    { bgLight: 'bg-green-100/80', textLight: 'text-green-900', borderLight: 'border-green-500', bgDark: 'bg-[#1d5a2b]', textDark: 'text-[#72ff8b]', borderDark: 'border-green-500' },
    { bgLight: 'bg-orange-100/80', textLight: 'text-orange-900', borderLight: 'border-orange-500', bgDark: 'bg-[#5a3a1d]', textDark: 'text-[#ffb372]', borderDark: 'border-orange-500' },
    { bgLight: 'bg-purple-100/80', textLight: 'text-purple-900', borderLight: 'border-purple-500', bgDark: 'bg-[#461d5a]', textDark: 'text-[#d472ff]', borderDark: 'border-purple-500' },
];

const getLanguageFromFileName = (fileName: string) => {
    if (!fileName) return 'text';
    const extension = fileName.split('.').pop()?.toLowerCase();

    switch (extension) {
        case 'java':
            return 'java';
        case 'cpp':
        case 'cxx':
        case 'cc':
        case 'h':
        case 'hpp':
            return 'cpp';
        case 'c':
            return 'c';
        case 'py':
            return 'python';
        case 'js':
            return 'javascript';
        case 'ts':
            return 'typescript';
        case 'cs':
            return 'csharp';
        case 'go':
            return 'go';
        case 'rb':
            return 'go';
        case 'php':
            return 'php';
        case 'html':
            return 'markup';
        case 'css':
            return 'css';
        case 'json':
            return 'json';
        case 'xml':
            return 'xml';
        case 'sql':
            return 'sql';
        case 'sh':
            return 'bash';
        default:
            return 'text';
    }
};

interface SideBySideViewerProps {
    comparison: TeacherComparison;
}

export const SideBySideViewer: React.FC<SideBySideViewerProps> = ({comparison}) => {
    const {t} = useTranslation();
    const [history, setHistory] = useState<Record<string, number>>({});
    const compKey = `${comparison.firstSubmission}-${comparison.secondSubmission}`;
    const selectedFileIdx = history[compKey] || 0;

    const handleSetSelectedFileIdx = (idx: number) => {
        setHistory(prev => ({...prev, [compKey]: idx}));
    };

    const [isDarkMode, setIsDarkMode] = useState(false);

    useEffect(() => {
        const checkTheme = () => {
            const hasDarkClass = document.documentElement.classList.contains('dark') || document.body.classList.contains('dark');
            const hasDarkAttr = document.documentElement.getAttribute('data-theme') === 'dark' || document.body.getAttribute('data-theme') === 'dark';
            setIsDarkMode(hasDarkClass || hasDarkAttr);
        };
        checkTheme();
        const observer = new MutationObserver(checkTheme);
        observer.observe(document.documentElement, {attributes: true, attributeFilter: ['class', 'data-theme']});
        observer.observe(document.body, {attributes: true, attributeFilter: ['class', 'data-theme']});
        return () => observer.disconnect();
    }, []);

    const scrollContainerARef = useRef<HTMLDivElement>(null);
    const scrollContainerBRef = useRef<HTMLDivElement>(null);

    const isSyncingLeft = useRef(false);
    const isSyncingRight = useRef(false);

    const handleScrollA = (e: React.UIEvent<HTMLDivElement>) => {
        if (isSyncingLeft.current) {
            isSyncingLeft.current = false;
            return;
        }
        isSyncingRight.current = true;
        if (scrollContainerBRef.current) scrollContainerBRef.current.scrollLeft = e.currentTarget.scrollLeft;
    };

    const handleScrollB = (e: React.UIEvent<HTMLDivElement>) => {
        if (isSyncingRight.current) {
            isSyncingRight.current = false;
            return;
        }
        isSyncingLeft.current = true;
        if (scrollContainerARef.current) scrollContainerARef.current.scrollLeft = e.currentTarget.scrollLeft;
    };

    const filesToRender = useMemo(() => {
        if (!comparison.matchedFiles || comparison.matchedFiles.length === 0) return [];

        const grouped: Record<string, any> = {};

        comparison.matchedFiles.forEach(match => {
            const key = `${match.fileA}|${match.fileB}`;
            if (!grouped[key]) {
                grouped[key] = {
                    fileA: match.fileA,
                    fileB: match.fileB,
                    codeA: match.codeA,
                    codeB: match.codeB,
                    blocks: [],
                    totalTokens: 0
                };
            }

            const tokensCount = match.tokens || match.matchedLinesA?.length || 0;

            grouped[key].blocks.push({
                linesA: match.matchedLinesA || [],
                linesB: match.matchedLinesB || [],
                tokens: tokensCount
            });

            grouped[key].totalTokens += tokensCount;
        });

        const result = Object.values(grouped);

        result.forEach(fileMatch => {
            fileMatch.blocks.sort((block1: any, block2: any) => {
                const startLine1 = block1.linesA.length > 0 ? block1.linesA[0] : 0;
                const startLine2 = block2.linesA.length > 0 ? block2.linesA[0] : 0;
                return startLine1 - startLine2;
            });
        });

        return result.sort((a, b) => b.totalTokens - a.totalTokens);
    }, [comparison]);

    const currentMatch = filesToRender[selectedFileIdx] || filesToRender[0];

    useEffect(() => {
        if (!currentMatch || currentMatch.blocks.length === 0) return;

        const firstBlock = currentMatch.blocks[0];
        let isScrolledA = false;
        let isScrolledB = false;

        const interval = setInterval(() => {
            const containerA = scrollContainerARef.current;
            const containerB = scrollContainerBRef.current;

            if (!isScrolledA && containerA && firstBlock.linesA.length > 0) {
                const elA = containerA.querySelector(`#line-a-${firstBlock.linesA[0]}`);
                if (elA) {
                    containerA.scrollTo({top: (elA as HTMLElement).offsetTop - 40, behavior: 'smooth'});
                    isScrolledA = true;
                }
            } else {
                isScrolledA = true;
            }

            if (!isScrolledB && containerB && firstBlock.linesB.length > 0) {
                const elB = containerB.querySelector(`#line-b-${firstBlock.linesB[0]}`);
                if (elB) {
                    containerB.scrollTo({top: (elB as HTMLElement).offsetTop - 40, behavior: 'smooth'});
                    isScrolledB = true;
                }
            } else {
                isScrolledB = true;
            }

            if (isScrolledA && isScrolledB) clearInterval(interval);
        }, 200);

        const timeout = setTimeout(() => clearInterval(interval), 10000);
        return () => {
            clearInterval(interval);
            clearTimeout(timeout);
        };
    }, [currentMatch]);

    const handleLineClick = (blockIdx: number, source: 'a' | 'b') => {
        if (blockIdx === -1) return;
        const block = currentMatch.blocks[blockIdx];

        if (source === 'a' && block.linesB.length > 0) {
            const el = scrollContainerBRef.current?.querySelector(`#line-b-${block.linesB[0]}`);
            if (el) scrollContainerBRef.current?.scrollTo({
                top: (el as HTMLElement).offsetTop - 40,
                behavior: 'smooth'
            });
        } else if (source === 'b' && block.linesA.length > 0) {
            const el = scrollContainerARef.current?.querySelector(`#line-a-${block.linesA[0]}`);
            if (el) scrollContainerARef.current?.scrollTo({
                top: (el as HTMLElement).offsetTop - 40,
                behavior: 'smooth'
            });
        }
    };

    const renderCode = (code: string, columnPrefix: 'a' | 'b', fileName: string) => {
        const language = getLanguageFromFileName(fileName);
        if (!currentMatch) return null;

        return (
            <div className="min-w-max">
                <SyntaxHighlighter
                    language={language}
                    style={isDarkMode ? vscDarkPlus : vs}
                    showLineNumbers={true}
                    wrapLines={true}
                    codeTagProps={{
                        style: {
                            fontSize: '13px',
                            fontFamily: 'Consolas, Monaco, "Andale Mono", "Ubuntu Mono", monospace'
                        }
                    }}
                    customStyle={{margin: 0, padding: '10px 0', background: 'transparent', fontSize: '13px'}}
                    lineProps={(line) => {
                        const linesKey = columnPrefix === 'a' ? 'linesA' : 'linesB';
                        const blockIdx = currentMatch.blocks.findIndex((b: any) => b[linesKey].includes(line));

                        const isMatched = blockIdx !== -1;
                        let className = `block px-2 min-w-full transition-colors `;

                        if (isMatched) {
                            const color = MATCH_PALETTE[blockIdx % MATCH_PALETTE.length];
                            const styleClass = isDarkMode
                                ? `${color.bgDark} ${color.textDark} border-l-4 ${color.borderDark}`
                                : `${color.bgLight} ${color.textLight} border-l-4 ${color.borderLight}`;
                            className += `${styleClass} font-medium cursor-pointer hover:opacity-80`;
                        } else {
                            className += isDarkMode ? 'hover:bg-[#2a2d2e] border-l-4 border-transparent' : 'hover:bg-slate-100 border-l-4 border-transparent text-gray-800';
                        }

                        return {
                            id: `line-${columnPrefix}-${line}`,
                            className,
                            onClick: () => handleLineClick(blockIdx, columnPrefix)
                        };
                    }}
                >
                    {code || t('report.noCode')}
                </SyntaxHighlighter>
            </div>
        );
    };

    if (!currentMatch) return <div className="p-4">{t('report.noFiles')}</div>;

    const codeContainerBg = isDarkMode ? 'bg-[#1e1e1e]' : 'bg-[#ffffff]';
    const headerBgClass = isDarkMode ? 'bg-[#2d2d30] border-[#404040]' : 'bg-slate-100 border-slate-300';
    const headerTextClass = isDarkMode ? 'text-slate-300' : 'text-slate-700';

    return (
        <div className="flex flex-col flex-1 min-h-0 bg-base pt-4">

            <div className="flex items-center gap-2 mb-3 shrink-0">
                <span
                    className="font-semibold text-primary text-sm whitespace-nowrap bg-surface px-3 py-1.5 rounded-lg border border-border shadow-sm">
                    {t('report.files')} ({filesToRender.length}):
                </span>

                <div className="flex flex-nowrap overflow-x-auto custom-scrollbar gap-2 pb-1 flex-1">
                    {filesToRender.map((match, idx) => {
                        const nameA = match.fileA.split('/').pop() || 'Brak pliku';
                        const nameB = match.fileB.split('/').pop() || 'Brak pliku';
                        const tabLabel = nameA === nameB ? nameA : `${nameA} vs ${nameB}`;
                        const isSelected = selectedFileIdx === idx;

                        return (
                            <button
                                key={idx}
                                onClick={() => handleSetSelectedFileIdx(idx)}
                                className={`flex flex-col items-start text-xs py-1 px-3 rounded-md shadow-sm transition-all border whitespace-nowrap shrink-0 ${
                                    isSelected
                                        ? 'bg-brand/10 border-brand text-brand font-bold'
                                        : 'bg-surface border-border hover:border-brand/50 hover:bg-active text-secondary'
                                }`}
                                title={`${t('report.totalTokens')}: ${match.totalTokens}\n${t('report.copiedFragments')}: ${match.blocks.length}`}
                            >
                                <span className="mb-0.5">{tabLabel}</span>
                                <span
                                    className={`text-[10px] mt-0.5 ${isSelected ? 'text-brand/70' : 'opacity-60 font-normal'}`}>
    ({match.totalTokens} {t('report.tokensLabel')} | {match.blocks.length} {t('report.fragmentsLabel')})
</span>
                            </button>
                        );
                    })}
                </div>
            </div>

            <div className="flex flex-col xl:flex-row gap-4 flex-1 min-h-0">
                <div
                    className="flex-1 flex flex-col border border-border rounded-lg overflow-hidden shadow-sm bg-surface">
                    <div
                        className={`${headerBgClass} p-2 text-xs font-mono font-semibold border-b break-words shrink-0`}
                        title={currentMatch.fileA}>
                        <span className={headerTextClass}> {comparison.firstSubmission}</span> <br/>
                        <span className="text-slate-500 dark:text-slate-400 text-[10px]">{currentMatch.fileA}</span>
                    </div>
                    <div
                        ref={scrollContainerARef}
                        onScroll={handleScrollA}
                        className={`flex-1 overflow-auto relative custom-scrollbar ${codeContainerBg}`}
                    >
                        {renderCode(currentMatch.codeA || '', 'a', currentMatch.fileA)}
                    </div>
                </div>

                <div
                    className="flex-1 flex flex-col border border-border rounded-lg overflow-hidden shadow-sm bg-surface">
                    <div
                        className={`${headerBgClass} p-2 text-xs font-mono font-semibold border-b break-words shrink-0`}
                        title={currentMatch.fileB}>
                        <span className={headerTextClass}> {comparison.secondSubmission}</span> <br/>
                        <span className="text-slate-500 dark:text-slate-400 text-[10px]">{currentMatch.fileB}</span>
                    </div>
                    <div
                        ref={scrollContainerBRef}
                        onScroll={handleScrollB}
                        className={`flex-1 overflow-auto relative custom-scrollbar ${codeContainerBg}`}
                    >
                        {renderCode(currentMatch.codeB || '', 'b', currentMatch.fileB)}
                    </div>
                </div>

            </div>
        </div>
    );
};