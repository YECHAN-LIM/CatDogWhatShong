package com.demo.cdmall1.domain.imageboard.entity;

import org.springframework.data.repository.*;

public interface ImageBoardMemberRepository extends CrudRepository<ImageBoardMember, ImageBoardMemberId>{

	boolean existsByIbnoAndUsername(Integer ibno, String loginId);

}
