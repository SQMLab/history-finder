package com.shahidul.commit.trace.oracle.core.mongo.entity;

import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AdditionalCommitInfoUdt {
    ChangeTag changeTag;
    String oldMethodName;
    String newMethodName;
    String oldSignature;
    String newSignature;
    String oldFile;
    String newFile;
}
