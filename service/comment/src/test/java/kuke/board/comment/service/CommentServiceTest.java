package kuke.board.comment.service;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // JUNIT5에서 Mockito를 사용하기 위한 필수 설정
class CommentServiceTest {
    @InjectMocks // 가짜 repository(@Mock) 를 CommentService 안에 자동 주입 -> 실제 DB 없이 테스트 가능
    CommentService commentService;
    @Mock // 실제 DB와 통신하는 repository 대신 가짜 repository 생성
    CommentRepository commentRepository;

    @Test
    @DisplayName("삭제할 자식이 있으면, 삭제 표시만 한다.") // 자식이 있어서 못 지우는 경우 (soft delete)
    void deleteShouldMarkDeletedIfHasChildren(){

        // given (상황 설정)
        Long articleId = 1L;
        Long commentId = 2L;
        Comment comment = createComment(articleId, commentId);
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(2L); // 자식이 있다는 뜻

        //when (실행)
        commentService.delete(commentId);

        //then (검증)
        verify(comment).delete(); // Entity의 delete() 메서드가 호출되었는지 확인
    }

    @Test
    @DisplayName("하위 댓글이 삭제되고, 삭제되지 않은 부모면, 하위 댓글만 삭제한다.") // 단순 대댓글 삭제 (hard delete)
    void deleteShouldDeleteChildrenOnlyIfDeletedParent(){

        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId, parentCommentId);
        given(comment.isRoot()).willReturn(false);

        Comment parentComment = mock(Comment.class);
        given(parentComment.getDeleted()).willReturn(false); // 부모는 살아있음

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L); // 나는 자식이 없음

        given(commentRepository.findById(parentCommentId))
                .willReturn(Optional.of(parentComment));

        //when
        commentService.delete(commentId);

        //then
        verify(commentRepository).delete(comment); // 나는 DB 에서 삭제됨 (hard delete)
        verify(commentRepository, never()).delete(parentComment); // 부모는 건드리지 않음 (never)
    }

    @Test
    @DisplayName("하위 댓글이 삭제되고, 삭제된 부모면 재귀적으로 모두 삭제한다.")
    void deleteShouldDeleteAllRecursivelyIfDeletedParent(){

        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId, parentCommentId);
        given(comment.isRoot()).willReturn(false);

        Comment parentComment = createComment(articleId, parentCommentId);
        given(parentComment.isRoot()).willReturn(true);
        given(parentComment.getDeleted()).willReturn(true); // 부모는 이미 soft delete 상태

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L); // 나는 자식이 없음

        given(commentRepository.findById(parentCommentId))
                .willReturn(Optional.of(parentComment));
        given(commentRepository.countBy(articleId, parentCommentId, 2L)).willReturn(1L); // 부모도 나를 제외한 자식이 없음


        //when
        commentService.delete(commentId);

        //then
        verify(commentRepository).delete(comment); // 나도 hard delete
        verify(commentRepository).delete(parentComment); // 부모도 hard delete
    }


    private Comment createComment(Long articleId, Long commentId){
        Comment comment = mock(Comment.class); // Mock 객체로 생성하면 테스트 목적 상 Getter 호출 시 원하는 값만 뱉어주면 되므로 편함
        given(comment.getArticleId()).willReturn(articleId); // Getter 호출 시 행동 정의
        given(comment.getCommentId()).willReturn(commentId);
        return comment;
    }

    private Comment createComment(Long articleId, Long commentId, Long parentCommentId){
        Comment comment = createComment(articleId, commentId);
        given(comment.getParentCommentId()).willReturn(parentCommentId);
        return comment;
    }
}