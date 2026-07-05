import { Outlet } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Globe, Moon, Sun, ShieldCheck, GitMerge, FileCode2 } from "lucide-react";
import { useTheme } from "../../hooks/useTheme.ts";
import { motion } from "framer-motion";

function ScrollingCodeBackground() {
    const codeSnippet = `
// JPlag Abstract Syntax Tree (AST) Scanner
import { Parser } from '@jplag/core';

class JPlagEngine {
  private targetRepo: string;
  private similarityThreshold: number;

  constructor(repoUrl: string) {
    this.targetRepo = repoUrl;
    this.similarityThreshold = 0.85; // 85% match
  }

  public async analyzeTokens(): Promise<Report> {
    const astNodes = await Parser.parse(this.targetRepo);
    const hashes = astNodes.map(node => this.hashNode(node));
    
    return this.compare(hashes);
  }

  private hashNode(node: ASTNode): string {
    if (node.type === 'BlockStatement') {
      return Crypto.SHA256(node.body);
    }
    return node.hash;
  }
}

// Initiating parallel processing
const engine = new JPlagEngine('gitea.p.lodz.pl/projects');
engine.analyzeTokens().then(console.log);
`;

    const repeatedCode = Array(5).fill(codeSnippet).join('\n');

    return (
        <div
            className="absolute inset-0 overflow-hidden pointer-events-none select-none z-0 opacity-[0.15]"
            style={{
                maskImage: 'linear-gradient(to bottom, transparent 0%, black 15%, black 85%, transparent 100%)',
                WebkitMaskImage: 'linear-gradient(to bottom, transparent 0%, black 15%, black 85%, transparent 100%)'
            }}
        >
            <motion.pre
                className="text-primary font-mono text-[10px] md:text-xs leading-relaxed p-8"
                animate={{ y: ["0%", "-50%"] }}
                transition={{ repeat: Infinity, ease: "linear", duration: 40 }}
            >
                {repeatedCode}
            </motion.pre>
        </div>
    );
}

