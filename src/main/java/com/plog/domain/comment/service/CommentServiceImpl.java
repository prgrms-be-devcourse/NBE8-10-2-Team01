package com.plog.domain.comment.service;

import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.comment.dto.ReplyInfoRes;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.repository.PostRepository;
import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.entity.Comment;
import com.plog.domain.comment.repository.CommentRepository;
import com.plog.global.exception.errorCode.CommentErrorCode;
import com.plog.global.exception.errorCode.PostErrorCode;
import com.plog.global.exception.exceptions.CommentException;
import com.plog.global.exception.exceptions.PostException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;


    @Override
    @Transactional
    public Long createComment(Long postId, CommentCreateReq req){

        //TODO: 추후 Post 예외처리 정책으로 수정 예정.
        Post post = postRepository.findById(postId).get();

        Comment parentComment = null;

        String content = req.content();
        Long parentCommentId = req.parentCommentId();

        if(parentCommentId != null){
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new CommentException(
                            CommentErrorCode.COMMENT_NOT_FOUND,
                            "[PostCommentService#createComment] parent comment not found. parentCommentId=" + parentCommentId,
                            "부모 댓글이 존재하지 않습니다."
                    ));
        }

        Comment comment = Comment.builder()
                .post(post)
                .content(content)
                .parent(parentComment)
                .build();

        return commentRepository.save(comment).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<CommentInfoRes> getCommentsByPostId(Long postId, Pageable pageable) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(
                        PostErrorCode.POST_NOT_FOUND,
                        "[CommentService#getCommentsByPostId] can't find post by id : " + postId,
                        "존재하지 않는 게시물입니다."
                ));

        Slice<Comment> comments = commentRepository.findByPostIdAndParentIsNull(postId, pageable);

        return comments.map(CommentInfoRes::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ReplyInfoRes> getRepliesByCommentId(Long commentId, Pageable pageable) {

        Comment parent = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND,
                        "[CommentService#getRepliesByCommentId] 부모 댓글이 존재하지 않음: " + commentId,
                        "존재하지 않는 댓글입니다."));

        Slice<Comment> replies = commentRepository.findByParentId(commentId, pageable);

        return replies.map(ReplyInfoRes::new);
    }

    @Override
    @Transactional
    public void updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(
                        CommentErrorCode.COMMENT_NOT_FOUND,
                        "[PostCommentService#updateComment] can't find comment by id : " + commentId,
                        "존재하지 않는 댓글입니다."
                ));

        comment.modify(content);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(
                        CommentErrorCode.COMMENT_NOT_FOUND,
                        "[PostCommentService#deleteComment] can't find comment by id : " + commentId,
                        "존재하지 않는 댓글입니다."
                ));

        if(comment.isDeleted()){
            return;
        }

        boolean hasChildComments = commentRepository.existsByParent(comment);

        if(hasChildComments){
            comment.softDelete();
        }else{
            commentRepository.delete(comment);
        }
    }
}
