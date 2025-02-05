package ewm.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    public static PageRequest createPageRequestAsc(String sortBy, int from, int size) {
        return PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(sortBy).ascending());
    }
}
