package jpabook.jpashop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public Long save(Member member){
        em.persist(member);
        return member.getId(); //id를 반환하는 이유는 저장을 하고나면 나중에 조회하기 편하게 ?
    }

    public Member find(Long id){
        return em.find(Member.class, id);
    }
}
