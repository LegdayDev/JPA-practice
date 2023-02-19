package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jdk.jfr.Description;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
/** @Ingeritance 상속관계테이블에 전략을 정하는 어노테이션이다
 * SINGLE_TABLE : 한 테이블에 다 넣는 전략
 * JOIN : 가장 정규화된 방식
 * TABLE_PER_CLASS :
 */
@DiscriminatorColumn(name = "dtype") //SINGLE_TABLE에서 구분하기위해
@Getter @Setter
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    //==비즈니스 로직==//
    /**
     * stock(재고) 증가
     */
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }

    /**
     * stock(재고) 감소
     */
    public void removeStock(int quantity){
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {//수량이 없을 때
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
