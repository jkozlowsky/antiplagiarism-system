import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { searchTeachers } from '../../services/teacherService';
import type { TeacherAssignmentDTO, TeacherSearchDTO, TeacherSubjectRole } from '../../types/SubjectDTO';

interface Props {
    teachers: TeacherAssignmentDTO[];
    setTeachers: (teachers: TeacherAssignmentDTO[]) => void;
}

export const TeacherAssignmentManager: React.FC<Props> = ({ teachers, setTeachers }) => {
    const { t } = useTranslation();
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<TeacherSearchDTO[]>([]);
    const [isSearching, setIsSearching] = useState(false);

    useEffect(() => {
        const timer = setTimeout(() => {
            if (searchQuery.trim().length >= 2) {
                setIsSearching(true);
                searchTeachers(searchQuery)
                    .then(res => setSearchResults(res))
                    .catch(() => setSearchResults([]))
                    .finally(() => setIsSearching(false));
            } else {
                setSearchResults([]);
            }
        }, 300);
        return () => clearTimeout(timer);
    }, [searchQuery]);

    const handleAddTeacher = (teacher: TeacherSearchDTO) => {
        if (!teachers.find(t => t.login === teacher.login)) {
            setTeachers([...teachers, { login: teacher.login, role: 'EDITOR' }]);
        }
        setSearchQuery('');
        setSearchResults([]);
    };

    const handleRemoveTeacher = (login: string) => {
        setTeachers(teachers.filter(t => t.login !== login));
    };

    const handleRoleChange = (login: string, role: TeacherSubjectRole) => {
        setTeachers(teachers.map(t => t.login === login ? { ...t, role } : t));
    };

    return (
        <div className="flex flex-col gap-4">
            <div className="relative">
                <label className="block text-sm font-semibold text-primary mb-1">{t('subjectCreate.addTeacher')}</label>
                <input
                    type="text"
                    placeholder={t('subjectCreate.searchPlaceholder')}
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                    className="w-full bg-surface border border-border text-primary rounded-lg px-4 py-2.5 focus:outline-none focus:border-brand transition-colors"
                />
                {searchQuery.length >= 2 && (
                    <div className="absolute z-10 w-full mt-1 bg-surface border border-border rounded-lg shadow-lg overflow-hidden max-h-60 overflow-y-auto">
                        {isSearching ? (
                            <div className="p-4 text-sm text-secondary text-center">{t('common.searching')}</div>
                        ) : searchResults.length > 0 ? (
                            searchResults.map(res => (
                                <div key={res.login} onClick={() => handleAddTeacher(res)} className="px-4 py-3 hover:bg-base cursor-pointer border-b border-border last:border-0 flex justify-between items-center">
                                    <div>
                                        <p className="font-semibold text-primary">{res.firstName} {res.lastName}</p>
                                        <p className="text-xs text-secondary">{res.email}</p>
                                    </div>
                                    <div className="text-brand text-sm font-bold">{t('subjectCreate.addBtn')}</div>
                                </div>
                            ))
                        ) : (
                            <div className="p-4 text-sm text-secondary text-center">{t('subjectCreate.noResults')}</div>
                        )}
                    </div>
                )}
            </div>
            <div>
                <label className="block text-sm font-semibold text-secondary mb-2 uppercase text-xs tracking-wider">{t('subjectCreate.assignedStaff')}</label>
                <div className="flex flex-col gap-3">
                    {teachers.length === 0 && <p className="text-sm text-secondary italic">{t('subjectCreate.noStaff')}</p>}
                    {teachers.map(teacher => (
                        <div key={teacher.login} className="flex items-center justify-between p-3 bg-surface border border-border rounded-xl">
                            <div className="flex items-center gap-3">
                                <div className="w-10 h-10 rounded-full bg-brand text-white flex items-center justify-center font-bold text-sm">
                                    {teacher.login.substring(0, 2).toUpperCase()}
                                </div>
                                <p className="font-semibold text-primary">{teacher.login}</p>
                            </div>
                            <div className="flex items-center gap-4">
                                <select
                                    value={teacher.role}
                                    onChange={(e) => handleRoleChange(teacher.login, e.target.value as TeacherSubjectRole)}
                                    className="bg-base border border-border text-primary text-sm rounded-lg px-3 py-1.5 focus:outline-none focus:border-brand"
                                >
                                    <option value="OWNER">{t('subjectCreate.roleOwner')}</option>
                                    <option value="EDITOR">{t('subjectCreate.roleEditor')}</option>
                                    <option value="VIEWER">{t('subjectCreate.roleViewer')}</option>
                                </select>
                                <button type="button" onClick={() => handleRemoveTeacher(teacher.login)} className="text-secondary hover:text-danger p-1">
                                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};