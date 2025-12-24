package kuke.board.article.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE) // new PageLimitCalculator()로 객체 생성하는 것을 막음(오직 계산 기능만 제공하는 순수 도구이기 때문)
public final class PageLimitCalculator {
    public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount){
        return (((page -1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
    }
}
