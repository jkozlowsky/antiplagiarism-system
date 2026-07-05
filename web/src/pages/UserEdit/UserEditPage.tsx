import {useCallback, useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import {CheckCircle2, Lock} from "lucide-react";
import axios from "axios";
import * as yup from "yup";
import {useTranslation} from "react-i18next";
import {userService} from "../../services/userService";
import type {AccountWithAccessLevelsDTO, ChangeEmailDTO} from "../../types/user.types";
import {RoleEnum} from "../../types/role.types";
import {PATHS} from "../../routes/paths";
import SubmitButton from "../../shared/components/buttons/SubmitButton";
import {useBreadcrumb} from "../../contexts/BreadcrumbContext";
import {emailChangeService} from "../../services/emailChangeService.ts";
import {emailSchema} from "../../shared/validators/emailSchema.ts";
import {useAuth} from "../../hooks/useAuth.ts";
import {ChangeOwnPasswordForm} from "./ChangeOwnPasswordForm.tsx";
import ConfirmationModal from "../../shared/components/modals/ConfirmationPopup.tsx";
import ChangeOtherPasswordModal from "../UserList/ChangeOtherPasswordModal.tsx";
import {personalDetailsSchema} from "../../shared/validators/userSchema.ts";

export default function UserEditPage() {
    const {id} = useParams<{ id: string }>();
    const navigate = useNavigate();
    const {t} = useTranslation();
    const {setDynamicBreadcrumb} = useBreadcrumb();
    const {userLogin, activeRole} = useAuth();
    const isAdmin = activeRole?.includes(RoleEnum.ADMINISTRATOR);

    const [user, setUser] = useState<AccountWithAccessLevelsDTO | null>(null);
    const [localRoles, setLocalRoles] = useState<string[]>([]);
    const [isSaving, setIsSaving] = useState(false);
    const [isBlocking, setIsBlocking] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [emailValue, setEmailValue] = useState('');
    const [emailError, setEmailError] = useState<boolean>(false);
    const [isEmailRequesting, setIsEmailRequesting] = useState(false);
    const [emailRequestError, setEmailRequestError] = useState<string | null>(null);
    const [emailSuccess, setEmailSuccess] = useState(false);
    const [resendStatus, setResendStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
    const [isMyself, setIsMyself] = useState(true);

    const [nameValue, setNameValue] = useState('');
    const [nameError, setNameError] = useState<string | null>(null);
    const [surnameValue, setSurnameValue] = useState('');
    const [surnameError, setSurnameError] = useState<string | null>(null);
    const [showPasswordForm, setShowPasswordForm] = useState(false);

    const [isSaveModalOpen, setIsSaveModalOpen] = useState(false);
    const [isBlockModalOpen, setIsBlockModalOpen] = useState(false);
    const [isChangePasswordModalOpen, setIsChangePasswordModalOpen] = useState(false);
    const fetchUser = useCallback(async (userId: string) => {
        try {
            const data = await userService.getAccountById(userId);
            setUser(data);
            setLocalRoles(data.accessLevels.filter(al => al.active).map(al => al.accessLevelName));
            setNameValue(data.account.name);
            setSurnameValue(data.account.surname);
            setEmailValue(data.account.email);
            setIsMyself(data.account.login === userLogin)
        } catch (err) {
            console.error(err);
            navigate(PATHS.USER_LIST);
        }
    }, [navigate, userLogin]);

    const fetchUserByLogin = useCallback(async (login: string) => {
        try {
            const data = await userService.getAccountByLogin(login);
            setUser(data);
            setLocalRoles(data.accessLevels.filter(al => al.active).map(al => al.accessLevelName));
            setNameValue(data.account.name);
            setSurnameValue(data.account.surname);
            setEmailValue(data.account.email);
            setIsMyself(data.account.login === userLogin)
        } catch (err) {
            console.error(err);
            navigate(PATHS.USER_LIST);
        }
    }, [navigate, userLogin]);

    useEffect(() => {
        if (!id && userLogin) {
            fetchUserByLogin(userLogin);
        } else if (id) {
            fetchUser(id);
        }
    }, [id, userLogin, activeRole, isAdmin, navigate, fetchUser, fetchUserByLogin]);

    useEffect(() => {
        if (user) {
            setDynamicBreadcrumb(user.account.login);
        }
        return () => setDynamicBreadcrumb(null);
    }, [user, setDynamicBreadcrumb]);

    const handleBlock = async () => {
        if (!user || isBlocking) return;

        setIsBlocking(true);
        setError(null);

        try {
            if (!user.account.isBlocked) {
                await userService.blockUser(user.account.id, user.account.versionHash);
            } else {
                await userService.unblockUser(user.account.id, user.account.versionHash);
            }
            await fetchUser(user.account.id);
            setIsBlockModalOpen(false);
        } catch (err) {
            console.error(err);
            if (axios.isAxiosError(err)) {
                setError(err.response?.data?.message || err.response?.data || t('userEdit.messages.updateError'));
            } else {
                setError(t('userEdit.messages.updateError'));
            }
        } finally {
            setIsBlocking(false);
            setIsBlockModalOpen(false)
        }
    };

    const handleSave = async () => {
        if (!user) return;
        setIsSaving(true);
        setError(null);
        setNameError(null);
        setSurnameError(null);
        setEmailError(false);

        try {
            await emailSchema.validate({email: emailValue});
            await personalDetailsSchema.validate({ name: nameValue, surname: surnameValue }, { abortEarly: false });

            let currentHash = user.account.versionHash;
            let latestUser = user;

            if (latestUser.account.email !== emailValue && isAdmin) {
                const emailDTO: ChangeEmailDTO = {email: emailValue};
                await emailChangeService.changeEmailByAdmin(latestUser.account.login, emailDTO, currentHash);

                latestUser = await userService.getAccountById(latestUser.account.id);
                currentHash = latestUser.account.versionHash;
            }
            if (latestUser.account.name !== nameValue || latestUser.account.surname !== surnameValue) {
                const accountUpdateDTO = {
                    name: nameValue,
                    surname: surnameValue,
                };
                if (isAdmin) {
                    await userService.updateUserDetails(latestUser.account.id, accountUpdateDTO, currentHash);
                    latestUser = await userService.getAccountById(latestUser.account.id);
                } else {
                    latestUser = await userService.updateMyDetails(accountUpdateDTO, currentHash);
                    latestUser = await userService.getAccountByLogin(latestUser.account.login);
                }
            }
            currentHash = latestUser.account.versionHash;
            if (isAdmin) {
                const initialRoles = user.accessLevels.filter(al => al.active).map(al => al.accessLevelName);
                const rolesToGrant = localRoles.filter(r => !initialRoles.includes(r));
                const rolesToRevoke = initialRoles.filter(r => !localRoles.includes(r));

                for (const role of rolesToGrant) {
                    latestUser = await userService.grantAccessLevel(user.account.id, role, currentHash);
                    currentHash = latestUser.account.versionHash;
                }

                for (const role of rolesToRevoke) {
                    latestUser = await userService.revokeAccessLevel(user.account.id, role, currentHash);
                    currentHash = latestUser.account.versionHash;
                }
            }
            setUser(latestUser);
            setLocalRoles(latestUser.accessLevels.filter(al => al.active).map(al => al.accessLevelName));
            setIsSaveModalOpen(false);
        } catch (err) {
            console.error(err);
            if (yup.ValidationError.isError(err)) {
                if (err.inner.length > 0) {
                    err.inner.forEach((e) => {
                        if (e.path === "name") setNameError(t(e.message));
                        if (e.path === "surname") setSurnameError(t(e.message));
                        if (e.path === "email") setEmailError(true);
                    });
                } else {
                    if (err.path === "name") setNameError(t(err.message));
                    else if (err.path === "surname") setSurnameError(t(err.message));
                    else if (err.path === "email" || !err.path) setEmailError(true);
                    else setError(t(err.message));
                }
            } else if (axios.isAxiosError(err)) {
                const responseData = err.response?.data;
                if (responseData && typeof responseData === 'object' && 'violations' in responseData && Array.isArray(responseData.violations)) {
                    const violationMessages = responseData.violations
                        .map((v: { name: string; message: string }) => `${v.name}: ${v.message}`)
                        .join(', ');
                    setError(violationMessages);
                } else {
                    setError(typeof responseData === 'string' ? responseData : t('userEdit.messages.updateError'));
                }
            } else if (err instanceof Error) {
                setError(err.message);
            } else {
                setError(t('userEdit.messages.updateError'));
            }
        } finally {
            setIsSaving(false);
            setIsSaveModalOpen(false);
        }
    };

    const onEmailSubmit = async () => {
        setIsEmailRequesting(true);
        setEmailRequestError(null);
        try {
            await emailChangeService.requestEmailChange();
            setEmailSuccess(true);
        } catch (error) {
            console.error("Failed to initiate email change:", error);
            if (axios.isAxiosError(error)) {
                const msg = error.response?.data?.message;
                setEmailRequestError(msg ? t(msg) : t('emailChange.main.error.default'));
            } else {
                setEmailRequestError(t('emailChange.main.error.unexpected'));
            }
        } finally {
            setIsEmailRequesting(false);
        }
    };

    const handleResend = async () => {
        setResendStatus('loading');
        try {
            await emailChangeService.resendEmailChangeRequest();
            setResendStatus('success');
        } catch (error) {
            console.error("Failed to resend email change token", error);
            setResendStatus('error');
        }
    };

    const handleDiscard = () => {
        if (user) {
            setLocalRoles(user.accessLevels.filter(al => al.active).map(al => al.accessLevelName));
            setEmailValue(user.account.email);
            setNameValue(user.account.name);
            setSurnameValue(user.account.surname);
            setNameError(null);
            setSurnameError(null);
            setEmailError(false);
        }
    };

    const handleEmailOnBlur = async () => {
        try {
            await emailSchema.validate({email: emailValue});
            setEmailError(false);
        } catch (err) {
            if (yup.ValidationError.isError(err)) {
                setEmailError(true);
            }
        }
    };

    const handleNameOnBlur = async () => {
        try {
            await (yup.reach(personalDetailsSchema, 'name') as yup.StringSchema).validate(nameValue);
            setNameError(null);
        } catch (err) {
            if (yup.ValidationError.isError(err)) setNameError(t(err.message));
        }
    };

    const handleSurnameOnBlur = async () => {
        try {
            await (yup.reach(personalDetailsSchema, 'surname') as yup.StringSchema).validate(surnameValue);
            setSurnameError(null);
        } catch (err) {
            if (yup.ValidationError.isError(err)) setSurnameError(t(err.message));
        }
    };

    const toggleRole = (role: string) => {
        setLocalRoles(prev =>
            prev.includes(role) ? prev.filter(r => r !== role) : [...prev, role]
        );
    };

    const renderRoleBadge = (roleName: string) => {
        if (roleName === RoleEnum.ADMINISTRATOR) {
            return (
                <span key={roleName}
                      className="px-3 py-1 bg-red-100 text-[#7A1014] text-[10px] font-bold rounded-full tracking-wider">
                    {t('userEdit.roles.adminBadge')}
                </span>
            );
        }
        return (
            <span key={roleName}
                  className="px-3 py-1 bg-teal-100 text-teal-800 text-[10px] font-bold rounded-full tracking-wider">
                {roleName === RoleEnum.TEACHER
                    ? t('userEdit.roles.teacher').toUpperCase()
                    : roleName === RoleEnum.STUDENT
                        ? t('userEdit.roles.student').toUpperCase()
                        : roleName}
            </span>
        );
    };

    const formatDate = (dateString?: string | null) => {
        if (!dateString) return t('userEdit.stats.never');
        return new Date(dateString).toLocaleString();
    };

    if (!user) {
        return (
            <div className="min-h-screen bg-base flex justify-center items-center">
                <div className="w-8 h-8 border-4 border-brand border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-base p-8 transition-colors duration-300">
            <div className="max-w-6xl mx-auto">
                <h1 className="text-3xl font-bold text-primary mb-8">{t('userEdit.title')}</h1>

                {error && (
                    <div
                        className="mb-4 p-4 bg-danger-subtle text-danger rounded-md border border-danger-border text-sm font-semibold">
                        {error}
                    </div>
                )}

                <div className="flex flex-col md:flex-row gap-8 items-start">
                    <div className="w-full md:w-1/3 space-y-6">
                        <div
                            className="bg-surface rounded-2xl shadow-sm border border-border p-8 flex flex-col items-center justify-center">
                            <h2 className="text-2xl font-extrabold text-primary text-center">{user.account.name} {user.account.surname}</h2>
                            <div className="flex flex-wrap gap-2 justify-center mt-4">
                                {localRoles.length > 0
                                    ? localRoles.map(renderRoleBadge)
                                    : <span className="text-xs text-secondary">{t('userEdit.noRoles')}</span>
                                }
                            </div>
                        </div>

                        <div className="bg-base rounded-r-2xl border-l-4 border-brand/50 p-6">
                            <h3 className="text-xs font-bold tracking-widest text-secondary uppercase mb-4">{t('userEdit.stats.title')}</h3>

                            <div className="space-y-4">
                                <div className="flex justify-between items-center">
                                    <span
                                        className="text-sm font-semibold text-secondary">{t('userEdit.stats.lastLogin')}</span>
                                    <span
                                        className="text-sm font-bold text-primary">{formatDate(user.account.lastLoginSuccessDateTime)}</span>
                                </div>

                                <div className="flex justify-between items-center">
                                    <span
                                        className="text-sm font-semibold text-secondary">{t('userEdit.stats.lastLoginFail')}</span>
                                    <span
                                        className="text-sm font-bold text-primary">{formatDate(user.account.lastLoginFailureDateTime)}</span>
                                </div>

                                <div className="flex justify-between items-start">
                                    <span
                                        className="text-sm font-semibold text-secondary">{t('userEdit.stats.createdAt')}</span>
                                    <div className="text-right">
                                        <p className="text-sm font-bold text-primary">{formatDate(user.account.createdAt)}</p>
                                        {user.account.createdBy && (
                                            <p className="text-[10px] text-secondary font-medium tracking-wide mt-0.5">
                                                {t('userEdit.stats.createdBy')}: <span
                                                className="text-brand">{user.account.createdBy}</span>
                                            </p>
                                        )}
                                    </div>
                                </div>

                                <div className="flex justify-between items-start">
                                    <span
                                        className="text-sm font-semibold text-secondary">{t('userEdit.stats.updatedAt')}</span>
                                    <div className="text-right">
                                        <p className="text-sm font-bold text-primary">{formatDate(user.account.updatedAt)}</p>
                                        {user.account.modifiedBy && (
                                            <p className="text-[10px] text-secondary font-medium tracking-wide mt-0.5">
                                                {t('userEdit.stats.modifiedBy')}: <span
                                                className="text-brand">{user.account.modifiedBy}</span>
                                            </p>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="w-full md:w-2/3 bg-surface rounded-2xl shadow-sm border border-border p-8">

                        <div className="mb-8">
                            <h3 className="text-xs font-bold text-brand tracking-widest uppercase mb-4">{t('userEdit.credentials.title')}</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label
                                        className="block text-xs font-bold text-secondary uppercase tracking-wider mb-2">{t('userEdit.credentials.login')}</label>
                                    <div className="relative bg-base border border-border rounded-md">
                                        <input
                                            type="text"
                                            value={user.account.login}
                                            readOnly
                                            className="w-full bg-transparent p-3 text-sm font-medium text-secondary outline-none pr-10 cursor-not-allowed"
                                        />
                                        <Lock size={16}
                                              className="absolute right-3 top-1/2 -translate-y-1/2 text-secondary opacity-50"/>
                                    </div>
                                    <p className="text-[10px] text-secondary mt-2">{t('userEdit.credentials.loginHint')}</p>
                                </div>
                                <div>
                                    <label
                                        className="block text-xs font-bold text-secondary uppercase tracking-wider mb-2">{t('userEdit.credentials.password')}</label>
                                    <div className="relative bg-base border border-border rounded-md">
                                        <input
                                            type="password"
                                            value="••••••••"
                                            readOnly
                                            className="w-full bg-transparent p-3 text-sm font-medium text-secondary outline-none pr-10 cursor-not-allowed"
                                        />
                                    </div>
                                    {isAdmin && !isMyself && (
                                        <div className="mt-2 text-right">
                                            <button
                                                id="changePasswordModalBtn"
                                                type="button"
                                                onClick={() => setIsChangePasswordModalOpen(true)}
                                                className="text-[10px] font-bold text-brand hover:text-brand-hover tracking-widest uppercase transition-colors"
                                            >
                                                {t('userList.changePassword')}
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>

                        {isMyself && (
                            <div className="mb-8 p-6 bg-base rounded-xl border border-border">
                                {!showPasswordForm ? (
                                    <div className="flex justify-between items-center">
                                        <div>
                                            <h3 className="font-medium text-primary">{t('profile.security')}</h3>
                                            <p className="text-sm text-secondary">{t('profile.securityDesc')}</p>
                                        </div>
                                        <button
                                            id="changeOwnPasswordBtn"
                                            onClick={() => setShowPasswordForm(true)}
                                            className="bg-brand hover:bg-brand-hover text-white font-bold px-4 py-2 rounded-md transition-colors text-xs tracking-widest uppercase"
                                        >
                                            {t('profile.changeOwnPassword')}
                                        </button>
                                    </div>
                                ) : (
                                    <div className="animate-in fade-in duration-300">
                                        <div className="flex justify-between items-center mb-4">
                                            <h3 className="font-medium text-primary">{t('profile.changePassword')}</h3>
                                            <button
                                                onClick={() => setShowPasswordForm(false)}
                                                className="text-secondary hover:text-primary font-bold text-sm"
                                            >
                                                {t('profile.cancel')}
                                            </button>
                                        </div>
                                        <ChangeOwnPasswordForm
                                            version={user.account.versionHash}
                                            onSuccess={() => {
                                                setShowPasswordForm(false);
                                            }}
                                        />
                                    </div>
                                )}
                            </div>
                        )}

                        <hr className="border-border mb-8"/>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                            <div>
                                <label
                                    className="block text-xs font-bold text-secondary uppercase tracking-wider mb-2">{t('userEdit.personal.firstName')}</label>
                                <input
                                    id="firstNameInput"
                                    type="text"
                                    value={nameValue}
                                    onChange={(e) => {
                                        setNameValue(e.target.value);
                                        if (nameError) setNameError(null);
                                    }}
                                    onBlur={handleNameOnBlur}
                                    className={`w-full bg-surface border rounded-md p-3 text-sm font-medium text-primary outline-none transition-colors ${
                                        nameError ? "border-danger focus:border-danger" : "border-border focus:border-brand"
                                    }`}
                                />
                                {nameError && (
                                    <p className="text-xs text-danger font-semibold mt-2">{nameError}</p>
                                )}
                            </div>
                            <div>
                                <label
                                    className="block text-xs font-bold text-secondary uppercase tracking-wider mb-2">{t('userEdit.personal.surname')}</label>
                                <input
                                    id="lastNameInput"
                                    type="text"
                                    value={surnameValue}
                                    onChange={(e) => {
                                        setSurnameValue(e.target.value);
                                        if (surnameError) setSurnameError(null);
                                    }}
                                    onBlur={handleSurnameOnBlur}
                                    className={`w-full bg-surface border rounded-md p-3 text-sm font-medium text-primary outline-none transition-colors ${
                                        surnameError ? "border-danger focus:border-danger" : "border-border focus:border-brand"
                                    }`}
                                />
                                {surnameError && (
                                    <p className="text-xs text-danger font-semibold mt-2">{surnameError}</p>
                                )}
                            </div>
                        </div>

                        <div className="mb-8">
                            <label className="block text-xs font-bold text-secondary uppercase tracking-wider mb-2">
                                {t('userEdit.personal.email')}
                            </label>
                            <input
                                id="userEmailInput"
                                type="email"
                                value={emailValue}
                                required={true}
                                readOnly={!isAdmin || isMyself}
                                onChange={(e) => setEmailValue(e.target.value)}
                                onBlur={handleEmailOnBlur}
                                className={`w-full rounded-md p-3 text-sm font-medium transition-colors outline-none border ${
                                    emailError ? "border-danger focus:border-danger" : "border-border focus:border-brand"
                                } ${isAdmin && !isMyself ? "bg-surface text-primary" : "text-secondary bg-base cursor-not-allowed"}`}
                            />

                            {emailError && (
                                <p className="text-xs text-danger font-semibold mt-2">
                                    {t('userEdit.personal.emailError')}
                                </p>
                            )}

                            {emailRequestError && (
                                <p className="text-xs text-danger font-semibold mt-2">
                                    {emailRequestError}
                                </p>
                            )}

                            {(!isAdmin || isMyself) && !emailSuccess && (
                                <SubmitButton
                                    id="requestEmailChangeBtn"
                                    onClick={onEmailSubmit}
                                    isLoading={isEmailRequesting}
                                    className="mt-6"
                                >
                                    {t('emailChange.main.form.submitButton')}
                                </SubmitButton>
                            )}

                            {(!isAdmin || isMyself) && emailSuccess && (
                                <div
                                    className="mt-6 p-5 bg-green-50 dark:bg-green-900/20 border border-green-100 dark:border-green-900/50 rounded-xl animate-in fade-in duration-300">
                                    <div className="flex items-start gap-3">
                                        <CheckCircle2
                                            className="text-green-600 dark:text-green-500 mt-0.5 flex-shrink-0"
                                            size={20}/>
                                        <div className="flex-1">
                                            <p className="text-sm text-green-800 dark:text-green-400 font-medium mb-1">
                                                {t('emailChange.main.success.description')}
                                            </p>
                                            <p className="text-xs text-green-600 dark:text-green-500/80 mb-4 font-semibold">
                                                {t('emailChange.main.success.timeLimit')}
                                            </p>

                                            <div className="flex items-center gap-4">
                                                <button
                                                    id="resendEmailChangeBtn"
                                                    type="button"
                                                    onClick={handleResend}
                                                    disabled={resendStatus === 'loading' || resendStatus === 'success'}
                                                    className="px-4 py-2 bg-white dark:bg-green-900/40 border border-green-200 dark:border-green-800 hover:bg-green-100 dark:hover:bg-green-900/60 text-green-700 dark:text-green-400 text-xs font-bold tracking-widest uppercase rounded-md transition-colors disabled:opacity-50"
                                                >
                                                    {resendStatus === 'loading' ? t('common.loading', 'LOADING...') :
                                                        resendStatus === 'success' ? t('emailChange.main.resendSuccess', 'SENT!') :
                                                            t('emailChange.main.resendButton', 'RESEND EMAIL')}
                                                </button>
                                                {resendStatus === 'error' && (
                                                    <span className="text-danger text-xs font-semibold">
                                                    {t('emailChange.main.error.unexpected')}
                                                </span>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>

                        <hr className="border-border mb-8"/>

                        {isAdmin && !isMyself && (
                            <>
                                <div
                                    className={`bg-base border border-border rounded-xl p-4 flex items-center justify-between mb-8 transition-opacity ${isBlocking ? 'opacity-50' : 'opacity-100'}`}>
                                    <div>
                                        <h3 className="text-xs font-bold text-brand tracking-widest uppercase mb-1">{t('userEdit.blockStatus.title')}</h3>
                                        <p className="text-xs text-secondary">{t('userEdit.blockStatus.hint')}</p>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <div
                                            id="blockUserBtn"
                                            onClick={() => !isBlocking && setIsBlockModalOpen(true)}
                                            className={`w-10 h-6 flex items-center rounded-full p-1 transition-colors duration-300 ${isBlocking ? 'cursor-wait' : 'cursor-pointer'} ${user.account.isBlocked ? "bg-brand" : "bg-border"}`}
                                        >
                                            <div
                                                className={`bg-white w-4 h-4 rounded-full shadow-md transform duration-300 ease-in-out ${user.account.isBlocked ? "translate-x-4" : ""}`}></div>
                                        </div>
                                        <span id="userBlockStatusLabel" className="text-sm font-bold text-primary">
                                        {user.account.isBlocked ? t('userEdit.blockStatus.blocked') : t('userEdit.blockStatus.active')}
                                    </span>
                                    </div>
                                </div>

                                <div className="mb-10">
                                    <h3 className="text-xs font-bold text-brand tracking-widest uppercase mb-4">{t('userEdit.roles.title')}</h3>
                                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                                        {[
                                            {id: RoleEnum.STUDENT, label: t('userEdit.roles.student')},
                                            {id: RoleEnum.TEACHER, label: t('userEdit.roles.teacher')},
                                            {id: RoleEnum.ADMINISTRATOR, label: t('userEdit.roles.admin')}
                                        ].map((role) => {
                                            const isChecked = localRoles.includes(role.id);
                                            return (
                                                <div
                                                    id={`roleCard-${role.id}`}
                                                    key={role.id}
                                                    onClick={() => toggleRole(role.id)}
                                                    className={`flex items-center gap-3 p-4 rounded-lg border-2 cursor-pointer transition-colors ${isChecked ? 'bg-active border-brand' : 'bg-base border-transparent hover:border-border'}`}
                                                >
                                                    <div
                                                        className={`w-5 h-5 rounded border flex items-center justify-center transition-colors ${isChecked ? 'bg-brand border-brand' : 'bg-surface border-border'}`}>
                                                        {isChecked && (
                                                            <svg className="w-3 h-3 text-white" fill="none"
                                                                 viewBox="0 0 24 24" stroke="currentColor">
                                                                <path strokeLinecap="round" strokeLinejoin="round"
                                                                      strokeWidth={3} d="M5 13l4 4L19 7"/>
                                                            </svg>
                                                        )}
                                                    </div>
                                                    <span
                                                        className={`text-sm font-bold ${isChecked ? 'text-brand' : 'text-primary'}`}>
                                                    {role.label}
                                                </span>
                                                </div>
                                            )
                                        })}
                                    </div>
                                </div>
                            </>
                        )}

                        <div className="flex items-center justify-end gap-4 pt-6">
                            <button
                                onClick={handleDiscard}
                                disabled={isSaving}
                                className="px-6 py-3 text-xs font-bold text-secondary tracking-widest uppercase hover:text-primary transition-colors disabled:opacity-50"
                            >
                                {t('userEdit.actions.discard')}
                            </button>

                            <span id="saveProfileBtn">
                            <SubmitButton
                                onClick={() => setIsSaveModalOpen(true)}
                                isLoading={isSaving}
                                className="w-auto mt-0 px-8 py-3 text-xs tracking-widest uppercase">
                                {t('userEdit.actions.save')}
                            </SubmitButton>
                            </span>
                        </div>

                    </div>
                </div>
            </div>

            <ConfirmationModal
                isOpen={isSaveModalOpen}
                title={t('common.confirmSaveTitle')}
                description={t('common.confirmSaveDesc')}
                confirmText={t('userEdit.actions.save')}
                onConfirm={handleSave}
                onCancel={() => setIsSaveModalOpen(false)}
                isLoading={isSaving}
            />

            <ConfirmationModal
                isOpen={isBlockModalOpen}
                title={user.account.isBlocked ? t('common.confirmUnblockTitle') : t('common.confirmBlockTitle')}
                description={
                    user.account.isBlocked
                        ? t('common.confirmUnblockDesc')
                        : t('common.confirmBlockDesc')
                }
                confirmText={user.account.isBlocked ? t('common.unblock', 'Unblock') : t('common.block', 'Block')}
                onConfirm={handleBlock}
                onCancel={() => setIsBlockModalOpen(false)}
                isLoading={isBlocking}
            />

            {isAdmin && user && (
                <ChangeOtherPasswordModal
                    isOpen={isChangePasswordModalOpen}
                    onClose={() => setIsChangePasswordModalOpen(false)}
                    user={user.account}
                    onSuccess={() => {
                        setIsChangePasswordModalOpen(false);
                    }}
                />
            )}
        </div>
    );
}