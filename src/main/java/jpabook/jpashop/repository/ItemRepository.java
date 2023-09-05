package jpabook.jpashop.repository;

import javax.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item){
        if(item.getId() == null){ //아이템은 jpa에 등록하기 전엔 id가 없기 때문에 체크해야한다.
            em.persist(item);
        }else{ // 이미 jpa등록되었기 때문에 update역할을 한다.
            em.merge(item); //merge 는 update와 비슷하다.
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    public List<Item> findAll(){
        return  em.createQuery("select i from Item i",Item.class).getResultList();
    }
}
