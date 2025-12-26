package kuke.board.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// 단순히 데이터를 담는 그릇(DTO)와는 다르게, DB의 영속성을 담당하며 comment 테이블과 1:1 로 매핑되는 데이터 객체
@Table(name = "comment") // DB의 comment 라는 테이블과 연결
@Getter
@Entity // 이 클래스가 DB의 테이블과 매핑되는 객체임을 명시
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 프록시 객체를 생성하기 위해 매개변수 없는 기본 생성자가 반드시 필요. 이 기본 생성자를 public으로 열어두면 개발자가 불완전한 객체를 무분별하게 생성할 위험이 있으므로 protected 로 제한
public class Comment {
    @Id // commentID를 테이블의 Primary Key로 지정
    private Long commentId;
    private String content;
    private Long parentCommentId;
    private Long articleId; // shard key
    private Long writerId;
    private Boolean deleted;
    private LocalDateTime createdAt;

    public static Comment create(Long commentId, String content, Long parentCommentId, Long articleId, Long writerId){
        Comment comment = new Comment();
        comment.commentId = commentId;
        comment.content = content;
        // parentCommentId 가 없으면(null) 자기 자신의 Id를 parentCommentId로 설정
        comment.parentCommentId = parentCommentId == null ? commentId : parentCommentId;
        comment.articleId = articleId;
        comment.writerId = writerId;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();
        return comment;
    }

    // depth1 comment 인지 확인
    public boolean isRoot(){
        return parentCommentId.longValue() == commentId;
    }

    public void delete(){
        deleted = true;
    }
}
