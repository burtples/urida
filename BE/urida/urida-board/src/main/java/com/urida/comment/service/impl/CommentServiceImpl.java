package com.urida.comment.service.impl;

import com.urida.board.entity.Board;
import com.urida.board.repo.BoardJpqlRepo;
import com.urida.comment.dto.CommentRequestDto;
import com.urida.comment.dto.CommentResponseDto;
import com.urida.comment.entity.Comment;
import com.urida.comment.repo.CommentJpqlRepo;
import com.urida.comment.service.CommentService;
import com.urida.exception.NoDataException;
import com.urida.exception.SaveException;
import com.urida.user.entity.User;
import com.urida.user.repo.UserJpqlRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final UserJpqlRepo userJpqlRepo;
    private final BoardJpqlRepo boardJpqlRepo;
    private final CommentJpqlRepo commentJpqlRepo;

    @Override
    public Comment createComment(CommentRequestDto commentRequestDto) {
        Board targetArticle = boardJpqlRepo.getArticle(commentRequestDto.getBoard_id());
        Optional<User> user = userJpqlRepo.findByUid(commentRequestDto.getUid());
        if(user.isPresent()) {
            Comment comment = Comment.builder()
                    .content(commentRequestDto.getContent())
                    .dateTime(LocalDateTime.now().toString())
                    .board(targetArticle)
                    .user(user.get())
                    .build();
            try{
                commentJpqlRepo.saveComment(comment);
                return comment;
            } catch(Exception e) {
                throw new SaveException("Value Invalid");
            }
        } else{
            throw new NoDataException("Value Invalid");
        }
    }

    // 특정 게시물 댓글 불러오기
    @Override
    public List<CommentResponseDto> getComments (Long board_id) {
        List<Comment> comments = commentJpqlRepo.getComments(board_id);
        List<CommentResponseDto> commentDto = new ArrayList<>();
        for( Comment comment : comments){
            CommentResponseDto dto = CommentResponseDto.builder()
                    .comment_id(comment.getComment_id())
                    .content(comment.getContent())
                    .dateTime(comment.getDateTime())
                    .board_id(board_id)
                    .uid(comment.getUser().getUid())
                    .nickname(comment.getUser().getNickname())
                    .build();

            commentDto.add(dto);
        }
        return commentDto;
    }

    // 특정 댓글 수정하기
    @Override
    public Comment updateComment(Long comment_id, CommentRequestDto commentRequestDto) {
        Comment targetComment = commentJpqlRepo.findById(comment_id);
        String newContent = commentRequestDto.getContent();

        Comment updatedComment = Comment.builder()
                .comment_id(targetComment.getComment_id())
                .content(newContent)
                .dateTime(LocalDateTime.now().toString())
                .board(targetComment.getBoard())
                .user(targetComment.getUser())
                .build();

        commentJpqlRepo.saveComment(updatedComment);

        return updatedComment;
    }

    // 댓글 개수
    @Override
    public int commentCnt(Long board_id) {
        return commentJpqlRepo.getComments(board_id).size();
    }

    // 댓글 삭제
    @Override
    public Long removeComment(Long comment_id) {
        commentJpqlRepo.deleteComment(comment_id);
        return comment_id;
    }
}