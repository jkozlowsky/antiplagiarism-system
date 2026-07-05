import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import {AlertCircle, ArrowLeft, RefreshCw, Search} from "lucide-react";
import {useTranslation} from "react-i18next";
import {subjectService} from '../../services/subjectService.ts';
import type {SubjectStudentStatsDTO} from "../../types/subject.types";
import {PATHS} from "../../routes/paths";

export default function TeacherSubjectUsersPage() {
    const {subjectName} = useParams<{ subjectName: string }>();
    const {t} = useTranslation();
    const navigate = useNavigate();

    const decodedSubjectName = subjectName ? decodeURIComponent(subjectName) : "";

    const [data, setData] = useState<SubjectStudentStatsDTO[]>([]);
    const [filteredData, setFilteredData] = useState<SubjectStudentStatsDTO[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [searchPhrase, setSearchPhrase] = useState("");

    const fetchData = async () => {
        if (!decodedSubjectName) return;

        setIsLoading(true);
        setErrorMessage(null);
        try {
            const result = await subjectService.getSubjectUsers(decodedSubjectName);
            setData(result);
            setFilteredData(result);
        } catch (error) {
            console.error("Błąd podczas pobierania studentów przedmiotu:", error);
            setErrorMessage(t('subjectUsers.error'));
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [subjectName]);

    useEffect(() => {
        const lowercasedFilter = searchPhrase.toLowerCase().trim();
        if (!lowercasedFilter) {
            setFilteredData(data);
            return;
        }

        const filtered = data.filter(item =>
            item.firstName.toLowerCase().includes(lowercasedFilter) ||
            item.lastName.toLowerCase().includes(lowercasedFilter) ||
            item.repositoryName.toLowerCase().includes(lowercasedFilter)
        );
        setFilteredData(filtered);
    }, [searchPhrase, data]);

    return (
        <div className="min-h-screen bg-base p-8">
            <div className="max-w-7xl mx-auto">

                <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
                    <div>
                        <button
                            onClick={() => navigate(PATHS.TEACHER_SUBJECT_LIST)}
                            className="flex items-center gap-2 text-xs font-bold text-secondary uppercase tracking-widest hover:text-brand transition-colors mb-4">
                            <ArrowLeft size={16}/>
                            {t('common.cancel')}
                        </button>

                        <h1 className="text-3xl font-bold text-primary mb-2">
                            {t('subjectUsers.title')}{" "}
                            <span className="text-brand font-extrabold">{decodedSubjectName}</span>
                        </h1>
                        <p className="text-secondary text-sm max-w-2xl">
                            {t('subjectUsers.subtitle')}
                        </p>
                    </div>

                    <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3 w-full md:w-auto">
                        <button
                            onClick={fetchData}
                            disabled={isLoading}
                            className="flex items-center justify-center gap-2 px-4 py-2 bg-surface border border-border hover:bg-active disabled:opacity-50 rounded-md text-sm font-bold text-primary transition-colors cursor-pointer">
                            <RefreshCw size={16} className={isLoading ? "animate-spin" : ""}/>
                            {t('userList.refresh')}
                        </button>

                        <div className="relative flex-1 sm:flex-none">
                            <div
                                className="absolute inset-y-0 left-3 flex items-center pointer-events-none text-secondary">
                                <Search size={16}/>
                            </div>
                            <input
                                type="text"
                                placeholder={t('subjectUsers.searchPlaceholder')}
                                value={searchPhrase}
                                onChange={(e) => setSearchPhrase(e.target.value)}
                                className="pl-10 pr-4 py-2 bg-surface border border-border rounded-md text-sm font-semibold text-primary focus:ring-2 focus:ring-brand outline-none w-full sm:w-64 transition-all"
                            />
                        </div>
                    </div>
                </div>

                {errorMessage && (
                    <div
                        className="mb-6 p-4 bg-danger-subtle border border-danger text-danger rounded-xl flex items-center gap-3 text-sm font-semibold">
                        <AlertCircle size={20}/>
                        {errorMessage}
                    </div>
                )}

                <div
                    className="bg-surface rounded-2xl shadow-sm border border-border overflow-hidden flex flex-col relative min-h-50">

                    {isLoading && (
                        <div
                            className="absolute inset-0 bg-surface/60 backdrop-blur-[1px] flex items-center justify-center z-10 transition-all">
                            <RefreshCw className="animate-spin text-brand" size={36}/>
                        </div>
                    )}

                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse relative">
                            <thead>
                            <tr className="border-b border-border select-none bg-base">
                                <th className="py-5 px-8 text-xs font-bold text-secondary uppercase tracking-widest">
                                    {t('subjectUsers.table.firstName')}
                                </th>
                                <th className="py-5 px-8 text-xs font-bold text-secondary uppercase tracking-widest">
                                    {t('subjectUsers.table.surname')}
                                </th>
                                <th className="py-5 px-8 text-xs font-bold text-secondary uppercase tracking-widest">
                                    {t('subjectUsers.table.repoName')}
                                </th>
                                <th className="py-5 px-8 text-xs font-bold text-secondary uppercase tracking-widest text-center">
                                    {t('subjectUsers.table.plagiarism')}
                                </th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-border">
                            {filteredData.length === 0 ? (
                                <tr>
                                    <td colSpan={4} className="py-12 text-center text-secondary font-medium">
                                        {isLoading
                                            ? t('common.loading')
                                            : t('subjectUsers.noResults')
                                        }
                                    </td>
                                </tr>
                            ) : (
                                filteredData.map((row) => (
                                    <tr key={row.studentId} className="hover:bg-base/40 transition-colors group">
                                        <td className="py-4 px-8 text-sm text-primary font-semibold">
                                            {row.firstName}
                                        </td>

                                        <td className="py-4 px-8 text-sm text-primary font-semibold">
                                            {row.lastName}
                                        </td>

                                        <td className="py-4 px-8 text-sm">
                                                <span
                                                    className="font-mono text-xs bg-base text-secondary px-2.5 py-1.5 rounded-md border border-border group-hover:border-brand/30 transition-colors inline-block">
                                                    {row.repositoryName}
                                                </span>
                                        </td>

                                        <td className="py-4 px-8 text-sm text-center">
                                            {typeof row.plagiarismRatio === 'number' ? (
                                                <span
                                                    className={`px-3 py-1 rounded-full text-xs font-bold tracking-wide shadow-sm inline-block ${
                                                        row.plagiarismRatio > 60
                                                            ? 'bg-danger-subtle text-danger border border-danger/20'
                                                            : 'bg-green-100 text-green-800 dark:bg-green-950/40 dark:text-green-400 border border-green-500/20'
                                                    }`}>
                {row.plagiarismRatio.toFixed(1)}%
            </span>
                                            ) : (
                                                <span
                                                    className="text-secondary italic text-xs bg-base px-2.5 py-1 rounded-full border border-border">
                {t('subjectUsers.table.noData')}
        </span>
                                            )}
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}