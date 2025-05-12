package com.shahidul.commit.trace.oracle.core.model;

import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.TreeSet;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InputCommit {
    String commitHash;
    List<ChangeTag> changeTags;
}
