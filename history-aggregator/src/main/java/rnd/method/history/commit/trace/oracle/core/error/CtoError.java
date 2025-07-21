package rnd.method.history.commit.trace.oracle.core.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @since 3/22/2024
 */
@AllArgsConstructor
@Getter
public enum CtoError {
    Commit_Not_Found("1001", "Commit not found"),
    Trace_Not_Found("1002", "Trace not found"),
    File_Read_Error("1003", "File read error"),
    Oracle_File_Write_Error("1004", "Oracle file write error"),
    Commit_Already_Exist("1005", "Commit already exist"),
    Git_Checkout_Failed("1006", "Failed to checkout commit"),
    Java_Method_Parsing_Failed("1007", "Failed to parse methods"),
    Failed_To_Execute_Trace("1008", "Failed to execute trace"),
    Failed_To_Find_Methods("1009", "Failed to find methods"),
    Failed_To_Find_Paths("1010", "Failed to find paths"),
    Failed_To_Find_Repositories("1011", "Failed to find repositories"),
    CodeShovel_Failure("1012", "CodeShovel failed to generate method history"),
    Git_Function_Name_Failure("1013", "Failed to generate method history. Please ensure that Git is properly configured to use the git log func name command. For setup instructions, refer to the README file."),
    Tool_X_Failed_To_Generate_Method_History("1014", "%s failed to generate method history")

    ;
    private String code;
    private String msg;
}
