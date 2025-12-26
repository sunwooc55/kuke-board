package kuke.board.comment.service;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.entity.CommentPath;
import kuke.board.comment.entity.CommentV2;
import kuke.board.comment.repository.CommentRepositoryV2;
import kuke.board.comment.service.request.CommentCreateRequestV2;
import kuke.board.comment.service.response.CommentResponseV2;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentServiceV2 {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepositoryV2 commentRepositoryV2;

    @Transactional
    public CommentResponseV2 create(CommentCreateRequestV2 requestV2){
        CommentV2 parent = findParent(requestV2);
        CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        CommentV2 comment = commentRepositoryV2.save(
                CommentV2.create(
                        snowflake.nextId(),
                        requestV2.getContent(),
                        requestV2.getArticleId(),
                        requestV2.getWriterId(),
                        parentCommentPath.createChildCommentPath(
                                commentRepositoryV2.findDescendantsTopPath(requestV2.getArticleId(), parentCommentPath.getPath())
                                        .orElse(null)
                        )
                )
        );

        return CommentResponseV2.from(comment);
    }

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

    @Transactional
    public void delete(Long commentId){
        commentRepositoryV2.findById(commentId)
                .filter(not(CommentV2::getDeleted))
                .ifPresent(comment ->{
                    if(hasChildren(comment)){
                        comment.delete();
                    } else {
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
        commentRepositoryV2.delete(comment);
        if(!comment.isRoot()){
            commentRepositoryV2.findByPath(comment.getCommentPath().getParentPath())
                    .filter(CommentV2::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
        }
    }

}
