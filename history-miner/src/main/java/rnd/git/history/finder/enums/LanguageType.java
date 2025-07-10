package rnd.git.history.finder.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Shahidul Islam
 * @since 3/23/2024
 */
@AllArgsConstructor
@Getter
public enum LanguageType {
    JAVA("Java");
    String code;
    public static LanguageType from(String code){
        for (LanguageType languageType : values()){
            if (languageType.getCode().equalsIgnoreCase(code)){
                return languageType;
            }
        }
        throw new RuntimeException("Illegal argument exception : " + code);
    }
}
