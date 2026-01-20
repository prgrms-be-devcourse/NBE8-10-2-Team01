package com.plog.domain.post.service;

import com.plog.domain.post.dto.PostInfoRes;
import java.util.List;

/**
 * 게시물 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 * <p>
 * 게시물의 생성, 상세 조회, 목록 조회 등의 기능을 정의하며,
 * 각 기능의 상세한 비즈니스 규칙과 처리 로직에 대한 명세를 제공합니다.
 *
 * <p><b>주요 기능 요약:</b><br>
 * 1. 게시물 작성: 마크다운 본문 파싱 및 요약본 자동 생성 <br>
 * 2. 상세 조회: 조회수 증가 로직 포함 <br>
 * 3. 목록 조회: 최신순 정렬 반환
 *
 * @author MintyU
 * @since 2026-01-19
 */
public interface PostService {

    /**
     * 새로운 게시물을 작성하고 저장합니다.
     * <p><b>실행 로직:</b><br>
     * 1. 입력받은 마크다운(Markdown) 형식의 본문에서 특수 기호를 제거하여 순수 텍스트를 추출합니다. <br>
     * 2. 추출된 텍스트의 앞부분을 최대 150자까지 잘라내어 목록 노출용 요약본(Summary)을 생성합니다. <br>
     * 3. 게시물 상태를 'PUBLISHED'로 설정하여 데이터베이스에 영구 저장합니다.
     *
     * @param title 게시물 제목
     * @param content 마크다운 형식의 본문
     * @return 저장된 게시물의 고유 식별자(ID)
     */
    Long createPost(String title, String content);

    /**
     * 특정 ID의 게시물을 상세 조회합니다.
     * <p><b>실행 로직:</b><br>
     * 1. 전달받은 ID로 게시물을 검색하며, 존재하지 않을 경우 {@code PostException}을 발생시킵니다. <br>
     * 2. 조회에 성공하면 해당 게시물의 누적 조회수를 1 증가시킵니다. <br>
     * 3. 엔티티 객체를 응답용 DTO({@code PostInfoRes})로 변환하여 반환합니다.
     *
     * @param id 게시물 고유 식별자
     * @return 조회된 게시물 정보 DTO
     * @throws com.plog.global.exception.exceptions.PostException 게시물을 찾을 수 없을 때 발생
     */
    PostInfoRes getPostDetail(Long id);

    /**
     * 모든 게시물 목록을 조회합니다.
     * <p><b>실행 로직:</b><br>
     * 1. 데이터베이스의 모든 게시물을 조회합니다. <br>
     * 2. 게시물의 ID를 기준으로 내림차순(최신순) 정렬을 수행합니다. <br>
     * 3. 조회된 모든 엔티티를 응답용 DTO 리스트로 변환하여 반환합니다.
     *
     * @return 최신순으로 정렬된 게시물 정보 DTO 리스트
     */
    List<PostInfoRes> getPosts();

    /**
     * 기존 게시물을 수정합니다.
     * <p><b>실행 로직:</b><br>
     * 1. 전달받은 ID로 게시물을 조회하며, 없을 경우 예외를 발생시킵니다. <br>
     * 2. 제목과 본문을 새로운 내용으로 교체합니다. <br>
     * 3. 본문이 수정됨에 따라 마크다운 파싱 및 요약본(Summary) 생성 로직을 다시 실행하여 업데이트합니다.
     *
     * @param id 수정할 게시물 ID
     * @param title 새로운 제목
     * @param content 새로운 마크다운 본문
     */
    void updatePost(Long id, String title, String content);
}