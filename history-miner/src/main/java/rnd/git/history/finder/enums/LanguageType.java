package rnd.git.history.finder.enums;

/**
 * @author Shahidul Islam
 * @since 2/2/2024
 */
public enum LanguageType {
    JAVA,PYTHON;
    public static LanguageType from(String language){
        for (LanguageType languageType : values()){
            if (languageType.name().equalsIgnoreCase(language)){
                return languageType;
            }
        }
        throw new RuntimeException("Unsupported Language : " + language);
    }
}
