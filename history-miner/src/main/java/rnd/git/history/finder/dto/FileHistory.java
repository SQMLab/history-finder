package rnd.git.history.finder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@Builder
@Getter
@ToString
public class FileHistory {
    String commit;
    String path;
    Date committedAt;
}
