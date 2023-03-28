package com.urida.board.service.impl;

import com.urida.board.dto.request.ArticleCreateDto;
import com.urida.board.dto.request.ArticleUpdateDto;
import com.urida.board.dto.response.BoardDetailDto;
import com.urida.board.dto.response.BoardListDto;
import com.urida.board.service.BoardService;
import com.urida.board.entity.Board;
import com.urida.comment.dto.CommentResponseDto;
import com.urida.comment.repo.CommentJpqlRepo;
import com.urida.comment.service.CommentService;
import com.urida.exception.NoDataException;
import com.urida.exception.SaveException;
import com.urida.board.repo.BoardJpqlRepo;
import com.urida.likeboard.entity.Likeboard;
import com.urida.likeboard.repo.LikeBoardJpqlRepo;
import com.urida.user.entity.User;
import com.urida.user.repo.UserJpqlRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardJpqlRepo boardJpqlRepo;
    private final LikeBoardJpqlRepo likeBoardJpqlRepo;
    private final UserJpqlRepo userJpqlRepo;
    private final CommentService commentService;


    @Override
    @Transactional(readOnly = true)
    public List<BoardListDto> getArticles(int category_id) {
        List<Board> allArticles = boardJpqlRepo.findAll(category_id);
        List<BoardListDto> articleDtoList = new ArrayList<>();

        for (Board article : allArticles) {
            BoardListDto dto = BoardListDto.builder()
                    .board_id(article.getBoard_id())
                    .title(article.getTitle())
                    .content(article.getContent())
                    .view(article.getView())
                    .dateTime(article.getTime())
                    .category_id(category_id)
                    .likeCnt(likeBoardJpqlRepo.likeCnt(article.getBoard_id()))
                    .commentCnt(commentService.commentCnt(article.getBoard_id()))
                    .nickname(article.getUser().getNickname())
                    .build();

            articleDtoList.add(dto);
        }

        return articleDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDetailDto getArticle(Long id) {
        Board article = boardJpqlRepo.findById(id);
        String nickname = article.getUser().getNickname();
        List<CommentResponseDto> comments = commentService.getComments(id);

        return BoardDetailDto.builder()
                .board_id(article.getBoard_id())
                .title(article.getTitle())
                .content(article.getContent())
                .view(article.getView())
                .dateTime(article.getTime())
                .likeCnt(likeBoardJpqlRepo.likeCnt(article.getBoard_id()))
                .nickname(nickname)
                .comment(comments)
                .build();
        /*return boardJpqlRepo.findById(id);*/
    }

    @Override
    public Board createArticle(ArticleCreateDto articleCreateDto) {
        Optional<User> user = userJpqlRepo.findByUid(articleCreateDto.getUid());
        if (user.isPresent()) {
            Board article = Board.builder()
                    .title(articleCreateDto.getTitle())
                    .content(articleCreateDto.getContent())
                    .category_id(articleCreateDto.getCategory_id())
                    .time(LocalDateTime.now().toString())
                    .user(user.get())
                    .build();

            try {
                boardJpqlRepo.saveArticle(article);
                return article;
            } catch (Exception e) {
                System.out.println(e.toString());
                throw new SaveException("Value invalid");
            }
        } else {
            throw new NoDataException("Value invalid");
        }
    }

    @Override
    public Board updateArticle(ArticleUpdateDto articleUpdateDto, Long id) {
        Board targetArticle = boardJpqlRepo.findById(id);
        Optional<User> currUser = userJpqlRepo.findByUid(targetArticle.getUser().getUid());
        String newContent = articleUpdateDto.getContent();

        if (currUser.isPresent()) {
            Board updatedArticle = Board.builder()
                    .board_id(targetArticle.getBoard_id())
                    .title(targetArticle.getTitle())
                    .content(newContent)
                    .time(LocalDateTime.now().toString())
                    .user(currUser.get())
                    .comment(targetArticle.getComment())
                    .build();

            try {
                boardJpqlRepo.saveArticle(updatedArticle);
                return updatedArticle;
            } catch (Exception e) {
                throw new SaveException("Value invalid");
            }
        } else {
            throw new NoDataException("Value invalid");
        }
    }

    @Override
    public void deleteArticle(Long id) {
        boardJpqlRepo.removeArticle(id);
    }

    @Override
    public Boolean clickLikeArticle(Long board_id, Long uid) {
        Optional<Likeboard> targetArticle = likeBoardJpqlRepo.findByUserAndBoard(uid, board_id);
        if (targetArticle.isPresent()) {
            likeBoardJpqlRepo.deleteLikeBoard(targetArticle);
            return false;
        } else {
            likeBoardJpqlRepo.saveLikeBoard(board_id, uid);
            return likeBoardJpqlRepo.findByUserAndBoard(uid, board_id).get().isStatus();
        }

    }

    @Override
    public int likeCnt(Long board_id) {
        return likeBoardJpqlRepo.likeCnt(board_id);
    }

    @Override
    public Boolean isLiked(Long board_id, Long uid) {
        Optional<Likeboard> isLiked = likeBoardJpqlRepo.findByUserAndBoard(uid, board_id);
        return isLiked.isPresent();
    }

    /*@Override
    public int dislikeArticle(Long id) {
        return boardJpqlRepo.dislikeArticle(id);
    }*/
}
