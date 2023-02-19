package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository //스프링 빈에 등록
@RequiredArgsConstructor
public class MemberRepository {

    //@PersistenceContext //spring이 엔티티매니저를 만들어 주입(injection)해준다.
    //위 어노테이션도 사용가능하지만 현재 스프링부트에서는 아래방식을 지원한다.
    private final EntityManager em;

    public void save(Member member){
        em.persist(member);
    }

    public Member findOne(Long id){
        return em.find(Member.class,id);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name=:name",Member.class)
                .setParameter("name",name) // name으로 바인딩이 된다.
                .getResultList();
    }

    //==비즈니스 로직==//

}
