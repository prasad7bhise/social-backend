package com.example.social.app.business.service.feed;

import com.example.social.app.business.dto.feed.*;
import org.springframework.data.domain.Page;

public interface FeedService {
    Page<PostDTO> getFeed(int page, int size, String keycloakId);

    Page<PostDTO> getExplore(int page, int size, String keycloakId);

    PostDTO getPost(Long postId, String keycloakId);

    PostDTO createPost(CreatePostRequest request, String keycloakId);

    void deletePost(Long postId, String keycloakId);

    void toggleLike(Long postId, String keycloakId);

    CommentDTO addComment(Long postId, CreateCommentRequest request, String keycloakId);

    void deleteComment(Long commentId, String keycloakId);

    CommentDTO updateComment(Long commentId, String content, String keycloakId);
}
