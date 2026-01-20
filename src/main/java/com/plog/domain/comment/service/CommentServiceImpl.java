package com.plog.domain.comment.service;

import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.repository.PostRepository;
import com.plog.domain.comment.dto.CommentGetRes;
import com.plog.domain.comment.entity.Comment;
import com.plog.domain.comment.repository.CommentRepository;
import com.plog.global.exception.errorCode.CommentErrorCode;
import com.plog.global.exception.exceptions.CommentException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

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
    public List<CommentGetRes> getCommentsByPostId(Long postId, int page) {

        int pageSize = 10;

        // TODO: 추후 postService.findById(postId)로 교체
        Page<Comment> commentPage = commentRepository.findAllByPostId(
                postId,
                PageRequest.of(page, pageSize, Sort.by("createDate").descending()) // 최신 댓글 먼저
        );

        return commentPage.stream()
                .map(CommentGetRes::new)
                .toList();
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(
                        CommentErrorCode.COMMENT_NOT_FOUND,
                        "[PostCommentService#updateComment] can't find comment by id : " + commentId,
                        "존재하지 않는 댓글입니다."
                ));

        comment.modify(content);

        return comment;
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
