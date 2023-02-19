package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor //final을 가지고 있는 필드만 생성자 주입을 시켜준다.
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원가입
     */
    @Transactional
    public Long join(Member member){
        //같은 이름 중복체크
        validateDuplicateMember(member);
        //중복이 아니면 아래로직 실행
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        //여기서 예외발생하면 터뜨린다.
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){ //중복이라면
            throw new IllegalStateException("이미 존재하 회원입니다.");
        }
    }// 실무에서는 멀티 쓰레드의 문제등이 있기때문에 이방법을 권장하진 않는다.
     // 실제 db에 name컬럼을 유니크 제약조건을 거는것을 추천한다.

    /**
     * 회원 전체 조회
     */
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    /**
     * 회원 한건 조회
     */
    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }
}
