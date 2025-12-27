package kuke.board.comment.service;

import kuke.board.comment.entity.ArticleCommentCount;
import kuke.board.comment.entity.Comment;
import kuke.board.comment.entity.CommentPath;
import kuke.board.comment.entity.CommentV2;
import kuke.board.comment.repository.ArticleCommentCountRepository;
import kuke.board.comment.repository.CommentRepositoryV2;
import kuke.board.comment.service.request.CommentCreateRequestV2;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentPageResponseV2;
import kuke.board.comment.service.response.CommentResponseV2;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.function.Predicate.not;

// 댓글 기능의 비즈니스 로직을 총괄하는 핵심 Service 계층
@Service
@RequiredArgsConstructor
public class CommentServiceV2 {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepositoryV2 commentRepositoryV2; // DB 작업을 수행할 저장소를 주입
    private final ArticleCommentCountRepository articleCommentCountRepository;

    // 댓글 생성
    @Transactional
    public CommentResponseV2 create(CommentCreateRequestV2 requestV2){
        // 부모 댓글 찾기 (없으면 null)
        CommentV2 parent = findParent(requestV2);
        // 부모의 경로 가져오기 (부모가 없으면 "")
        CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        // 저장
        CommentV2 comment = commentRepositoryV2.save(
                CommentV2.create(
                        snowflake.nextId(), // Id 직접 생성
                        requestV2.getContent(),
                        requestV2.getArticleId(),
                        requestV2.getWriterId(),
                        // 내 경로 계산
                        parentCommentPath.createChildCommentPath(
                                commentRepositoryV2.findDescendantsTopPath(requestV2.getArticleId(), parentCommentPath.getPath())
                                        .orElse(null)
                        )
                )
        );
        int result = articleCommentCountRepository.increase(requestV2.getArticleId()); // 생성될 때 comment count increase
        // 없을 경우 1로 초기화
        if(result == 0){
            articleCommentCountRepository.save(
                    ArticleCommentCount.init(requestV2.getArticleId(), 1L)
            );
        }
        return CommentResponseV2.from(comment);
    }

    // 부모 찾기
    private CommentV2 findParent(CommentCreateRequestV2 requestV2){
        String parentPath = requestV2.getParentPath();
        if(parentPath == null){
            return null;
        }
        return commentRepositoryV2.findByPath(parentPath)
                .filter(not(CommentV2::getDeleted))
                .orElseThrow();
    }

    public CommentResponseV2 read(Long commentId){
        return CommentResponseV2.from(
                commentRepositoryV2.findById(commentId).orElseThrow()
        );
    }

    // 댓글 삭제
    @Transactional
    public void delete(Long commentId){
        commentRepositoryV2.findById(commentId)
                .filter(not(CommentV2::getDeleted)) // 이미 지워진 건 패스
                .ifPresent(comment ->{
                    if(hasChildren(comment)){ // 이미 자식이 있으면 soft delete / count decrease X
                        comment.delete();
                    } else { // 자식이 없으면 hard delete + 부모 확인
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(CommentV2 comment){
        return commentRepositoryV2.findDescendantsTopPath(
                comment.getArticleId(),
                comment.getCommentPath().getPath()
        ).isPresent();
    }

    private void delete(CommentV2 comment){
        commentRepositoryV2.delete(comment); // 나를 진짜로 삭제 / count decrease O
        articleCommentCountRepository.decrease(comment.getArticleId());
        // 부모로 거슬러 올라가며 확인
        if(!comment.isRoot()){
            commentRepositoryV2.findByPath(comment.getCommentPath().getParentPath())
                    .filter(CommentV2::getDeleted) // 부모가 soft delete 상태이고
                    .filter(not(this::hasChildren)) // 이제 더 이상 자식이 없다면
                    .ifPresent(this::delete); // 부모 hard delete
        }
    }

    public CommentPageResponseV2 readAll(Long articleId, Long page, Long pageSize){
        return CommentPageResponseV2.of(
                commentRepositoryV2.findAll(articleId, (page-1)*pageSize, pageSize)
                        .stream()
                        .map(CommentResponseV2::from)
                        .toList(),
                // 전체 개수 세기
                commentRepositoryV2.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponseV2> readAllInfiniteScroll(Long articleId, String lastPath, Long pageSize){
        // lastPath가 null이면 첫 페이지, 있으면 다음 페이지
        List<CommentV2> comments = lastPath == null ?
                commentRepositoryV2.findAllInfiniteScroll(articleId, pageSize) :
                commentRepositoryV2.findAllInfiniteScroll(articleId, lastPath, pageSize);

        return comments.stream()
                .map(CommentResponseV2::from)
                .toList();
    }

    public Long count(Long articleId){
        return articleCommentCountRepository.findById(articleId)
                .map(ArticleCommentCount::getArticleId)
                .orElse(0L);
    }
}
