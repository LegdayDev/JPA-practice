package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member") // Member(일) : Order(다) 관계
    //mappedBy 는 연관관계에서 주인이되는 테이블의 컬럼명으로 해주면된다.
    //Order와 Member에서는 Order가 FK(외래키)를 가지고 있으므로 연관관게에서 주인이된다.
    //주인인 테이블은 건들필요 없고, 거울이 되는 테이블에서 지정해주면된다.
    private List<Order> orders = new ArrayList<>();



}
