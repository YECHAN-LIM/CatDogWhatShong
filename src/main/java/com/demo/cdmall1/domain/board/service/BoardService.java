package com.demo.cdmall1.domain.board.service;

import java.io.*;
import java.nio.file.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

import org.hibernate.internal.build.*;
import org.jsoup.*;
import org.modelmapper.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.util.*;
import org.springframework.web.multipart.*;

import com.demo.cdmall1.domain.board.entity.*;
import com.demo.cdmall1.util.*;
import com.demo.cdmall1.web.dto.*;

import lombok.*;

@RequiredArgsConstructor
@Service
public class BoardService {
	private final BoardRepository dao; 
	private final ModelMapper modelMapper;
	
	// 추가
	public Board write(BoardDto.Write dto, String loginId) {
		Board board = dto.toEntity().setWriter(loginId);
		Jsoup.parseBodyFragment(dto.getContent()).getElementsByTag("img").forEach(img->{
			// http://localhost:8081/temp/image?imagename=aaa.jpg;
			String imageUrl = img.attr("src");
			String imageName = imageUrl.substring(imageUrl.lastIndexOf("=")+1);
			File tempImage = new File(ZmallConstant.TEMP_FOLDER, imageName);
			File image = new File(ZmallConstant.IMAGE_FOLDER, imageName);
			try {
				if(tempImage.exists()) {
					FileCopyUtils.copy(Files.readAllBytes(tempImage.toPath()), image);
					tempImage.delete();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		});
		board.setContent(dto.getContent().replaceAll(ZmallConstant.CK_FIND_PATTERN, ZmallConstant.CK_REPLACE_PATTERN));
		
		if(dto.getAttachments()==null)
			return dao.save(board);
		
		dto.getAttachments().forEach(uploadFile->{
			// 저장할 파일 이름을 지정(현재시간을 1/1000초 단위로 계산)
			String saveFileName = System.currentTimeMillis() + "-" + uploadFile.getOriginalFilename();
			File saveFile = new File(ZmallConstant.ATTACHMENT_FOLDER, saveFileName);
			try {
				uploadFile.transferTo(saveFile);
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
			boolean isImage = uploadFile.getContentType().toLowerCase().startsWith("image/");
			// board는 관계의 주인이 아니다. board쪽에서 attachment를 저장하면 반영이 안된다
			board.addAttachment(new Attachment(0, null, uploadFile.getOriginalFilename(), saveFileName, uploadFile.getSize(), isImage));
		});
		return dao.save(board);
	}
	
	// 읽기
	@Transactional
	public BoardDto.Read read(Integer bno, String loginId) {
		Board board = dao.findById(bno).orElseThrow(BoardFail.BoardNotFoundException::new);
		board.increaseReadCnt(loginId);		
		List<CommentDto.Read> comments = board.getComments().stream().map(c->c.toDto()).collect(Collectors.toList());
		BoardDto.Read dto = modelMapper.map(board, BoardDto.Read.class);
		return dto.setComments(comments);
	}
	
	// 글 변경
	@Transactional
	public Board update(BoardDto.Update dto, String loginId) {
		Board board = dao.findById(dto.getBno()).orElseThrow(BoardFail.BoardNotFoundException::new);
		if(board.getWriter().equals(loginId)==false)
			throw new BoardFail.IllegalJobException();
		return board.update(dto);
	}

	@Transactional
	public Integer updateCommentCnt(Integer bno) {
		Board board = dao.findById(bno).orElseThrow(BoardFail.IllegalJobException::new);
		return board.updateCommentCnt();
	}

	public BoardDto.ListResponse list(Integer pageno, String writer) {
		// JPARepository의 findAll은 findById와 마찬가지로 관련 엔티티를 모두 읽어온다 -> 상관없다면 사용
		// Pageable pageable = PageRequest.of(pageno-1, 10, Sort.by(Sort.Direction.DESC, "bno"));
		// return dao.findAll(pageable);
		
		// 글의 전체 개수, 페이지 번호, 페이지 사이즈, content(글 목록)을 보내줘야 프론트에서 페이징할 수 있다....Map을 사용하자
		Pageable pageable = PageRequest.of(pageno-1, 10);
		BoardDto.ListResponse dto = new BoardDto.ListResponse(dao.readAll(pageable, writer), dao.countAll(writer), pageno, 10);
		//Map<String,Object> map = new HashMap<>();
		//map.put("content", dao.readAll(pageable, writer));
		//map.put("totalcount", dao.countAll(writer));
		//map.put("pageno", pageno);
		//map.put("pagesize", 10);
		return dto;
	}

	@Transactional
	public Integer goodOrBad(Integer bno, Integer state) {
		Board board = dao.findById(bno).orElseThrow(BoardFail.BoardNotFoundException::new);
		if(state==0) {
			board.setGoodCnt(board.getGoodCnt()+1);
			return board.getGoodCnt();
		} else if(state==1) {
			board.setBadCnt(board.getBadCnt()+1);
			return board.getBadCnt();
		} else if(state==2)
			return board.getGoodCnt();
		return board.getBadCnt();
	}

	// ck 이미지 업로드
	public CKResponse ckImageUpload(MultipartFile upload) {
		if(upload!=null && upload.isEmpty()==false && upload.getContentType().toLowerCase().startsWith("image/")) {
			String imageName = UUID.randomUUID().toString() + ".jpg";
			File file = new File(ZmallConstant.TEMP_FOLDER, imageName);
			try {
				upload.transferTo(file);
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
			return new CKResponse(1, imageName, ZmallConstant.TEMP_URL + imageName);
		}
		return null;
	}

	@Transactional
	public Integer updateAttachment(Integer bno) {
		Board board = dao.findById(bno).orElseThrow(BoardFail.BoardNotFoundException::new);
		return board.updateAttachmentCnt(bno);
	}
	
	@Transactional
	public Void delete(Integer bno, String loginId) {
		Board board = dao.findById(bno).orElseThrow(BoardFail.BoardNotFoundException::new);
		if(board.getWriter().equals(loginId)==false)
			throw new BoardFail.IllegalJobException();
		dao.delete(board);
		return null;
	}
}