export default function AuthLayout() {
    const { t, i18n } = useTranslation();
    const { theme, setTheme } = useTheme();

    return (
        <div className="min-h-screen w-full flex bg-base overflow-hidden transition-colors duration-300">

            <div className="hidden lg:flex lg:w-1/2 bg-surface border-r border-border relative flex-col justify-center p-16 xl:p-24 pb-32">
                <ScrollingCodeBackground />
                <div className="absolute top-0 left-0 w-full h-full bg-gradient-to-br from-surface/80 via-transparent to-surface/80 pointer-events-none z-0" />

                <motion.div
                    initial="hidden"
                    animate="visible"
                    variants={{ visible: { transition: { staggerChildren: 0.15 } } }}
                    className="relative z-10 space-y-8 max-w-2xl"
                >
                    <div className="flex items-center gap-6">
                        <motion.h1 variants={{ hidden: { opacity: 0, x: -15 }, visible: { opacity: 1, x: 0 } }} className="text-5xl xl:text-6xl font-black text-primary tracking-tighter leading-tight drop-shadow-sm">
                            {t('auth.layoutTitle')} <br/>
                            <span className="text-brand bg-clip-text">{t('auth.layoutTitleHighlight')}</span>
                        </motion.h1>

                        <motion.div
                            variants={{ hidden: { opacity: 0, scale: 0.8 }, visible: { opacity: 1, scale: 1 } }}
                            className="inline-flex p-4 bg-brand/10 rounded-3xl text-brand border border-brand/20 shadow-lg backdrop-blur-sm"
                        >
                            <ShieldCheck size={56} strokeWidth={2.5} />
                        </motion.div>
                    </div>

                    <motion.p variants={{ hidden: { opacity: 0, y: 15 }, visible: { opacity: 1, y: 0 } }} className="text-xl text-secondary max-w-xl leading-relaxed">
                        {t('auth.layoutSubtitle')}
                    </motion.p>

                    <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 pt-4">
                        <motion.a
                            href="https://team-1.proj-sum.it.p.lodz.pl/git/"
                            target="_blank"
                            rel="noopener noreferrer"
                            variants={{ hidden: { opacity: 0, y: 10 }, visible: { opacity: 1, y: 0 } }}
                            whileHover={{ scale: 1.02, y: -2, boxShadow: "0 10px 30px -10px rgba(0,0,0,0.05)" }}
                            className="flex items-start gap-5 p-6 bg-base/80 backdrop-blur-md rounded-2xl border border-border transition-all duration-300 hover:border-brand/30 cursor-pointer group"
                        >
                            <div className="p-3 bg-surface rounded-xl border border-border text-brand mt-0.5 shadow-sm group-hover:bg-brand/10 transition-colors">
                                <GitMerge size={24} />
                            </div>
                            <div>
                                <h3 className="font-bold text-base text-primary group-hover:text-brand transition-colors">{t('auth.features.gitea.title')}</h3>
                                <p className="text-sm text-secondary mt-1">{t('auth.features.gitea.desc')}</p>
                            </div>
                        </motion.a>

                        <motion.a
                            href="https://github.com/jplag/jplag"
                            target="_blank"
                            rel="noopener noreferrer"
                            variants={{ hidden: { opacity: 0, y: 10 }, visible: { opacity: 1, y: 0 } }}
                            whileHover={{ scale: 1.02, y: -2, boxShadow: "0 10px 30px -10px rgba(0,0,0,0.05)" }}
                            className="flex items-start gap-5 p-6 bg-base/80 backdrop-blur-md rounded-2xl border border-border transition-all duration-300 hover:border-brand/30 cursor-pointer group"
                        >
                            <div className="p-3 bg-surface rounded-xl border border-border text-brand mt-0.5 shadow-sm group-hover:bg-brand/10 transition-colors">
                                <FileCode2 size={24} />
                            </div>
                            <div>
                                <h3 className="font-bold text-base text-primary group-hover:text-brand transition-colors">{t('auth.features.jplag.title')}</h3>
                                <p className="text-sm text-secondary mt-1">{t('auth.features.jplag.desc')}</p>
                            </div>
                        </motion.a>
                    </div>
                </motion.div>

                <div className="absolute bottom-8 left-16 xl:left-24 z-10 text-sm text-secondary/50 font-mono">
                    <p>{t('auth.footer', { year: new Date().getFullYear() })}</p>
                </div>
            </div>

            <div className="w-full lg:w-1/2 h-full min-h-screen flex flex-col relative z-10 bg-base">
                <div className="absolute top-6 right-8 z-50 flex items-center gap-4">
                    <div className="flex items-center bg-base border border-border rounded-lg p-1 h-10 shadow-sm">
                        <button onClick={() => setTheme('light')} className={`flex items-center justify-center gap-2 h-full px-3 text-sm font-semibold rounded-md transition-all ${theme === 'light' ? 'bg-surface shadow-sm text-primary border border-border' : 'text-secondary hover:text-primary'}`}>
                            <Sun size={16}/> Light
                        </button>
                        <button onClick={() => setTheme('dark')} className={`flex items-center justify-center gap-2 h-full px-3 text-sm font-semibold rounded-md transition-all ${theme === 'dark' ? 'bg-surface shadow-sm text-primary border border-border' : 'text-secondary hover:text-primary'}`}>
                            <Moon size={16}/> Dark
                        </button>
                    </div>

                    <div className="relative flex items-center group bg-base border border-border hover:border-brand rounded-lg h-10 pl-3 pr-2 shadow-sm transition-all focus-within:ring-2 focus-within:ring-brand/20">
                        <Globe size={16} className="text-secondary group-hover:text-brand transition-colors shrink-0"/>
                        <select
                            value={i18n.language}
                            onChange={(e) => i18n.changeLanguage(e.target.value)}
                            className="bg-transparent text-sm font-bold text-primary outline-none cursor-pointer appearance-none pl-2 pr-6 h-full w-full dark:bg-base"
                        >
                            <option value="en">EN</option>
                            <option value="pl">PL</option>
                            <option value="uk">UK</option>
                        </select>
                        <div className="absolute right-2 pointer-events-none text-secondary group-hover:text-brand transition-colors h-full flex items-center">
                            <svg className="w-4 h-4 fill-current" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M5.22 8.22a.75.75 0 0 1 1.06 0L10 11.94l3.72-3.72a.75.75 0 1 1 1.06 1.06l-4.25 4.25a.75.75 0 0 1-1.06 0L5.22 9.28a.75.75 0 0 1 0-1.06Z" clipRule="evenodd"></path>
                            </svg>
                        </div>
                    </div>
                </div>

                <div className="flex-grow flex items-center justify-center p-6 sm:p-10 md:p-16">
                    <Outlet />
                </div>
            </div>
        </div>
    );
}