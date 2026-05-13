package pl.lodz.p.it.ssbd2026.ssbd01.config.consts;

public class AccountConsts {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_TEACHER = "TEACHER";

    public final static int NAME_LENGTH = 50;
    public final static int SURNAME_LENGTH = 50;
    public final static int PASSWORD_HASH_LENGTH = 64;
    public final static int LOGIN_LENGTH = 50;
    public final static int EMAIL_LENGTH = 50;
    public final static int IP_LENGTH = 45;

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String DEFAULT_SORT_BY = "login";
    public static final String SORT_ACCESS_LEVEL_ALIAS = "accessLevel";
    public static final String SORT_ACCESS_LEVEL_PATH = "accessLevelList.accessLevelDict.accessLevelName";
}
