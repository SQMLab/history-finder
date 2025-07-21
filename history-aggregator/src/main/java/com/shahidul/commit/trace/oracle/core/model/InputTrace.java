package com.shahidul.commit.trace.oracle.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rnd.git.history.finder.dto.InputCommit;

import java.util.List;

/**
 * @since 11/16/2023
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InputTrace {
    List<InputCommit> commits;
}
