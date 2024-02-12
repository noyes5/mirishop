package com.hh.mirishop.comment.service;

import com.hh.mirishop.comment.dto.CommentRequest;

import java.util.List;

public interface CommentService {

    Long createCommentOrReply(CommentRequest request, Long memberNumber, Long postId);

    void deleteComment(Long commentId, Long memberNumber);

    Long findPostIdByCommentId(Long commentId);

    List<Long> findCommentIdsByMemberNumber(Long memberNumber);
}

