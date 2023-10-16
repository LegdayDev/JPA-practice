package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OneToOne , ManyToOne -> 컬렉션이 아닌 관계
 * Order 조회
 * Order -> Member 연관
 * Order -> Delivery 연관
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * <h2>V1 : 엔티티를 직접 반환</h2>
     * <p>Entity 를 직접 노출하면서 무한참조가 일어나게 된다.-> API 통신시에 엔티티를 직접 노출하지 말자 </p>
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all; // order -> member -> order .... 무한루프에 빠진다.(연관관계 무한참조)
    }

    /**
     * <h2>V2 : Entity를 직접 반환하지 않고 API 스펙에 맞춘 DTO 를 반환</h2>
     * <p>
     * Entity 가 아닌 별도의 DTO 를 만들어 노출하지만 N + 1 문제
     * -> Lazy 전략 때문에 연관 엔티티를 직접 건들때 호출
     * </p>
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

    }

    /**
     * <h2>V3 : 패치 조인을 이용한 N+1 문제 해결</h2>
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    /**
     * <h2>V4 : JPA 에서 DTO 로 바로 조회 </h2>
     * <p>
     *     Repository 에서 Controller 에 의존하면 안되기 떄문에 Repository 에 별도의 DTO 클래스를 생성
     *     Repository 에서 JPQL 을 작성하고 new 키워드를 사용하여 DTO 에 필드와 바인딩 해준다.
     *     V3 와는 결과값은 같지만 쿼리문을 보면 Select 절에 원하는 컬럼만 뽑혀서 쿼리가 짧아진다.
     * </p>
     * <li>단점 : 코드의 재상용성이 떨어지므로 Repository 에 별도의 패키지를 생성하여 따로 관리</li>
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        public SimpleOrderDto(Order order){
            orderId=order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }

}
