package com.example.social.app.business.service.feed.impl;

import com.example.social.app.business.dto.feed.*;
import com.example.social.app.business.mapper.FeedMapper;
import com.example.social.app.business.service.feed.FeedService;
import com.example.social.app.business.service.notification.NotificationService;
import com.example.social.app.db.dao.follow.FollowRepository;
import com.example.social.app.db.dao.post.CommentRepository;
import com.example.social.app.db.dao.post.PostLikesRepository;
import com.example.social.app.db.dao.post.PostRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.post.CommentsEntity;
import com.example.social.app.db.entity.post.PostEntity;
import com.example.social.app.db.entity.post.PostLikesEntity;
import com.example.social.app.db.entity.post.PostMediaEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import com.example.social.app.enums.MediaType;
import com.example.social.app.enums.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikesRepository postLikesRepository;
    private final UsersRepository usersRepository;
    private final FollowRepository followRepository;
    private final FeedMapper feedMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getFeed(int page, int size, String keycloakId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Long> followingIds = followRepository.findFollowingUserIdsByFollower(currentUser);
        Page<PostEntity> postPage;

        if (followingIds.isEmpty()) {
            // Fallback to all posts if not following anyone yet
            postPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            postPage = postRepository.findByUserIdInOrderByCreatedAtDesc(followingIds, pageable);
        }

        return postPage.map(post -> buildPostDTO(post, currentUser));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getExplore(int page, int size, String keycloakId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostEntity> postPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        return postPage.map(post -> buildPostDTO(post, currentUser));
    }

    @Override
    @Transactional(readOnly = true)
    public PostDTO getPost(Long postId, String keycloakId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        return buildPostDTO(post, currentUser);
    }

    @Override
    @Transactional
    public PostDTO createPost(CreatePostRequest request, String keycloakId) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        PostEntity post = new PostEntity();
        post.setContent(request.getContent());
        post.setUser(user);

        if (request.getMedia() != null && !request.getMedia().isEmpty()) {
            List<PostMediaEntity> mediaList = new ArrayList<>();
            for (MediaRequest mediaReq : request.getMedia()) {
                PostMediaEntity media = new PostMediaEntity();
                media.setType(MediaType.valueOf(mediaReq.getType()));
                media.setUrl(mediaReq.getUrl());
                media.setPost(post);
                mediaList.add(media);
            }
            post.setMedia(mediaList);
        }

        postRepository.save(post);
        return buildPostDTO(post, user);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, String keycloakId) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    @Override
    @Transactional
    public void toggleLike(Long postId, String keycloakId) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        postLikesRepository.findByPostAndUser(post, user)
                .ifPresentOrElse(
                        postLikesRepository::delete,
                        () -> {
                            PostLikesEntity like = new PostLikesEntity();
                            like.setPost(post);
                            like.setUser(user);
                            postLikesRepository.save(like);

                            if (!post.getUser().getId().equals(user.getId())) {
                                notificationService.createNotification(
                                        post.getUser().getId(), user.getId(),
                                        NotificationType.LIKE, post.getId(),
                                        user.getFirstName() + " liked your post"
                                );
                            }
                        }
                );
    }

    @Override
    @Transactional
    public CommentDTO addComment(Long postId, CreateCommentRequest request, String keycloakId) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        CommentsEntity comment = new CommentsEntity();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setUser(user);
        commentRepository.save(comment);

        if (!post.getUser().getId().equals(user.getId())) {
            notificationService.createNotification(
                    post.getUser().getId(), user.getId(),
                    NotificationType.COMMENT, post.getId(),
                    user.getFirstName() + " commented on your post"
            );
        }

        UserBriefDTO userBrief = feedMapper.toUserBrief(user);
        return feedMapper.toCommentDTO(comment, userBrief);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String keycloakId) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        CommentsEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public CommentDTO updateComment(Long commentId, String content, String keycloakId) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        CommentsEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only edit your own comments");
        }

        if (comment.getCreatedAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
            throw new SecurityException("Comments can only be edited within 15 minutes of posting");
        }

        comment.setContent(content);
        commentRepository.save(comment);

        UserBriefDTO userBrief = feedMapper.toUserBrief(user);
        CommentDTO dto = feedMapper.toCommentDTO(comment, userBrief);
        return dto;
    }

    private PostDTO buildPostDTO(PostEntity post, UsersEntity currentUser) {
        UserBriefDTO userBrief = feedMapper.toUserBrief(post.getUser());
        List<MediaDTO> media = post.getMedia() != null
                ? feedMapper.toMediaDTOList(post.getMedia())
                : Collections.emptyList();

        long likeCount = postLikesRepository.countByPost(post);
        long commentCount = post.getComments() != null ? post.getComments().size() : 0;
        boolean likedByMe = postLikesRepository.existsByPostAndUser(post, currentUser);

        List<CommentsEntity> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(post.getId());
        List<CommentDTO> recentComments = comments.stream()
                .limit(3)
                .map(c -> {
                    UserBriefDTO commentUserBrief = feedMapper.toUserBrief(c.getUser());
                    return feedMapper.toCommentDTO(c, commentUserBrief);
                })
                .toList();

        return feedMapper.toPostDTO(post, userBrief, media, likeCount, commentCount, likedByMe, recentComments);
    }
}
